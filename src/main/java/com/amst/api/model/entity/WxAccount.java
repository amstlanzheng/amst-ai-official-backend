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
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;

/**
 * 微信公众号账号 实体类。
 *
 * @author lanzhs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("wx_account")
public class WxAccount implements Serializable {

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
     * 公众号名称
     */
    private String name;

    /**
     * 认证状态
     */
    private Boolean verified;

    /**
     * appSecret
     */
    private String secret;

    /**
     * token
     */
    private String token;

    /**
     * aesKey
     */
    @Column("aesKey")
    private String aesKey;

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

    /**
     * 将公众号信息转换为WxJava的配置对象
     * @return WxMpDefaultConfigImpl 微信公众号配置对象
     */
    public WxMpDefaultConfigImpl toWxMpConfigStorage(){
        WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
        configStorage.setAppId(appId);
        configStorage.setSecret(secret);
        configStorage.setToken(token);
        configStorage.setAesKey(aesKey);
        return configStorage;
    }

}
