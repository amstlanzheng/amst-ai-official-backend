package com.amst.api.controller;

import cn.hutool.core.io.FileUtil;
import com.amst.api.common.enums.WxMaterialTypeEnum;
import com.amst.api.common.exception.BusinessException;
import com.amst.api.common.exception.ErrorCode;
import com.amst.api.common.exception.ThrowUtils;
import com.amst.api.common.response.BaseResponse;
import com.amst.api.common.response.ResultUtils;
import com.amst.api.model.dto.wx.WxMaterialQueryRequest;
import com.amst.api.model.entity.User;
import com.amst.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.material.WxMpMaterial;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialFileBatchGetResult;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialVideoInfoResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 微信素材接口
 */
@RestController
@RequestMapping("/wx/material")
@Tag(name = "微信素材接口")
@RequiredArgsConstructor
@Slf4j
public class WxMaterialController {

    private final WxMpService wxMpService;
    private final UserService userService;

    /**
     * 允许上传的图片类型
     */
    private static final List<String> ALLOW_IMG_TYPE = Arrays.asList("bmp", "png", "jpeg", "jpg", "gif");

    /**
     * 允许上传的音频类型
     */
    private static final List<String> ALLOW_VOICE_TYPE = Arrays.asList("mp3", "wma", "wav", "amr");

    /**
     * 允许上传的视频类型
     */
    private static final List<String> ALLOW_VIDEO_TYPE = Collections.singletonList("mp4");

    /**
     * 验证上传文件的合法性
     * 
     * @param multipartFile 上传的文件
     * @param wxMaterialTypeEnum 素材类型
     */
    private void validFile(MultipartFile multipartFile, WxMaterialTypeEnum wxMaterialTypeEnum) {
        String originalFilename = multipartFile.getOriginalFilename();
        long fileSize = multipartFile.getSize();
        String fileSuffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(StringUtils.isBlank(fileSuffix), ErrorCode.PARAMS_ERROR, "未知的文件类型");
        fileSuffix = fileSuffix.toLowerCase();
        final long oneM = 1024 * 1024L;
        switch (wxMaterialTypeEnum) {
            case IMAGE:
                ThrowUtils.throwIf(!ALLOW_IMG_TYPE.contains(fileSuffix), ErrorCode.PARAMS_ERROR);
                ThrowUtils.throwIf(fileSize > 10 * oneM, ErrorCode.PARAMS_ERROR, "图片过大");
                break;
            case VOICE:
                ThrowUtils.throwIf(!ALLOW_VOICE_TYPE.contains(fileSuffix), ErrorCode.PARAMS_ERROR);
                ThrowUtils.throwIf(fileSize > 2 * oneM, ErrorCode.PARAMS_ERROR, "音频过大");
                break;
            case VIDEO:
                ThrowUtils.throwIf(!ALLOW_VIDEO_TYPE.contains(fileSuffix), ErrorCode.PARAMS_ERROR);
                ThrowUtils.throwIf(fileSize > 10 * oneM, ErrorCode.PARAMS_ERROR, "视频过大");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂未实现该类型的上传");
        }
    }

    /**
     * 上传素材（图片、音频、视频）
     *
     * @param appId 公众号AppID
     * @param multipartFile 上传的文件
     * @param materialType 素材类型
     * @param request HTTP请求
     * @return 上传结果
     */
    @PostMapping("/{appId}/upload")
    @Operation(summary = "上传素材（图片、音频、视频）")
    public BaseResponse<Boolean> uploadMaterial(@PathVariable String appId,
                                                @RequestPart("file") MultipartFile multipartFile,
                                                String materialType,
                                                HttpServletRequest request){
        // 将前端传来的素材类型字符串转换为枚举类型
        WxMaterialTypeEnum wxMaterialTypeEnum = WxMaterialTypeEnum.getEnumByValue(materialType);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(wxMaterialTypeEnum), ErrorCode.PARAMS_ERROR);

        // 校验文件
        this.validFile(multipartFile, wxMaterialTypeEnum);

        String originalFilename = multipartFile.getOriginalFilename();
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 创建临时文件存储路径
        String filepath = String.format("%s/%s/%s/%s", System.getProperty("user.dir"), loginUser.getId(), UUID.randomUUID(), originalFilename);
        File file = null;
        try {
            // 将上传的文件保存到临时目录
            file = new File(filepath);
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);

