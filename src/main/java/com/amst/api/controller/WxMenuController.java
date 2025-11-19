package com.amst.api.controller;

import com.amst.api.common.response.BaseResponse;
import com.amst.api.common.response.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.menu.WxMpMenu;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 微信菜单管理接口
 */
@RestController
@RequestMapping("/wx/menu")
@Tag(name = "微信公众号菜单管理")
@RequiredArgsConstructor
public class WxMenuController {

    private final WxMpService wxMpService;

    /**
     * 创建或更新菜单
     * 
     * @param appId 公众号appId
     * @param wxMenuButtonList 菜单按钮列表
     * @return 操作结果
     * @throws WxErrorException 微信API异常
     */
    @PostMapping("/update/{appId}")
    @Operation(summary = "更新菜单", description = "替换当前公众号的全部菜单配置")
    public BaseResponse<Boolean> updateMenu(@PathVariable String appId,
                                            @RequestBody List<WxMenuButton> wxMenuButtonList) throws WxErrorException {
        // 切换到指定公众号
        wxMpService.switchoverTo(appId);
        
        // 构建菜单对象
        WxMenu wxMenu = new WxMenu();
        wxMenu.setButtons(wxMenuButtonList);
        
        // 调用接口创建菜单
        wxMpService.getMenuService().menuCreate(wxMenu);
        return ResultUtils.success(true);
    }

    /**
     * 删除公众号全部菜单
     * 
     * @param appId 公众号appId
     * @return 操作结果
     * @throws WxErrorException 微信API异常
     */
    @PostMapping("/delete/{appId}")
    @Operation(summary = "删除菜单", description = "删除该公众号的全部菜单设置")
    public BaseResponse<Boolean> deleteMenu(@PathVariable String appId) throws WxErrorException {
        // 切换到指定公众号
        wxMpService.switchoverTo(appId);
        
        // 调用接口删除菜单
        wxMpService.getMenuService().menuDelete();
        return ResultUtils.success(true);
    }

    /**
     * 获取公众号菜单配置
     * 
     * @param appId 公众号appId
     * @return 菜单配置信息
     * @throws WxErrorException 微信API异常
     */
    @GetMapping("/get/{appId}")
    @Operation(summary = "获取公众号菜单", description = "查询当前配置的菜单信息")
    public BaseResponse<WxMpMenu> getMenu(@PathVariable String appId) throws WxErrorException {
        // 切换到指定公众号
        wxMpService.switchoverTo(appId);
        
        // 调用接口获取菜单
        return ResultUtils.success(wxMpService.getMenuService().menuGet());
    }
}
