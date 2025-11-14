package com.amst.api.controller;

import com.amst.api.common.exception.BusinessException;
import com.amst.api.common.exception.ErrorCode;
import com.amst.api.common.exception.ThrowUtils;
import com.amst.api.common.response.BaseResponse;
import com.amst.api.common.response.ResultUtils;
import com.amst.api.model.dto.WxAccountAddDTO;
import com.amst.api.model.dto.WxAccountPageQueryDTO;
import com.amst.api.model.dto.WxAccountUpdateDTO;
import com.amst.api.model.entity.User;
import com.amst.api.model.entity.WxAccount;
import com.amst.api.model.vo.WxAccountVO;
import com.amst.api.service.UserService;
import com.amst.api.service.WxAccountService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 微信公众号账号 控制层。
 *
 * @author lanzhs
 */
@RestController
@RequestMapping("/wxAccount")
@Tag(name = "公众号账号")
@RequiredArgsConstructor
public class WxAccountController {

    @Resource
    private WxAccountService wxAccountService;
    @Resource
    private UserService userService;
    @Resource
    private WxMpService wxMpService;

    /**
     * 添加新的公众号
     * @param wxAccountAddDTO 新增公众号信息
     * @param request HTTP请求
     * @return 公众号ID
     */
    @PostMapping("/add")
    @Operation(summary = "新增公众号")
    public BaseResponse<Long> addWxMpAccount(@RequestBody WxAccountAddDTO wxAccountAddDTO, HttpServletRequest request) {
        // 参数校验
        if (ObjectUtils.isEmpty(wxAccountAddDTO)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ThrowUtils.throwIf(StringUtils.isBlank(wxAccountAddDTO.getAppId()), ErrorCode.PARAMS_ERROR, "appId不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(wxAccountAddDTO.getName()), ErrorCode.PARAMS_ERROR, "公众号名称不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(wxAccountAddDTO.getSecret()), ErrorCode.PARAMS_ERROR, "秘钥不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(wxAccountAddDTO.getToken()), ErrorCode.PARAMS_ERROR, "token不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(wxAccountAddDTO.getAesKey()), ErrorCode.PARAMS_ERROR, "aesKey不能为空");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务保存数据并返回结果
        return ResultUtils.success(wxAccountService.saveAndToRuntime(wxAccountAddDTO, loginUser.getId()));
    }
    /**
     * 删除公众号
     * @param appIds 要删除的公众号appId列表
     * @return 删除结果
     */
    @PostMapping("/delete")
    @Operation(summary = "根据appId删除列表")
    public BaseResponse<Boolean> deleteWxMpAccount(@RequestBody List<String> appIds) {
        return ResultUtils.success(wxAccountService.deleteByAppIds(appIds));
    }
    /**
     * 更新公众号信息
     * @param wxAccountUpdateDTO 更新的公众号信息
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新公众号信息")
    public BaseResponse<Boolean> updateWxMpAccount(@RequestBody WxAccountUpdateDTO wxAccountUpdateDTO) {
        if (ObjectUtils.isEmpty(wxAccountUpdateDTO)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ThrowUtils.throwIf(ObjectUtils.isEmpty(wxAccountUpdateDTO.getId()), ErrorCode.PARAMS_ERROR, "id不能为空");

        return ResultUtils.success(wxAccountService.updateAndToRuntime(wxAccountUpdateDTO));
    }
    /**
     * 分页查询公众号
     * @param wxAccountPageQueryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询公众号")
    public BaseResponse<Page<WxAccountVO>> listWxMpAccountByPage(WxAccountPageQueryDTO wxAccountPageQueryDTO) {
        long current = wxAccountPageQueryDTO.getCurrent();
        long size = wxAccountPageQueryDTO.getPageSize();
        return ResultUtils.success(wxAccountService.getPage(new Page<>(current, size),
                wxAccountService.getQueryWrapper(wxAccountPageQueryDTO)));
    }
    @GetMapping("/access_token/get")
    @Operation(summary = "测试获取 access_token")
    public BaseResponse<String> getAccessToken(@RequestParam String appId) {
        try {
            wxMpService.switchover(appId);
            return ResultUtils.success(wxMpService.getAccessToken());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

}