            // 创建微信素材对象
            WxMpMaterial wxMpMaterial = new WxMpMaterial();
            wxMpMaterial.setName(originalFilename);
            wxMpMaterial.setFile(file);
            wxMpMaterial.setVideoTitle(originalFilename);

            // 切换到指定公众号并调用素材上传接口
            wxMpService.switchover(appId);
            wxMpService.getMaterialService().materialFileUpload(materialType, wxMpMaterial);
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("file upload error, filepath = {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                FileUtil.del(file);
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }
    /**
     * 下载图片或音频素材
     *
     * @param appId 公众号AppID
     * @param materialId 素材ID
     * @param fileName 文件名
     * @return 文件流
     */
    @GetMapping("/{appId}/img_voice/download")
    @Operation(summary = "下载图片或音频")
    public ResponseEntity<InputStreamResource> downloadImgAndVoiceMaterial(@PathVariable String appId,
                                                                           String materialId,
                                                                           String fileName) throws WxErrorException {
        ThrowUtils.throwIf(StringUtils.isAnyBlank(materialId, fileName), ErrorCode.PARAMS_ERROR);
        // 切换到指定公众号
        wxMpService.switchover(appId);

        try (InputStream inputStream = wxMpService.getMaterialService().materialImageOrVoiceDownload(materialId)) {
            // 创建输入流资源
            InputStreamResource resource = new InputStreamResource(inputStream);
            // 构建HTTP响应，设置文件下载的头信息
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + UriUtils.encode(fileName, StandardCharsets.UTF_8) + "\"")
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 获取视频素材信息
     *
     * @param appId 公众号AppID
     * @param materialId 素材ID
     * @return 视频信息，包含下载链接
     */
    @GetMapping("/{appId}/video/info")
    @Operation(summary = "查询视频详情")
    public BaseResponse<WxMpMaterialVideoInfoResult> getMaterialVideoByMaterialId(@PathVariable String appId,
                                                                                  @RequestParam String materialId) throws WxErrorException {
        ThrowUtils.throwIf(StringUtils.isBlank(materialId), ErrorCode.PARAMS_ERROR);
        // 切换到指定公众号
        wxMpService.switchover(appId);
        // 返回视频信息，包含下载链接
        return ResultUtils.success(wxMpService.getMaterialService().materialVideoInfo(materialId));
    }
    /**
     * 查询素材列表
     *
     * @param appId 公众号AppID
     * @param wxMaterialQueryRequest 查询参数
     * @return 素材列表
     */
    @PostMapping("/{appId}/list")
    @Operation(summary = "查询素材列表")
    public BaseResponse<WxMpMaterialFileBatchGetResult> listAllMaterial(@PathVariable String appId,
                                                                        @RequestBody WxMaterialQueryRequest wxMaterialQueryRequest) throws WxErrorException {
        String materialType = wxMaterialQueryRequest.getMaterialType();
        ThrowUtils.throwIf(StringUtils.isBlank(materialType), ErrorCode.PARAMS_ERROR);
        int current = (int) wxMaterialQueryRequest.getCurrent();
        int pageSize = (int) wxMaterialQueryRequest.getPageSize();
        // 计算偏移量 (当前页 - 1) * 每页数据量
        int offset = (current - 1) * pageSize;

        // 切换到指定公众号
        wxMpService.switchover(appId);
        // 调用WxJava的素材批量获取接口
        return ResultUtils.success(wxMpService.getMaterialService().materialFileBatchGet(materialType, offset, pageSize));
    }
    /**
     * 删除素材
     *
     * @param appId 公众号AppID
     * @param materialId 素材ID
     * @return 删除结果
     */
    @DeleteMapping("/{appId}/delete")
    @Operation(summary = "删除素材")
    public BaseResponse<Boolean> deleteMaterial(@PathVariable String appId,
                                                @RequestParam String materialId) throws WxErrorException {
        ThrowUtils.throwIf(StringUtils.isBlank(materialId), ErrorCode.PARAMS_ERROR);

        // 切换到指定公众号
        wxMpService.switchover(appId);
        // 调用WxJava的素材删除接口
        return ResultUtils.success(wxMpService.getMaterialService().materialDelete(materialId));
    }

}