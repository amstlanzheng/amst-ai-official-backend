package com.amst.api.model.dto.wxmpreplyrule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信回复匹配值DTO
 */
@Data
public class WxReplyMatchValueDTO {
    /**
     * 匹配类型（全匹配还是半匹配）
     */
    @Schema(description = "匹配类型（全匹配还是半匹配）,默认为半匹配")
    private Integer matchType = 1;

    @Schema(description = "关键字")
    private String matchKeyWords;
}