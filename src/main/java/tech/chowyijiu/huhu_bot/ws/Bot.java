package tech.chowyijiu.huhu_bot.ws;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tech.chowyijiu.huhu_bot.constant.GocqActionEnum;
import tech.chowyijiu.huhu_bot.entity.gocq.message.ForwardMessage;
import tech.chowyijiu.huhu_bot.entity.gocq.request.RequestBox;
import tech.chowyijiu.huhu_bot.entity.gocq.response.FriendInfo;
import tech.chowyijiu.huhu_bot.entity.gocq.response.GroupInfo;
import tech.chowyijiu.huhu_bot.entity.gocq.response.GroupMember;
import tech.chowyijiu.huhu_bot.entity.gocq.response.SelfInfo;
import tech.chowyijiu.huhu_bot.event.Event;
import tech.chowyijiu.huhu_bot.exception.gocq.ActionFailed;
import tech.chowyijiu.huhu_bot.utils.GocqSyncRequestUtil;

import java.util.HashMap;
import java.util.List;

/**
 * @author elastic chow
 * @date 17/5/2023
 */
@Slf4j
@Getter
@ToString
public class Bot {

    private final Long userId;
    private final WebSocketSession session;

    private List<GroupInfo> groups;

    public Bot(Long userId, WebSocketSession session) {
        this.userId = userId;
        this.session = session;
    }

    public void update() {
        groups = this.getGroupList();
    }

    /**
     * 私有 callApi
     */
    private void callApi(GocqActionEnum action, HashMap<String, Object> paramsMap) {
        RequestBox<HashMap<String, Object>> requestBox = new RequestBox<>();
        requestBox.setAction(action.getAction());
        requestBox.setParams(paramsMap);
        sessionSend(JSONObject.toJSONString(requestBox));
    }

    /**
     * 提供给外部调用的callApi
     * callApi(GocqActionEnum action, key, value, key, value ...)
     *
     * @param action GocqActionEnum
     * @param params 参数 key, value
     */
    public void callApi(GocqActionEnum action, Object... params) {
        HashMap<String, Object> paramsMap = new HashMap<>();
        int length = params.length;
        if (length % 2 != 0) {
            log.info("[Bot] callApi invalid params, canceled");
            return;
        }
        for (int i = 0; i < params.length; i += 2) {
            paramsMap.put(params[i].toString(), params[i + 1]);
        }
        this.callApi(action, paramsMap);
    }

    /**
     * 用于有响应数据的gocq api
     * @param action GocqActionEnum
     * @param paramsMap map
     * @return json 字符串数据
     */
    private String callGetApi(GocqActionEnum action, @Nullable HashMap<String, Object> paramsMap) {
        RequestBox<HashMap<String, Object>> requestBox = new RequestBox<>();
        requestBox.setAction(action.getAction());
        if (paramsMap != null) {
            requestBox.setParams(paramsMap);
        }
        String responseStr = GocqSyncRequestUtil.sendSyncRequest(this, action, paramsMap, 5000L);
        if (StringUtils.hasLength(responseStr)) {
            return responseStr;
        }
        throw new ActionFailed("action:" + action.getAction() + " 获取数据为空");
    }

    /**
     * 调用 get api获取数据, 同步, 10s超时
     * @param action GocqActionEnum
     * @param params 参数 key, value, key, value
     * @return json 字符串数据
     */
    public String callGetApi(GocqActionEnum action, Object... params) {
        HashMap<String, Object> paramsMap = new HashMap<>();
        int length = params.length;
        if (length % 2 != 0) {
            log.info("[Bot] callApi invalid params, canceled");
            return null;
        }
        for (int i = 0; i < params.length; i += 2) {
            paramsMap.put(params[i].toString(), params[i + 1]);
        }
        return this.callGetApi(action, paramsMap);
    }

    /**
     * 获取bot信息
     * @return SelfInfo
     */
    public SelfInfo getLoginInfo() {
        String data = this.callGetApi(GocqActionEnum.GET_LOGIN_INGO, (HashMap<String, Object>) null);
        return JSONObject.parseObject(data, SelfInfo.class);
    }

    /**
     * 获取好友列表
     * @return List<FriendInfo>
     */
    public List<FriendInfo> getFriendList() {
        String data = this.callGetApi(GocqActionEnum.GET_FRIEND_LIST, (HashMap<String, Object>) null);
        return JSONArray.parseArray(data, FriendInfo.class);
    }

    public void deleteFriend(Long userId) {
        this.callApi(GocqActionEnum.DELETE_FRIEND, (HashMap<String, Object>) null);
    }


    /**
     * 获取群成员列表
     * GocqActionEnum.GET_GROUP_MEMBER_LIST
     * @param groupId  groupId
     * @param noCache 为true时, 不使用缓存
     **/
    public List<GroupMember> getGroupMembers(Long groupId, boolean noCache) {
        String data = callGetApi(GocqActionEnum.GET_GROUP_MEMBER_LIST,
                "group_id", groupId, "no_cache", noCache);
        return JSONArray.parseArray(data, GroupMember.class);
    }

