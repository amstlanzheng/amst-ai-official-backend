package com.amst.api.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 微信回复内容类型枚举
 */
@Getter
public enum WxReplyContentTypeEnum {

    TEXT(0, "文字"),
    IMAGE(1, "图片"),
    VOICE(2, "语音"),
    VIDEO(3, "视频"),
    NEWS(4, "图文");

    private final Integer value;
    private final String desc;

    WxReplyContentTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 值
     * @return {@link WxReplyContentTypeEnum}
     */
    public static WxReplyContentTypeEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (WxReplyContentTypeEnum anEnum : WxReplyContentTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
