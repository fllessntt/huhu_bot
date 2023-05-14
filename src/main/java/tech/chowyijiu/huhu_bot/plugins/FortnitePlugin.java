package tech.chowyijiu.huhu_bot.plugins;

import org.springframework.web.socket.WebSocketSession;
import tech.chowyijiu.huhu_bot.annotation.BotPlugin;
import tech.chowyijiu.huhu_bot.annotation.message.MessageHandler;
import tech.chowyijiu.huhu_bot.entity.gocq.response.Message;
import tech.chowyijiu.huhu_bot.ws.Server;

/**
 * @author elastic chow
 * @date 14/5/2023
 */
@BotPlugin(value = "堡垒之夜")
public class FortnitePlugin {

    @MessageHandler(name = "商城", command = {"商城", "shop"})
    public void shop(WebSocketSession session, Message message) {
        Server.sendGroupMessage(session, message.getGroupId(), "https://cdn.dingpanbao.cn/blzy/shop.png", false);
    }

    @MessageHandler(name = "堡垒之夜每日vb图", command = {"vb", "VB"})
    public void vb(WebSocketSession session, Message message) {
        Server.sendGroupMessage(session, message.getGroupId(), "vb图", false);
    }
}
