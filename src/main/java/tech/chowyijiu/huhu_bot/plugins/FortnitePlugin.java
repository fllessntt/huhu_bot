package tech.chowyijiu.huhu_bot.plugins;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;
import tech.chowyijiu.huhu_bot.annotation.BotPlugin;
import tech.chowyijiu.huhu_bot.annotation.message.MessageHandler;
import tech.chowyijiu.huhu_bot.constant.MessageTypeEnum;
import tech.chowyijiu.huhu_bot.entity.gocq.response.Message;
import tech.chowyijiu.huhu_bot.utils.MessageSegment;
import tech.chowyijiu.huhu_bot.ws.Server;

import java.util.Objects;

/**
 * @author elastic chow
 * @date 14/5/2023
 */
@Slf4j
@BotPlugin(name = "堡垒之夜")
public class FortnitePlugin {

    @MessageHandler(name = "商城", command = {"商城", "shop"})
    public void shop(WebSocketSession session, Message message) {
        String cq = MessageSegment.image("https://cdn.dingpanbao.cn/blzy/shop.png");
        if (Objects.equals(message.getMessageType(), MessageTypeEnum.private_.getType())) {
            Server.sendPrivateMessage(session, message.getSender().getUserId(), cq ,false);
        } else if (Objects.equals(message.getMessageType(), MessageTypeEnum.group.getType())) {
            Server.sendGroupMessage(session, message.getGroupId(), cq, false);
        }

    }

    @MessageHandler(type = MessageTypeEnum.group, name = "堡垒之夜每日vb图", command = {"vb", "VB"})
    public void vb(WebSocketSession session, Message message) {
        Server.sendGroupMessage(session, message.getGroupId(), "vb图", true);
    }
}
