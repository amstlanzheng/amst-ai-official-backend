package com.amst.api.common.config;

import com.amst.api.handler.MessageHandler;
import jakarta.annotation.Resource;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信消息路由器配置
 */
@Configuration
public class WxMpMsgRouterConfig {

    @Resource
    private WxMpService wxMpService;

    @Resource
    private MessageHandler messageHandler;

    /**
     * 配置消息路由规则
     */
    @Bean
    public WxMpMessageRouter messageRouter() {
        WxMpMessageRouter router = new WxMpMessageRouter(wxMpService);
        // 注册文本消息处理规则
        router.rule()
                .async(false)  // 同步处理
                .msgType(WxConsts.XmlMsgType.TEXT)  // 处理文本类型消息
                .handler(messageHandler)  // 使用我们的消息处理器
                .end();  // 结束当前规则
        return router;
    }
}