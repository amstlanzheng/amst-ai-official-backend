package com.amst.api.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.amst.api.common.enums.WxReplyContentTypeEnum;
import com.amst.api.common.enums.WxReplyMatchTypeEnum;
import com.amst.api.common.enums.WxReplyRuleTypeEnum;
import com.amst.api.common.exception.BusinessException;
import com.amst.api.common.exception.ErrorCode;
import com.amst.api.common.request.PageRequest;
import com.amst.api.common.utls.WrapperUtil;
import com.amst.api.model.dto.wxmpreplyrule.WxReplyContentDTO;
import com.amst.api.model.dto.wxmpreplyrule.WxReplyMatchValueDTO;
import com.amst.api.model.dto.wxmpreplyrule.WxReplyRulePageQueryRequest;
import com.amst.api.model.entity.User;
import com.amst.api.model.vo.WxReplyRuleVO;
import com.amst.api.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.amst.api.model.entity.WxReplyRule;
import com.amst.api.mapper.WxReplyRuleMapper;
import com.amst.api.service.WxReplyRuleService;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 微信公众号回复规则 服务层实现。
 *
 * @author lanzhs
 */
@Service
@RequiredArgsConstructor
public class WxReplyRuleServiceImpl extends ServiceImpl<WxReplyRuleMapper, WxReplyRule>  implements WxReplyRuleService{

    private final UserService userService;

