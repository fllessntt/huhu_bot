package tech.chowyijiu.huhubot.core.entity.arr_message;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author elastic chow
 * @date 25/7/2023
 */
@Getter
public class Message extends ArrayList<MessageSegment> {

    @JsonIgnore
    private String plainText;

    public boolean checkToMe(Long selfId) {
        for (MessageSegment segment : this) {
            if ("at".equals(segment.getType()) ) {
                String qq = segment.getString("qq");
                if ("all".equals(qq)) return true;
                else return Long.parseLong(qq) == selfId;
            }
        }
        return false;
    }

    public Message append(String text) {
        this.add(MessageSegment.text(text));
        return this;
    }

    public Message append(MessageSegment segment) {
        this.add(segment);
        return this;
    }

    public static Message text(String text) {
        return new Message().append(text);
    }

    /**
     * 初始化所有text信息, 去除所有cq码
     */
    public void plainText() {
        //if (this.plainText != null) return this.plainText;
        StringBuilder sb = new StringBuilder();
        for (MessageSegment segment : this) sb.append(segment.text());
        this.plainText = sb.toString();
    }

    public List<MessageSegment> getSegmentByType(String type) {
        return this.stream().filter(seg -> seg.getType().equals(type)).toList();
    }

    @Override
    public String toString() {
        //return this.plainText;
        return JSONObject.toJSONString(this);
    }
}
