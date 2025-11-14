package com.amst.api.common.enums;

import lombok.Getter;

/**
 * AI 回复状态枚举
 */
@Getter
public enum WxAiReplyStatusEnum {

    NOT_REPLY(0, "未回复"),
    REPLIED(1, "已回复");

    private final Integer value;
    private final String desc;

    WxAiReplyStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    
    public static WxAiReplyStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (WxAiReplyStatusEnum statusEnum : WxAiReplyStatusEnum.values()) {
            if (statusEnum.value.equals(value)) {
                return statusEnum;
            }
        }
        return null;
    }
}
