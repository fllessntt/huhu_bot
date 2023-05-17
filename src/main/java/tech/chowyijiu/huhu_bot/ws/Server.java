package tech.chowyijiu.huhu_bot.ws;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tech.chowyijiu.huhu_bot.constant.EventTypeEnum;
import tech.chowyijiu.huhu_bot.constant.MetaTypeEnum;
import tech.chowyijiu.huhu_bot.constant.SubTypeEnum;
import tech.chowyijiu.huhu_bot.entity.gocq.event.Event;
import tech.chowyijiu.huhu_bot.entity.gocq.event.MetaEvent;
import tech.chowyijiu.huhu_bot.entity.gocq.response.MessageResp;
import tech.chowyijiu.huhu_bot.thread.ProcessEventTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author elastic chow
 * @date 13/5/2023
 */
@Slf4j
public class Server extends TextWebSocketHandler {

    private static final List<Bot> bots = new ArrayList<>();

    public static int getConnections() {
        return bots.size();
    }

    public static Bot getBot(Long userId) {
        for (Bot bot : bots) {
            if (Objects.equals(bot.getUserId(), userId)) {
                return bot;
            }
        }
        return null;
    }

    public static void addBot(Long userId, WebSocketSession session) {
        bots.add(new Bot(userId, session));
    }

    @Override
    public void afterConnectionEstablished(@NotNull final WebSocketSession session) throws Exception {
        log.info("[CLIENT] GOCQ CONNECT SUCCESS, Remote Address:{}，Client num：{}", session.getRemoteAddress(), getConnections());
    }


    @Override
    public void handleTextMessage(@NotNull final WebSocketSession session, final TextMessage message) throws Exception {
        final String json = message.getPayload();
        try {
            MessageResp messageResp = JSONObject.parseObject(json, MessageResp.class);
            Event event = Event.respToEvent(messageResp);
            if (Objects.equals(event.getClass().getSimpleName(), EventTypeEnum.MetaEvent.name())) {
                MetaEvent metaEvent = (MetaEvent) event;
                if (Objects.equals(metaEvent.getMetaEventType(), MetaTypeEnum.heartbeat.name())) {
                    //心跳忽略
                    //log.info("[{}] bot[{}] heartbeat ", this.getClass().getSimpleName(), metaEvent.getSelfId());
                    return;
                } else if (Objects.equals(metaEvent.getMetaEventType(), MetaTypeEnum.lifecycle.name())
                            && Objects.equals(metaEvent.getSubType(), SubTypeEnum.connect.name())) {
                    //刚连接成功时，gocq会发一条消息给bot, 添加bot对象到bots中
                    addBot(event.getSelfId(), session);
                    log.info("[{}] bot[{}] received gocq connection success message ", this.getClass().getSimpleName(), metaEvent.getSelfId());
                    return;
                }
            }
            for (Bot bot : bots) {
                if (bot.getSession() == session) {
                    ProcessEventTask.execute(bot, event, json);
                }
            }

        } catch (Exception e) {
            log.error("[{}] Parsing payload exception:{}", this.getClass().getSimpleName(), e);
        }
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[{}] connect exception sessionId: {} exception: {}", this.getClass().getSimpleName(), session.getId(), exception);
        removeClient(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("connect close, sessionId:{},{}", session.getId(), closeStatus.toString());
        removeClient(session);
    }

    private void removeClient(WebSocketSession session) {
        bots.removeIf(bot -> bot.getSession() == session);
    }

}