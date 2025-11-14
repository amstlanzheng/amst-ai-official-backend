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
 * AI 回复内容记录 实体类。
 *
 * @author lanzhs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ai_reply_record")
public class AiReplyRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 接收到消息的公众号 appId
     */
    @Column("appId")
    private String appId;

    /**
     * 发送用户
     */
    @Column("fromUser")
    private String fromUser;

    /**
     * 用户发送消息
     */
    private String message;

    /**
     * AI 回复消息
     */
    @Column("replyMessage")
    private String replyMessage;

    /**
     * 回复状态，0-未回复、1-已回复
     */
    @Column("replyStatus")
    private Integer replyStatus;

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

}
