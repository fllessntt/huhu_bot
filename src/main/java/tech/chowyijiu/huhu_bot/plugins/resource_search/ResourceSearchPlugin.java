package tech.chowyijiu.huhu_bot.plugins.resource_search;

import org.springframework.scheduling.annotation.Scheduled;
import tech.chowyijiu.huhu_bot.annotation.BotPlugin;
import tech.chowyijiu.huhu_bot.annotation.MessageHandler;
import tech.chowyijiu.huhu_bot.config.BotConfig;
import tech.chowyijiu.huhu_bot.core.rule.RuleEnum;
import tech.chowyijiu.huhu_bot.event.message.MessageEvent;
import tech.chowyijiu.huhu_bot.plugins.resource_search.gitcafe.GitCafeReq;
import tech.chowyijiu.huhu_bot.plugins.resource_search.hdhive.HdhiveReq;
import tech.chowyijiu.huhu_bot.utils.StringUtil;
import tech.chowyijiu.huhu_bot.ws.Bot;
import tech.chowyijiu.huhu_bot.ws.Server;

import java.util.Objects;


/**
 * @author elastic chow
 * @date 17/7/2023
 */
@BotPlugin
public class ResourceSearchPlugin {


    @Scheduled(cron = "0 0 12 * * *")
    public void scheduledCheck() {
        Objects.requireNonNull(Server.getBot(BotConfig.superUsers.get(0)))
                .sendGroupMessage(BotConfig.testGroup,
                        "阿里云盘签到结果: " + AliYunDriver.dailySignIn(), false);
    }

    @MessageHandler(name = "阿里云盘资源搜索 gitcafe ", commands = {".s"})
    public void search1(Bot bot, MessageEvent event) {
        String gitcafe = StringUtil.hasLengthReturn(event.getCommandArgs(), GitCafeReq::get);
        bot.sendMessage(event, gitcafe, false);
    }

    @MessageHandler(name = "阿里云盘资源搜索 hdhive", commands = {".ds"})
    public void search2(Bot bot, MessageEvent event) {
        String data = StringUtil.hasLengthReturn(event.getCommandArgs(), HdhiveReq::get1);
        bot.sendMessage(event, data, false);
    }

    @MessageHandler(name = "openwrt aliyundrive invalidate cache",
            keywords = {".ic", "删除缓存"}, rule = RuleEnum.superuser)
    public void _1(Bot bot, MessageEvent event) {
        boolean ok = OpenwrtReq.invalidateCache();
        bot.sendMessage(event, ok ? "删除Openwrt阿里云盘缓存成功" : "删除Openwrt阿里云盘缓存失败", false);
    }
}
