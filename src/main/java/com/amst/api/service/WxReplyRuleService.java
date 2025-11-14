package com.amst.api.service;

import com.amst.api.common.enums.WxReplyContentTypeEnum;
import com.amst.api.model.dto.wxmpreplyrule.WxReplyContentDTO;
import com.amst.api.model.dto.wxmpreplyrule.WxReplyRulePageQueryRequest;
import com.amst.api.model.vo.WxReplyRuleVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.amst.api.model.entity.WxReplyRule;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

/**
 * 微信公众号回复规则 服务层。
 *
 * @author lanzhs
 */
public interface WxReplyRuleService extends IService<WxReplyRule> {
    /**
     * 构建查询条件
     */
    QueryWrapper getQueryWrapper(WxReplyRulePageQueryRequest wxReplyRulePageQueryRequest);

    /**
     * 获取分页数据
     */
    Page<WxReplyRuleVO> getPage(Page<WxReplyRule> wxReplyRulePage, QueryWrapper queryWrapper);

    /**
     * 根据用户消息匹配回复内容
     *
     * @param appId 公众号appId
     * @param msg 用户发送的消息
     * @return 匹配到的回复内容，如果没有匹配到则返回null
     */
    WxReplyContentDTO receiveMessageReply(String appId, String msg);


    /**
     * 根据回复内容类型生成对应的微信回复消息
     *
     * @param wxMpXmlMessage 接收到的微信消息
     * @param replyContent 回复内容
     * @param contentTypeEnum 内容类型枚举
     * @return 生成的微信XML回复消息
     */
    WxMpXmlOutMessage replyByContentType(WxMpXmlMessage wxMpXmlMessage, WxReplyContentDTO replyContent, WxReplyContentTypeEnum contentTypeEnum);
}
