package com.amst.api.service.impl;

import com.amst.api.common.config.RabbitMQConfig;
import com.amst.api.common.enums.WxAiReplyStatusEnum;
import com.amst.api.mapper.AiReplyRecordMapper;
import com.amst.api.model.entity.AiReplyRecord;
import com.amst.api.model.msg.ReplyEvent;
import com.amst.api.service.AiReplyRecordService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI 回复内容记录 服务层实现。
 *
 * @author lanzhs
 */

@Service
@Slf4j
public class AiReplyRecordServiceImpl extends ServiceImpl<AiReplyRecordMapper, AiReplyRecord>
        implements AiReplyRecordService {

    @Resource
    private ChatClient chatClient;
    @Resource
    private RabbitTemplate rabbitTemplate;


    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${wx.is-service:false}")
    private Boolean isService;

    @Override
    public String aiReply(String appId, String fromUser, String message, AiReplyRecord aiReplyRecord) {
        // 使用 Java 21 虚拟线程执行 AI 调用，避免阻塞主线程
        log.info("开始调用AI生成回复");
        AtomicReference<String> contentTemp = new AtomicReference<>();
        CompletableFuture<AiReplyRecord> future = CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                // 调用 SpringAI 接口生成回复
                final String content = chatClient.prompt()
                        .user(message)
                        .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, aiReplyRecord.getId()))
                        .call()
                        .content();

                // 获取 AI 回复文本
                aiReplyRecord.setReplyMessage(content);

                // 更新数据库中的回复内容
                this.updateChain()
                        .eq(AiReplyRecord::getId, aiReplyRecord.getId())
                        .set(AiReplyRecord::getReplyMessage, content)
                        .update();



                log.info("AI 回复内容：{}", content);
                contentTemp.set(content);
                return aiReplyRecord;
            } catch (Exception e) {
                log.error("AI 回复生成失败", e);
                return aiReplyRecord;
            }
        }, Executors.newVirtualThreadPerTaskExecutor());

        try {
            // 设置 3 秒超时，符合微信回调接口要求
            AiReplyRecord aiReplyResult = future.get(3, TimeUnit.SECONDS);

            // 成功获取回复内容后，更新回复状态为"已回复"
            this.updateChain()
                    .set(AiReplyRecord::getReplyStatus, WxAiReplyStatusEnum.REPLIED.getValue())
                    .eq(AiReplyRecord::getId, aiReplyResult.getId())
                    .update();

            return aiReplyResult.getReplyMessage();
        } catch (TimeoutException e) {
            // 超时但不取消任务，让后台线程继续完成保存操作
            log.warn("AI 回复超时，返回默认内容（后台任务仍在执行）");
            // 存入消息中间件
            // 个人开发者限制暂时弃用
            if (isService){
                ReplyEvent replyEvent = ReplyEvent.builder()
                        .userId(fromUser)
                        .content(contentTemp.get())
                        .replyId(aiReplyRecord.getId())
                        .eventTime(LocalDateTime.now())
                        .build();
                sendDelayedMessage(replyEvent, 3000);
            }

            return null;
        } catch (InterruptedException | ExecutionException e) {
            // 处理其他异常
            log.error("AI 回复请求异常", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * 发送延迟消息
     */
    public void sendDelayedMessage(ReplyEvent replyEvent, int delayMillis) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_ROUTING_KEY,
                    replyEvent,
                    new CorrelationData(UUID.randomUUID().toString())
            );
            log.info("订单消息发送成功: {}", replyEvent);
        } catch (Exception e) {
            log.error("订单消息发送失败: {}", replyEvent, e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

}
