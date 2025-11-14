package com.amst.api.common.enums;

import lombok.Getter;

/**
 * 微信素材类型枚举
 */
@Getter
public enum WxMaterialTypeEnum {

    IMAGE("image", "图片"),
    VOICE("voice", "音频"),
    VIDEO("video", "视频");

    private final String value;
    private final String desc;

    WxMaterialTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据value获取枚举
     */
    public static WxMaterialTypeEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (WxMaterialTypeEnum typeEnum : WxMaterialTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }
}