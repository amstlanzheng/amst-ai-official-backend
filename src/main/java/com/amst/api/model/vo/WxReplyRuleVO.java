package com.amst.api.model.vo;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.amst.api.model.dto.wxmpreplyrule.WxReplyContentDTO;
import com.amst.api.model.dto.wxmpreplyrule.WxReplyMatchValueDTO;
import com.amst.api.model.entity.WxReplyRule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 微信公众号回复规则 VO
 */
@Data
public class WxReplyRuleVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * appid
     */
    private String appId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 匹配值（关键字或者事件的key）
     */
    @Schema(name = "匹配值（关键字或者事件的key）", description = "这里返回的是列表，前端根据情况展示")
    private List<WxReplyMatchValueDTO> matchValue;

    /**
     * 回复内容
     */
    private WxReplyContentDTO replyContent;

    /**
     * 规则描述
     */
    private String ruleDescription;

    /**
     * 0 为关键词触发、1 为默认触发、2 为被关注触发、3 为菜单点击事件类型
     */
    private Integer replyType;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建人信息
     */
    private UserVO user;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 将实体对象转换为视图对象
     * 
     * @param wxReplyRule 回复规则实体
     * @return 回复规则视图对象
     */
    public static WxReplyRuleVO obj2VO(WxReplyRule wxReplyRule) {
        if (wxReplyRule == null) {
            return null;
        }
        WxReplyRuleVO wxReplyRuleVO = BeanUtil.copyProperties(wxReplyRule, WxReplyRuleVO.class, "matchValue", "replyContent");
        if (StringUtils.isNotBlank(wxReplyRule.getMatchValue())) {
            wxReplyRuleVO.setMatchValue(JSONUtil.toList(wxReplyRule.getMatchValue(), WxReplyMatchValueDTO.class));
        }
        if (StringUtils.isNotBlank(wxReplyRule.getReplyContent())) {
            wxReplyRuleVO.setReplyContent(JSONUtil.toBean(wxReplyRule.getReplyContent(), WxReplyContentDTO.class));
        }
        return wxReplyRuleVO;
    }
}
