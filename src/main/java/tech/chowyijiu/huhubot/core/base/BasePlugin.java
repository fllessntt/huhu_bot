package tech.chowyijiu.huhubot.core.base;

import tech.chowyijiu.huhubot.core.DispatcherCore;
import tech.chowyijiu.huhubot.core.annotation.BotPlugin;
import tech.chowyijiu.huhubot.core.annotation.MessageHandler;
import tech.chowyijiu.huhubot.core.annotation.RuleCheck;
import tech.chowyijiu.huhubot.core.rule.RuleEnum;
import tech.chowyijiu.huhubot.core.event.message.MessageEvent;
import tech.chowyijiu.huhubot.utils.IocUtil;
import tech.chowyijiu.huhubot.utils.StringUtil;

/**
 * @author flless
 * @date 13/9/2023
 */
@BotPlugin("huhubot-plugin-base")
@SuppressWarnings("unused")
public class BasePlugin {


    @RuleCheck(rule = RuleEnum.superuser)
    @MessageHandler(name = "echo", commands = "echo")
    public void echo(MessageEvent event) {
        event.getBot().sendMessage(event, event.getCommandArgs());
    }


    @RuleCheck(rule = RuleEnum.superuser)
    @MessageHandler(name = "功能关闭", commands = "close")
    public void close(MessageEvent event) {
        DispatcherCore core = IocUtil.getBean(DispatcherCore.class);
        String arg = event.getCommandArgs();
        if (!StringUtil.isDigit(arg)) return;
        int no = Integer.parseInt(arg);
        event.getBot().sendMessage(event, "关闭" + arg + (core.logicClose(no) ? "成功" : "失败"));
    }

    @RuleCheck(rule = RuleEnum.superuser)
    @MessageHandler(name = "功能开启", commands = "open")
    public void open(MessageEvent event) {
        DispatcherCore core = IocUtil.getBean(DispatcherCore.class);
        String arg = event.getCommandArgs();
        if (!StringUtil.isDigit(arg)) return;
        int no = Integer.parseInt(arg);
        event.getBot().sendMessage(event, "开启" + arg + (core.logicOpen(no) ? "成功" : "失败"));
    }

    @RuleCheck(rule = RuleEnum.superuser)
    @MessageHandler(name = "功能集", commands = "list")
    public void list(MessageEvent event) {
        DispatcherCore core = IocUtil.getBean(DispatcherCore.class);
        String handlerNames = core.getHandlerNameList();
        event.getBot().sendMessage(event, handlerNames);
    }

}
