package com.amst.api.model.entity;

import com.amst.api.model.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

import java.io.Serial;

import lombok.*;

/**
 * 用户 实体类。
 *
 * @author lanzhs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user")
@Schema(description = "用户实体")
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    @Schema(description = "用户账号")
    private String userAccount;

    /**
     * 密码
     */
    @Schema(description = "用户密码")
    private String userPassword;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String userName;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Schema(description = "用户简介")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Schema(description = "用户角色：user/admin")
    private String userRole;

    /**
     * 会员过期时间
     */
    @Schema(description = "会员过期时间")
    private LocalDateTime vipExpireTime;

    /**
     * 会员兑换码
     */
    @Schema(description = "会员兑换码")
    private String vipCode;

    /**
     * 会员编号
     */
    @Schema(description = "会员编号")
    private Long vipNumber;

    /**
     * 分享码
     */
    @Schema(description = "分享码")
    private String shareCode;

    /**
     * 邀请用户 id
     */
    @Schema(description = "邀请用户 id")
    private Long inviteUser;

    /**
     * 编辑时间
     */
    @Schema(description = "编辑时间")
    private LocalDateTime editTime;

    /**
     * 是否删除
     */
    @Column(isLogicDelete = true)
    @Schema(description = "是否删除")
    private Integer isDelete;

}