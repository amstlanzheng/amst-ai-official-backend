package com.amst.api.consumer;

import com.amst.api.common.config.RabbitMQConfig;
import com.amst.api.common.enums.WxAiReplyStatusEnum;
import com.amst.api.common.exception.BusinessException;
import com.amst.api.common.exception.ErrorCode;
import com.amst.api.model.entity.AiReplyRecord;
import com.amst.api.model.msg.ReplyEvent;
import com.amst.api.service.AiReplyRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消息消费者
 * @author lanzhs
 * @date 2023/9/27 16:05
 * @description: 用来延迟向用户发送消息，但是因为个人开发者限制，无法主动向用户发送消息
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class MessageConsumerService {
    private final WxMpService wxService;
    private final AiReplyRecordService aiReplyRecordService;
    /**
     * 消息消费者：本意是想延迟向用户发送消息，但是因为个人开发者限制，导致无法实现延迟发送消息，就先只能这样
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrderMessage(ReplyEvent replyEvent) {
        try {
            log.info("接收到消息: {}", replyEvent);
            // 处理订单业务逻辑
            dealUserOrder(replyEvent);



            log.info("消息处理失败: {}", replyEvent);
        } catch (Exception e) {
            log.error("消息处理失败: {}", replyEvent, e);
            throw new AmqpRejectAndDontRequeueException("消息处理失败");
        }
    }

    private void dealUserOrder(ReplyEvent msg) {

            WxMpKefuMessage message = new WxMpKefuMessage();
            message.setMsgType(WxConsts.KefuMsgType.TEXT);
            message.setToUser(msg.getUserId());
            message.setContent(msg.getContent());

            boolean result = false;
            try {
                result = this.wxService.getKefuService().sendKefuMessage(message);
            } catch (WxErrorException e) {
                log.error("发送消息失败: {}", message, e);
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            // 找到了完整的回复记录，更新状态并返回AI 回复
            aiReplyRecordService.updateChain()
                    .set(AiReplyRecord::getReplyStatus, WxAiReplyStatusEnum.REPLIED.getValue())
                    .eq(AiReplyRecord::getId, msg.getReplyId())
                    .update();

    }


}