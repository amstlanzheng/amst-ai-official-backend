package com.amst.api.model.dto.wxmpreplyrule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信回复内容DTO
 */
@Data
public class WxReplyContentDTO {

    /**
     * 内容类型
     */
    @Schema(description = "回复内容类型，默认为文字类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer contentType = 0;

    @Schema(description = "图文消息的id，可以调用获取图文消息接口获取")
    private String articleId;

    @Schema(description = "素材Id（图片、音频、视频等）")
    private String mediaId;

    @Schema(description = "文本消息内容")
    private String textContent;
}
