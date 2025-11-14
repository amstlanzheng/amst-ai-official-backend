package com.amst.api.service.impl;

import com.amst.api.common.enums.WxAiReplyStatusEnum;
import com.amst.api.mapper.AiReplyRecordMapper;
import com.amst.api.model.entity.AiReplyRecord;
import com.amst.api.service.AiReplyRecordService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

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

    @Override
    public String aiReply(String appId, String fromUser, String message, AiReplyRecord aiReplyRecord) {
        // 使用 Java 21 虚拟线程执行 AI 调用，避免阻塞主线程
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

                long endTime = System.currentTimeMillis();
                log.info("AI 回复耗时:{}s", (endTime - startTime) / 1000.0);
                log.info("AI 回复内容：{}", content);
                return aiReplyRecord;
            } catch (Exception e) {
                log.error("AI 回复生成失败", e);
                return aiReplyRecord;
            }
        }, Executors.newVirtualThreadPerTaskExecutor());

        try {
            // 设置 3 秒超时，符合微信回调接口要求
            AiReplyRecord aiReplyResult = future.get(20, TimeUnit.SECONDS);

            // 成功获取回复内容后，更新回复状态为"已回复"
            this.updateChain()
                    .set(AiReplyRecord::getReplyStatus, WxAiReplyStatusEnum.REPLIED.getValue())
                    .eq(AiReplyRecord::getId, aiReplyResult.getId())
                    .update();

            return aiReplyResult.getReplyMessage();
        } catch (TimeoutException e) {
            // 超时但不取消任务，让后台线程继续完成保存操作
            log.warn("AI 回复超时，返回默认内容（后台任务仍在执行）");
            return null;
        } catch (InterruptedException | ExecutionException e) {
            // 处理其他异常
            log.error("AI 回复请求异常", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public String aiReply2(String appId, String fromUser, String message, AiReplyRecord aiReplyRecord) {
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

            long endTime = System.currentTimeMillis();
            log.info("AI 回复内容：{}", content);
            return aiReplyRecord.getReplyMessage();
        } catch (Exception e) {
            log.error("AI 回复生成失败", e);
            return aiReplyRecord.getReplyMessage();
        }
    }
}