    /**
     * 获取群列表
     * @param noCache 默认false, 使用缓存
     * @return List<GroupInfo>
     */
    public List<GroupInfo> getGroupList(boolean noCache) {
        String data = this.callGetApi(GocqActionEnum.GET_GROUP_LIST, "no_cache", noCache);
        return JSONArray.parseArray(data, GroupInfo.class);
    }

    /**
     * 不使用缓存获取群列表
     * @return List<GroupInfo>
     */
    public List<GroupInfo> getGroupList() {
        //todo 想想再哪里更新groups最好, 目前感觉最好是 入群/退群更新,
        // 或者GroupMessageEvent进来,看看groups里有没有这个groupId
        groups = this.getGroupList(true);
        return groups;
    }

    /**
     * 获取群成员详细信息
     * GocqActionEnum.GET_GROUP_MEMBER_INFO
     * @param groupId  groupId
     * @param userId userId
     * @param noCache 为true时, 不使用缓存
     * @return GroupMember
     */
    public GroupMember getGroupMember(Long groupId, Long userId, boolean noCache) {
        String data = callGetApi(GocqActionEnum.GET_GROUP_MEMBER_INFO,
                "group_id", groupId, "user_id", userId, "no_cache", noCache);
        return JSONObject.parseObject(data, GroupMember.class);
    }

    /**
     * 发送群消息
     *
     * @param groupId    群号
     * @param message    消息
     * @param autoEscape 是否以纯文本发送 true:以纯文本发送，不解析cq码
     */
    public void sendGroupMessage(Long groupId, String message, boolean autoEscape) {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("group_id", groupId);
        paramsMap.put("message", message);
        paramsMap.put("auto_escape", autoEscape);
        this.callApi(GocqActionEnum.SEND_GROUP_MSG, paramsMap);
    }

    /**
     * 发送私聊消息
     *
     * @param userId     对方qq
     * @param message    消息
     * @param autoEscape 是否以纯文本发送 true:以纯文本发送，不解析cq码
     */
    public void sendPrivateMessage(Long userId, String message, boolean autoEscape) {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("user_id", userId);
        paramsMap.put("message", message);
        paramsMap.put("auto_escape", autoEscape);
        this.callApi(GocqActionEnum.SEND_PRIVATE_MSG, paramsMap);
    }

    /**
     * 撤回消息
     * @param messageId Integer
     */
    public void deleteMsg(Integer messageId) {
        this.callApi(GocqActionEnum.DELETE_MSG, (HashMap<String, Object>) null);
    }

    /**
     * 标记消息为已读
     * @param messageId Integer
     */
    public void markMsgAsRead(Integer messageId) {
        this.callApi(GocqActionEnum.MARK_MSG_AS_READ, (HashMap<String, Object>) null);
    }

    /**
     * 获取转发消息
     * @param messageId Integer
     */
    public void getForwardMsg(Integer messageId) {
        String data = this.callGetApi(GocqActionEnum.GET_FORWARD_MSG, "message_id", messageId);
    }

    /**
     * 群组发送合并转发消息
     * @param groupId
     * @param nodes
     */
    public void sendGroupForwardMsg(Long groupId, List<ForwardMessage> nodes) {
        this.callApi(GocqActionEnum.SEND_GROUP_FORWARD_MSG,
                "group_id", groupId, "messages", nodes);
    }

    /**
     * 私聊发送合并转发消息
     * @param userId
     * @param nodes
     */
    public void sendPrivateForwardMsg(Long userId, List<ForwardMessage> nodes) {
        this.callApi(GocqActionEnum.SEND_PRIVATE_FORWARD_MSG,
                "user_id", userId, "messages", nodes);
    }

    /**
     * 根据事件场景, 来发送对应的消息
     *
     * @param event      event object
     * @param message    消息
     * @param autoEscape 是否以纯文本发送 true:以纯文本发送，不解析cq码
     */
    public void sendMessage(Event event, String message, boolean autoEscape) {
        JSONObject jsonObject = event.getJsonObject();
        Long groupId = jsonObject.getLong("group_id");
        if (groupId != null) {
            sendGroupMessage(groupId, message, autoEscape);
        } else {
            Long userId = jsonObject.getLong("user_id");
            if (userId != null) {
                sendPrivateMessage(userId, message, autoEscape);
            }
        }
    }

    /**
     * ws session send
     * @param text text
     */
    public void sessionSend(String text) {
        try {
            session.sendMessage(new TextMessage(text));
        } catch (Exception e) {
            log.error("发送消息发生异常,session:{},消息：{}", session, text, e);
        }
    }


}
