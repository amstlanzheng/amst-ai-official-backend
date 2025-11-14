package com.amst.api.model.dto.wx;


import com.amst.api.common.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;


@Data
@Schema(description = "微信素材查询请求")
public class WxMaterialQueryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3191241716373120793L;
    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;
    /**
     * 素材类型: image, voice, video, news
     */
    @Schema(description = "素材类型：image,voice,video,news", required = true)
    private String materialType;
}