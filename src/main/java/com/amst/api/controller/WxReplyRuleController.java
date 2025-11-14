package com.amst.api.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.amst.api.common.enums.WxReplyRuleTypeEnum;
import com.amst.api.common.exception.BusinessException;
import com.amst.api.common.exception.ErrorCode;
import com.amst.api.common.exception.ThrowUtils;
import com.amst.api.common.response.BaseResponse;
import com.amst.api.common.response.ResultUtils;
import com.amst.api.model.dto.wxmpreplyrule.WxReplyRuleAddRequest;
import com.amst.api.model.dto.wxmpreplyrule.WxReplyRulePageQueryRequest;
import com.amst.api.model.dto.wxmpreplyrule.WxReplyRuleUpdateRequest;
import com.amst.api.model.vo.WxReplyRuleVO;
import com.amst.api.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.amst.api.model.entity.WxReplyRule;
import com.amst.api.service.WxReplyRuleService;

import java.util.List;
import java.util.Objects;

/**
 * 自动回复规则
 *
 * @author cq
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/wx/reply")
@Tag(name = "自动回复规则管理")
public class WxReplyRuleController {

    private final WxReplyRuleService wxReplyRuleService;

    private final UserService userService;

    /**
     * 新增
     */
    @PostMapping("/add")
    @Operation(summary = "新增回复规则")
    public BaseResponse<Long> addWxReplyRule(@Valid @RequestBody WxReplyRuleAddRequest wxReplyRuleAddRequest, HttpServletRequest request) {
        if (ObjectUtils.isEmpty(wxReplyRuleAddRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Integer replyType = wxReplyRuleAddRequest.getReplyType();

        // 当为菜单类型时
        if (Objects.equals(replyType, WxReplyRuleTypeEnum.EVENT.getValue())) {

            ThrowUtils.throwIf(StringUtils.isBlank(wxReplyRuleAddRequest.getEventKey()),
                    ErrorCode.PARAMS_ERROR, "菜单栏点击事件key不能为空");

            QueryWrapper wrapper = new QueryWrapper();

            wrapper.eq(WxReplyRule::getAppId, wxReplyRuleAddRequest.getAppId())
                   .eq(WxReplyRule::getEventKey, wxReplyRuleAddRequest.getEventKey());
            List<WxReplyRule> list = wxReplyRuleService.list(wrapper);
            ThrowUtils.throwIf(
                    !CollectionUtil.isEmpty(list),
                    ErrorCode.PARAMS_ERROR, "当前key值已被使用");
        }

        ThrowUtils.throwIf(Objects.equals(replyType, WxReplyRuleTypeEnum.KEYWORDS.getValue()) &&
                        ObjectUtils.isEmpty(wxReplyRuleAddRequest.getMatchValue()),
                ErrorCode.PARAMS_ERROR, "关键字不能为空");

        WxReplyRule wxReplyRule = wxReplyRuleAddRequest.toWxReplyRule();
        wxReplyRule.setUserId(userService.getLoginUser(request).getId());
        wxReplyRuleService.save(wxReplyRule);
        return ResultUtils.success(wxReplyRule.getId());

    }
    /**
     * 更新回复规则
     *
     * @param wxReplyRuleUpdateRequest 更新请求对象
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新回复规则")
    public BaseResponse<Boolean> updateWxReplyRule(@RequestBody WxReplyRuleUpdateRequest wxReplyRuleUpdateRequest) {
        if (ObjectUtils.isEmpty(wxReplyRuleUpdateRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否要修改菜单栏点击事件的key
        WxReplyRule wxReplyRuleDb = wxReplyRuleService.getById(wxReplyRuleUpdateRequest.getId());
        if (ObjectUtil.notEqual(wxReplyRuleDb.getEventKey(), wxReplyRuleUpdateRequest.getEventKey())) {

            // 判断是否已经被使用
            String appId = wxReplyRuleUpdateRequest.getAppId();

            ThrowUtils.throwIf(wxReplyRuleService.count(
                            QueryWrapper.create(WxReplyRule.class)
                                    .eq(WxReplyRule::getEventKey, wxReplyRuleUpdateRequest.getEventKey())
                                    .eq(WxReplyRule::getAppId, StringUtils.isBlank(appId) ? wxReplyRuleDb.getAppId() : appId)
                    ) > 0,
                    ErrorCode.PARAMS_ERROR,
                    String.format("当前key值已被使用，请更换其他key值，当前key值：%s", wxReplyRuleDb.getEventKey())
            );
        }

        return ResultUtils.success(wxReplyRuleService.updateById(wxReplyRuleUpdateRequest.toWxReplyRule()));
    }
    /**
     * 分页查询回复规则
     *
     * @param wxReplyRulePageQueryRequest 分页查询请求
     * @return 回复规则分页数据
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询回复规则")
    public BaseResponse<Page<WxReplyRuleVO>> listWxMpReplyRuleByPage(@RequestBody WxReplyRulePageQueryRequest wxReplyRulePageQueryRequest) {
        long current = wxReplyRulePageQueryRequest.getCurrent();
        long size = wxReplyRulePageQueryRequest.getPageSize();
        return ResultUtils.success(wxReplyRuleService.getPage(new Page<>(current, size),
                wxReplyRuleService.getQueryWrapper(wxReplyRulePageQueryRequest)));
    }

    /**
     * 根据ID查询回复规则详情
     *
     * @param id 规则ID
     * @return 回复规则详情
     */
    @GetMapping("/get/vo")
    @Operation(summary = "回复规则详情")
    public BaseResponse<WxReplyRuleVO> getWxMpReplyRuleVOById(@RequestParam Long id) {
        // 查询规则
        WxReplyRule wxReplyRule = wxReplyRuleService.getById(id);

        // 验证规则是否存在
        ThrowUtils.throwIf(ObjectUtils.isEmpty(wxReplyRule), ErrorCode.NOT_FOUND_ERROR);

        // 转换为 VO
        WxReplyRuleVO replyRuleVO = WxReplyRuleVO.obj2VO(wxReplyRule);

        // 添加创建者信息
        replyRuleVO.setUser(userService.getUserVO(userService.getById(wxReplyRule.getUserId())));
        return ResultUtils.success(replyRuleVO);
    }

}