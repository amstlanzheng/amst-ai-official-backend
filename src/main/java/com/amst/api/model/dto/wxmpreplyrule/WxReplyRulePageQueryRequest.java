package com.amst.api.model.dto.wxmpreplyrule;

import com.amst.api.common.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 微信回复规则分页查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "微信回复规则分页查询请求")
public class WxReplyRulePageQueryRequest extends PageRequest {

    /**
     * appid
     */
    @Schema(description = "公众号appId")
    private String appId;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    private String ruleName;

    /**
     * 匹配值（关键字或者事件的key）
     */
    @Schema(description = "匹配值")
    private String matchValue;
    
    /**
     * 菜单栏点击事件的key
     */
    @Schema(description = "菜单栏点击事件的key")
    private String eventKey;

    /**
     * 回复内容
     */
    @Schema(description = "回复内容")
    private String replyContent;

    /**
     * 规则描述
     */
    @Schema(description = "规则描述")
    private String ruleDescription;

    /**
     * 0 为关键词触发、1 为默认触发、2 为被关注触发、3 为菜单点击事件类型
     */
    @Schema(description = "回复类型：0-关键词触发、1-默认触发、2-被关注触发、3-菜单点击事件类型")
    private Integer replyType;
}