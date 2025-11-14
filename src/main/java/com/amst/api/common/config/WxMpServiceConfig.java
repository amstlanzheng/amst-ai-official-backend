package com.amst.api.common.config;

import cn.hutool.core.util.ObjectUtil;
import com.amst.api.mapper.WxAccountMapper;
import com.amst.api.model.entity.WxAccount;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * WX MP 服务配置
 *
 * @author cq
 * @since 2023/11/21
 */
@Configuration
@RequiredArgsConstructor
public class WxMpServiceConfig {

    private final WxAccountMapper wxAccountMapper;

    @Bean
    public WxMpService wxMpService() {
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setMaxRetryTimes(3);
        // 将数据库中的数据放入到wxJava中
        List<WxAccount> wxAccountList = wxAccountMapper.selectAll();
        if (!ObjectUtil.isEmpty(wxAccountList)){
            wxAccountList.forEach(wxAccount -> wxMpService.addConfigStorage(wxAccount.getAppId(), wxAccount.toWxMpConfigStorage()));
        }

        return wxMpService;
    }
}