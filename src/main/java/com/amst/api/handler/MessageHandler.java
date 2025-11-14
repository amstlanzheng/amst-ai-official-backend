package com.amst.api.handler;

import cn.hutool.crypto.digest.DigestUtil;
import com.amst.api.common.enums.WxAiReplyStatusEnum;
import com.amst.api.common.enums.WxReplyContentTypeEnum;
import com.amst.api.model.dto.wxmpreplyrule.WxReplyContentDTO;
import com.amst.api.model.entity.AiReplyRecord;
import com.amst.api.service.AiReplyRecordService;
import com.amst.api.service.WxReplyRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
import me.chanjar.weixin.mp.util.WxMpConfigStorageHolder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消息处理器
 * @author lanzhs
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler implements WxMpMessageHandler {
    private final WxReplyRuleService wxReplyRuleService;
    private final AiReplyRecordService aiReplyRecordService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, Map<String, Object> map,
                                    WxMpService wxMpService, WxSessionManager wxSessionManager) {
        // 获取当前公众号的appId
        String appId = WxMpConfigStorageHolder.get();
        String userMessage = wxMpXmlMessage.getContent();
        String fromUser = wxMpXmlMessage.getFromUser();

        // 定义默认的延迟回复消息
        WxMpXmlOutTextMessage defaultReplyMessage = WxMpXmlOutMessage.TEXT()
                .content(String.format("正在思考中，请 10 秒后再次发送原问题：%s", userMessage))
                .fromUser(wxMpXmlMessage.getToUser())
                .toUser(fromUser)
                .build();


        // 使用同步锁，避免用户短时间内重复发送相同消息导致重复AI调用
        // 锁的key由公众号ID+用户ID+消息内容的MD5值组成，确保唯一性
        synchronized ((appId + fromUser + DigestUtil.md5Hex(userMessage)).intern()) {
            // 首先尝试匹配预设的回复规则
            WxReplyContentDTO replyContent = wxReplyRuleService.receiveMessageReply(appId, userMessage);

            // 如果没有匹配到规则，进入AI 回复流程
            if (ObjectUtils.isEmpty(replyContent)) {
                // 检查数据库中是否已存在该问题的回复记录
                AiReplyRecord replyRecord = aiReplyRecordService.queryChain()
                        .eq(AiReplyRecord::getFromUser, fromUser)
                        .eq(AiReplyRecord::getAppId, appId)
                        .eq(AiReplyRecord::getMessage, userMessage)
                        .eq(AiReplyRecord::getReplyStatus, WxAiReplyStatusEnum.NOT_REPLY.getValue())
                        .one();

                // 如果不存在回复记录，创建新记录并调用AI生成回复
                if (ObjectUtils.isEmpty(replyRecord)) {
                    AiReplyRecord aiReplyRecord = new AiReplyRecord();
                    aiReplyRecord.setAppId(appId);
                    aiReplyRecord.setFromUser(fromUser);
                    aiReplyRecord.setMessage(userMessage);
                    aiReplyRecordService.save(aiReplyRecord);

                    // 调用AI生成回复
                    String content = aiReplyRecordService.aiReply(appId, fromUser, userMessage, aiReplyRecord);

                    // 如果AI未能在超时前回复，返回默认延迟消息
                    if (StringUtils.isBlank(content)) {
                        return defaultReplyMessage;
                    }
                    WxMpXmlOutTextMessage build = WxMpXmlOutMessage.TEXT().content(content)
                            .fromUser(wxMpXmlMessage.getToUser())
                            .toUser(fromUser)
                            .build();
                    log.info("AI生成回复成功，返回内容：{}", build.getContent());
                    // AI成功回复，直接返回内容
                    return build;
                }

                // 找到了之前的记录，但回复内容为空（AI尚未完成生成）
                if (ObjectUtils.isEmpty(replyRecord.getReplyMessage())) {
                    return defaultReplyMessage;
                }

                // 找到了完整的回复记录，更新状态并返回AI 回复
                aiReplyRecordService.updateChain()
                        .set(AiReplyRecord::getReplyStatus, WxAiReplyStatusEnum.REPLIED.getValue())
                        .eq(AiReplyRecord::getId, replyRecord.getId())
                        .update();

                return WxMpXmlOutMessage.TEXT().content(replyRecord.getReplyMessage())
                        .fromUser(wxMpXmlMessage.getToUser())
                        .toUser(fromUser)
                        .build();
            }

            // 如果匹配到了预设回复规则
            WxReplyContentTypeEnum contentTypeEnum = WxReplyContentTypeEnum.getEnumByValue(replyContent.getContentType());

            // 内容类型无效，返回通用错误消息
            if (ObjectUtils.isEmpty(contentTypeEnum)) {
                return WxMpXmlOutMessage.TEXT()
                        .content("抱歉，我暂时无法理解您的问题。您可以尝试问其他问题，或者提供更多详细信息。")
                        .fromUser(wxMpXmlMessage.getToUser())
                        .toUser(fromUser)
                        .build();
            }

            // 根据匹配到的规则和内容类型生成回复
            return wxReplyRuleService.replyByContentType(wxMpXmlMessage, replyContent, contentTypeEnum);
        }
    }
}