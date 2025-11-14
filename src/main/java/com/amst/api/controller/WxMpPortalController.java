package com.amst.api.controller;

import com.amst.api.common.exception.ErrorCode;
import com.amst.api.common.exception.ThrowUtils;
import com.amst.api.model.entity.WxAccount;
import com.amst.api.service.WxAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 微信消息接口
 * 用于处理来自微信服务器的请求
 * @author lanzhs
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/wx/msg/{appId}")
@Tag(name = "微信消息 - 供微信调用")
@Slf4j
public class WxMpPortalController {

    private final WxMpService wxService;
    private final WxAccountService wxAccountService;
    private final WxMpMessageRouter messageRouter;
    /**
     * 处理微信服务器认证请求
     * 
     * @param appId 公众号AppID，从路径中获取
     * @param signature 微信签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param echoStr 微信回显字符串
     * @return 认证成功时返回echoStr，认证失败时返回错误信息
     */
    @GetMapping(produces = "text/plain;charset=utf-8")
    @Operation(summary = "微信服务器的认证消息", description = "公众号接入开发模式时微信调用此接口")
    public String wxServiceAuth(@PathVariable String appId,
                          @RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echoStr) {

        log.info("接收到来自微信服务器的认证消息：[{}, {}, {}, {}]", signature,
                timestamp, nonce, echoStr);

        // 验证请求参数完整性
        ThrowUtils.throwIf(StringUtils.isAnyBlank(signature, timestamp, nonce, echoStr), ErrorCode.PARAMS_ERROR);

        // 切换到对应公众号服务
        this.wxService.switchoverTo(appId);
        
        // 验证签名是否来自微信
        if (wxService.checkSignature(timestamp, nonce, signature)) {
            wxAccountService.updateChain().set(WxAccount::getVerified, true).eq(WxAccount::getAppId, appId).update();
            // 验证成功，返回微信要求的回显字符串
            return echoStr;
        }

        return "非法请求";
    }

    /**
     * 处理微信服务器发来的各类消息
     */
    @PostMapping(produces = "application/xml; charset=UTF-8")
    @Operation(summary = "微信各类消息", description = "公众号接入开发模式后才有效")
    public String post(@PathVariable String appId,
                       @RequestBody String requestBody,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        // 切换到对应公众号的配置
        this.wxService.switchoverTo(appId);

        // 验证请求签名
        ThrowUtils.throwIf(!wxService.checkSignature(timestamp, nonce, signature),
                ErrorCode.PARAMS_ERROR, "非法请求，可能属于伪造的请求！");

        String out = null;
        if (encType == null) {
            // 明文传输的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
            WxMpXmlOutMessage outMessage = this.route(appId, inMessage);
            if (outMessage == null) {
                return "";
            }

            out = outMessage.toXml();
        } else if ("aes".equalsIgnoreCase(encType)) {
            // aes加密的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody, wxService.getWxMpConfigStorage(),
                    timestamp, nonce, msgSignature);
            log.info("消息解密后内容为：\n{} ", inMessage.toString());
            WxMpXmlOutMessage outMessage = this.route(appId, inMessage);
            if (outMessage == null) {
                return "";
            }

            out = outMessage.toEncryptedXml(wxService.getWxMpConfigStorage());
        }

        log.info("组装回复信息：{}", out);
        return out;
    }

    /**
     * 路由消息到对应的处理器
     */
    private WxMpXmlOutMessage route(String appid, WxMpXmlMessage message) {
        try {
            return messageRouter.route(appid, message);
        } catch (Exception e) {
            log.error("路由消息时出现异常！", e);
        }
        return null;
    }
}