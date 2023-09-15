package tech.chowyijiu.huhubot.plugins;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import tech.chowyijiu.huhubot.core.annotation.BotPlugin;
import tech.chowyijiu.huhubot.core.annotation.MessageHandler;
import tech.chowyijiu.huhubot.core.annotation.NoticeHandler;
import tech.chowyijiu.huhubot.core.annotation.RuleCheck;
import tech.chowyijiu.huhubot.core.aop.rule.RuleEnum;
import tech.chowyijiu.huhubot.core.constant.SubTypeEnum;
import tech.chowyijiu.huhubot.core.entity.arr_message.MessageSegment;
import tech.chowyijiu.huhubot.core.entity.response.GroupInfo;
import tech.chowyijiu.huhubot.core.entity.response.GroupMember;
import tech.chowyijiu.huhubot.core.event.message.GroupMessageEvent;
import tech.chowyijiu.huhubot.core.event.notice.NotifyNoticeEvent;
import tech.chowyijiu.huhubot.core.ws.Bot;
import tech.chowyijiu.huhubot.core.ws.Huhubot;
import tech.chowyijiu.huhubot.utils.xiaoai.XiaoAIUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * @author elastic chow
 * @date 18/5/2023
 */
@Slf4j
@BotPlugin("huhubot-plugin-mygroup")
@SuppressWarnings("unused")
public class MyGroupPlugin {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Async
    @Scheduled(cron = "0 0/2 * * * * ")
    public void dateGroupCard() {
        String card = "失业第" + this.countdown();
        log.info("Time group nicknames start to be updated card: {}", card);
        Huhubot.getBots().forEach(bot -> Optional.ofNullable(bot.getGroups()).orElseGet(bot::getGroupList)
                .stream().map(GroupInfo::getGroupId).forEach(groupId -> {
                    bot.setGroupCard(groupId, bot.getSelfId(), card);
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException ignored) {
                    }
                }));
        log.info("Time group nickname set up card: {}", card);
    }

    private String countdown() {
        LocalDateTime fromTime = LocalDateTime.parse("2023-06-16 10:00", formatter);
        Duration duration = Duration.between(fromTime, LocalDateTime.now());
        long days = duration.toDays();
        duration = duration.minusDays(days);
        long hours = duration.toHours();
        duration = duration.minusHours(hours);
        long minutes = duration.toMinutes();
        return days + "天" + hours + "时" + minutes + "分";
    }

    private final List<Long> clockGroups = List.of(768887710L, 754044548L, 208248400L, 643396867L);

    @Scheduled(cron = "0 0 0 * * *")
    public void dailyClockIn() {
        log.info("开始群打卡");
        for (Bot bot : Huhubot.getBots()) {
            for (Long clockGroup : clockGroups) {
                bot.sendGroupSign(clockGroup);
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException ignored) {
                }
            }
        }
        log.info("群打卡完毕");
    }

    @RuleCheck(rule = RuleEnum.self_owner)
    @MessageHandler(name = "头衔自助", commands = {"sgst"})
    public void sgst(GroupMessageEvent event) {
        String title = event.getCommandArgs();
        for (String filter : new String[]{"群主", "管理", "主群"}) {
            if (title.contains(filter)) {
                title = "群猪";
                break;
            }
        }
        event.getBot().setGroupSpecialTitle(event.getGroupId(), event.getUserId(), title);
    }

    @NoticeHandler(name = "群内回戳", priority = 0)
    public void replyPoke(NotifyNoticeEvent event) {
        Bot bot = event.getBot();
        if (!SubTypeEnum.poke.name().equals(event.getSubType()) //不是戳一戳事件
                || !bot.getSelfId().equals(event.getTargetId()) //被戳的不是bot
                || bot.getSelfId().equals(event.getUserId())    //是bot号自己戳的
        ) return;
        if (event.getGroupId() != null) {
            event.getBot().sendGroupMessage(event.getGroupId(), MessageSegment.poke(event.getUserId()));
        }
    }

    @RuleCheck(rule = RuleEnum.tome)
    @MessageHandler(name = "被@, 让小爱通知我", keywords = {""}, priority = 9)
    public void atMeXiaoAiNotice(GroupMessageEvent event) {
        Bot bot = event.getBot();
        GroupMember groupMember = bot.getGroupMember(event.getGroupId(), event.getUserId(), true);
        GroupInfo groupInfo = bot.getGroupInfo(event.getGroupId(), false);
        XiaoAIUtil.tts("群" + groupInfo.getGroupName() + "内"
                + groupMember.getNickname() + "艾特你说, " + event.getMessage().getPlainText());
    }
}
