package com.amst.api.controller;

import com.amst.api.common.response.BaseResponse;
import com.amst.api.common.response.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lanzhs
 * @date 2025/10/15
 * @description 健康检查
 * @version 1.0
 */
@RestController
@RequestMapping("/")
@Tag(name = "健康检查", description = "系统健康检查接口")
public class MainController {

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查系统是否正常运行")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}