package tech.chowyijiu.huhu_bot.entity.gocq.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;
import tech.chowyijiu.huhu_bot.constant.CqTypeEnum;
import tech.chowyijiu.huhu_bot.entity.gocq.message.MessageSegment;
import tech.chowyijiu.huhu_bot.entity.gocq.response.MessageResp;

/**
 * @author elastic chow
 * @date 16/5/2023
 */
@Setter
@Getter
@ToString(callSuper = true)
@NoArgsConstructor
public class GroupMessageEvent extends MessageEvent {

    private Long groupId;
    private String anonymous;
    private boolean toMe;

    public GroupMessageEvent(MessageResp messageResp) {
        BeanUtils.copyProperties(messageResp, this);
        this.getSender().setGroupId(groupId);
        //todo 判断toMe, 勉强实现, 但是灰常烂
        MessageSegment.CqCode cqCode = MessageSegment.toCqCode(this.getMessage());
        if (cqCode != null && cqCode.getType().equals(CqTypeEnum.at)
                && cqCode.getParams().get("qq").equals(this.getSelfId().toString())) {
            toMe = true;
        }
    }

}