    /**
     * 构建查询条件
     *
     * @param wxReplyRulePageQueryRequest 查询请求参数
     * @return 查询条件包装器
     */
    @Override
    public QueryWrapper getQueryWrapper(WxReplyRulePageQueryRequest wxReplyRulePageQueryRequest) {
        if (wxReplyRulePageQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String appId = wxReplyRulePageQueryRequest.getAppId();
        String ruleName = wxReplyRulePageQueryRequest.getRuleName();
        String matchValue = wxReplyRulePageQueryRequest.getMatchValue();
        String replyContent = wxReplyRulePageQueryRequest.getReplyContent();
        String eventKey = wxReplyRulePageQueryRequest.getEventKey();
        String ruleDescription = wxReplyRulePageQueryRequest.getRuleDescription();
        Integer replyType = wxReplyRulePageQueryRequest.getReplyType();
        String sortField = wxReplyRulePageQueryRequest.getSortField();
        String sortOrder = wxReplyRulePageQueryRequest.getSortOrder();
        List<PageRequest.Sorter> sorterList = wxReplyRulePageQueryRequest.getSorterList();

        // 构建查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("appId", appId, StringUtils.isNotBlank(appId));
        queryWrapper.eq( "replyType", replyType, ObjectUtils.isNotEmpty(replyType));
        queryWrapper.like("eventKey", eventKey, ObjectUtils.isNotEmpty(eventKey));
        queryWrapper.like("ruleName", ruleName,StringUtils.isNotBlank(ruleName));
        queryWrapper.like("matchValue", matchValue, StringUtils.isNotBlank(matchValue));
        queryWrapper.like("replyContent", replyContent, StringUtils.isNotBlank(replyContent));
        queryWrapper.like("ruleDescription", ruleDescription, StringUtils.isNotBlank(ruleDescription));

        // 处理排序
        WrapperUtil.handleOrder(queryWrapper, sorterList, sortField, sortOrder);
        return queryWrapper;
    }

    /**
     * 获取分页数据
     *
     * @param wxReplyRulePage 分页对象
     * @param queryWrapper 查询条件
     */
    @Override
    public Page<WxReplyRuleVO> getPage(Page<WxReplyRule> wxReplyRulePage, QueryWrapper queryWrapper) {
        Page<WxReplyRule> replyRulePage = this.page(wxReplyRulePage, queryWrapper);
        Page<WxReplyRuleVO> resultPage = new Page<>();
        List<WxReplyRule> wxReplyRuleList = replyRulePage.getRecords();


        Set<Long> userIdSet = wxReplyRuleList.stream().map(WxReplyRule::getUserId).collect(Collectors.toSet());
        Map<Long, User> userIdUserListMap;
        if (ObjectUtils.isNotEmpty(userIdSet)) {
            userIdUserListMap = userService.listByIds(userIdSet)
                    .stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
        } else {
            userIdUserListMap = new HashMap<>();
        }
        BeanUtils.copyProperties(replyRulePage, resultPage, "records");

        resultPage.setRecords(
                wxReplyRuleList
                        .stream()
                        .map(wxReplyRule -> {
                            WxReplyRuleVO replyRuleVO = WxReplyRuleVO.obj2VO(wxReplyRule);
                            User createUser = userIdUserListMap.get(wxReplyRule.getUserId());
                            if (ObjectUtils.isNotEmpty(createUser)) {
                                replyRuleVO.setUser(userService.getUserVO(createUser));
                            }
                            return replyRuleVO;
                        })
                        .collect(Collectors.toList())
        );
        return resultPage;
    }

    /**
     * 根据用户消息匹配回复内容
     *
     * @param appId 公众号appId
     * @param msg 用户发送的消息
     * @return 匹配到的回复内容，如果没有匹配到则返回null
     */
    @Override
    public WxReplyContentDTO receiveMessageReply(String appId, String msg) {
        // 1、将所有的关键词规则和默认规则都查询出来
        List<WxReplyRule> wxReplyRuleList = this.list(
                QueryWrapper.create()
                        .eq(WxReplyRule::getAppId, appId)
                        .in(
                                WxReplyRule::getReplyType,
                                WxReplyRuleTypeEnum.KEYWORDS.getValue(), WxReplyRuleTypeEnum.DEFAULT.getValue()
                        )
                        .orderBy(WxReplyRule::getUpdateTime, false)
        );

        // 2、将关键词规则都过滤出来
        WxReplyRule keyWordReply = wxReplyRuleList
                .stream()
                .filter(wxReplyRule -> {
                    // 过滤非关键词类型的规则
                    if (!WxReplyRuleTypeEnum.KEYWORDS.getValue().equals(wxReplyRule.getReplyType())) {
                        return false;
                    }
                    // 解析匹配值JSON为对象列表
                    List<WxReplyMatchValueDTO> keywords = JSONUtil.toList(wxReplyRule.getMatchValue(), WxReplyMatchValueDTO.class);
                    // 查看当前规则的关键字是否包含在用户发送的信息中
                    for (WxReplyMatchValueDTO keyword : keywords) {
                        String matchKeyWords = keyword.getMatchKeyWords();
                        Integer matchType = keyword.getMatchType();
                        if (StringUtils.isBlank(matchKeyWords)) {
                            continue;
                        }
                        // 全匹配：用户发送内容完全等于关键词（忽略大小写）
                        if (WxReplyMatchTypeEnum.FULL.getValue().equals(matchType) && matchKeyWords.equalsIgnoreCase(msg)) {
                            return true;
                        }
                        // 半匹配：用户发送内容包含关键词（忽略大小写）
                        if (WxReplyMatchTypeEnum.PART.getValue().equals(matchType) && msg.toLowerCase().contains(matchKeyWords.toLowerCase())) {
                            return true;
                        }
                    }
                    return false;
                })
                .findFirst()
                .orElse(null);

        // 3、如果没有匹配上的关键词，则返回默认回复内容
        if (ObjectUtils.isEmpty(keyWordReply)) {
            List<WxReplyRule> defaultList = wxReplyRuleList
                    .stream()
                    .filter(wxReplyRule -> WxReplyRuleTypeEnum.DEFAULT.getValue().equals(wxReplyRule.getReplyType()))
                    .collect(Collectors.toList());

            // 4、如果默认规则也没有，则返回null（由外层处理默认回复）
            if (ObjectUtils.isEmpty(defaultList)) {
                return null;
            }
            // 有多条默认规则时随机选择一条
            return JSONUtil.toBean(RandomUtil.randomEle(defaultList).getReplyContent(), WxReplyContentDTO.class);
        }

        // 5、解析并返回匹配到的回复内容
        return JSONUtil.toBean(keyWordReply.getReplyContent(), WxReplyContentDTO.class);
    }

    /**
     * 根据回复内容类型生成对应的微信回复消息
     *
     * @param wxMpXmlMessage 接收到的微信消息
     * @param replyContent 回复内容
     * @param contentTypeEnum 内容类型枚举
     * @return 生成的微信XML回复消息
     */
    @Override
    public WxMpXmlOutMessage replyByContentType(WxMpXmlMessage wxMpXmlMessage, WxReplyContentDTO replyContent, WxReplyContentTypeEnum contentTypeEnum) {
        return switch (contentTypeEnum) {
            case TEXT -> WxMpXmlOutMessage
                    .TEXT()
                    .content(replyContent.getTextContent())
                    .fromUser(wxMpXmlMessage.getToUser())
                    .toUser(wxMpXmlMessage.getFromUser())
                    .build();
            case IMAGE -> WxMpXmlOutMessage
                    .IMAGE()
                    .mediaId(replyContent.getMediaId())
                    .fromUser(wxMpXmlMessage.getToUser())
                    .toUser(wxMpXmlMessage.getFromUser())
                    .build();
            case VOICE -> WxMpXmlOutMessage
                    .VOICE()
                    .mediaId(replyContent.getMediaId())
                    .fromUser(wxMpXmlMessage.getToUser())
                    .toUser(wxMpXmlMessage.getFromUser())
                    .build();
            case VIDEO -> WxMpXmlOutMessage
                    .VIDEO()
                    .mediaId(replyContent.getMediaId())
                    .fromUser(wxMpXmlMessage.getToUser())
                    .toUser(wxMpXmlMessage.getFromUser())
                    .build();
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂未实现该类型的上传");
        };
    }

}
