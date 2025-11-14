package com.amst.api.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信公众号回复规则 实体类。
 *
 * @author lanzhs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("wx_reply_rule")
public class WxReplyRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * appid
     */
    @Column("appId")
    private String appId;

    /**
     * 规则名称
     */
    @Column("ruleName")
    private String ruleName;

    /**
     * 匹配值（关键字或者事件的key）
     */
    @Column("matchValue")
    private String matchValue;

    /**
     * 菜单栏点击事件的key
     */
    @Column("eventKey")
    private String eventKey;

    /**
     * 回复内容（json）
     */
    @Column("replyContent")
    private String replyContent;

    /**
     * 规则描述
     */
    @Column("ruleDescription")
    private String ruleDescription;

    /**
     * 0 为关键词触发、1 为默认触发、2 为被关注触发、3 为菜单点击事件类型
     */
    @Column("replyType")
    private Integer replyType;

    /**
     * 创建用户 id
     */
    @Column("userId")
    private Long userId;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
