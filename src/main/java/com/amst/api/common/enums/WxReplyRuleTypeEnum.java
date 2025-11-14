package com.amst.api.common.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 回复规则类型枚举
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum WxReplyRuleTypeEnum {

    KEYWORDS("关键字回复", 0),
    DEFAULT("收到消息回复", 1),
    SUBSCRIBE("被关注回复", 2),
    EVENT("菜单栏点击事件回复", 3);

    @JsonProperty("label")
    private final String text;

    private final Integer value;

    WxReplyRuleTypeEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return {@link List}<{@link Integer}>
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 值
     * @return {@link WxReplyRuleTypeEnum}
     */
    public static WxReplyRuleTypeEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (WxReplyRuleTypeEnum anEnum : WxReplyRuleTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
