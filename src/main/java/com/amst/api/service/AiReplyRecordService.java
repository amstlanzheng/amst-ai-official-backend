package com.amst.api.service;


import com.amst.api.model.entity.AiReplyRecord;
import com.mybatisflex.core.service.IService;

/**
 * @author lanzhs
 */
public interface AiReplyRecordService extends IService<AiReplyRecord> {

    /**
     * 调用AI生成回复内容
     *
     * @param appId 公众号appId
     * @param fromUser 用户openId
     * @param message 用户消息
     * @param aiReplyRecord 回复记录对象
     * @return AI生成的回复内容，超时则返回null
     */
    String aiReply(String appId, String fromUser, String message, AiReplyRecord aiReplyRecord);

    String aiReply2(String appId, String fromUser, String message, AiReplyRecord aiReplyRecord);

}