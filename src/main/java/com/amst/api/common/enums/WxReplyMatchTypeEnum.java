package com.amst.api.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 微信回复匹配类型枚举
 */
@Getter
public enum WxReplyMatchTypeEnum {

    FULL(0, "全匹配"),
    PART(1, "半匹配");

    private final Integer value;
    private final String desc;

    WxReplyMatchTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 值
     * @return {@link WxReplyMatchTypeEnum}
     */
    public static WxReplyMatchTypeEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (WxReplyMatchTypeEnum anEnum : WxReplyMatchTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
