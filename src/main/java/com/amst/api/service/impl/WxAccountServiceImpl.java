package com.amst.api.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.amst.api.common.exception.BusinessException;
import com.amst.api.common.exception.ErrorCode;
import com.amst.api.common.exception.ThrowUtils;
import com.amst.api.common.request.PageRequest;
import com.amst.api.common.utls.WrapperUtil;
import com.amst.api.common.utls.WxMpUtils;
import com.amst.api.model.dto.WxAccountAddDTO;
import com.amst.api.model.dto.WxAccountPageQueryDTO;
import com.amst.api.model.dto.WxAccountUpdateDTO;
import com.amst.api.model.entity.User;
import com.amst.api.model.vo.WxAccountVO;
import com.amst.api.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.amst.api.model.entity.WxAccount;
import com.amst.api.mapper.WxAccountMapper;
import com.amst.api.service.WxAccountService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 微信公众号账号 服务层实现。
 *
 * @author lanzhs
 */

@Slf4j
@Service
public class WxAccountServiceImpl extends ServiceImpl<WxAccountMapper, WxAccount>  implements WxAccountService{

    @Resource
    private WxMpService wxMpService;

    @Resource
    private UserService userService;
    /**
     * 将公众号配置添加到运行时服务中
     * @param wxAccount 公众号信息
     */
    private synchronized void addAccountToRuntime(WxAccount wxAccount) {
        String appId = wxAccount.getAppId();
        WxMpDefaultConfigImpl config = wxAccount.toWxMpConfigStorage();
        try {
            wxMpService.addConfigStorage(appId, config);
        } catch (NullPointerException e) {
            // 当 wxMpService 最开始没有公众号配置时可能会出现空指针异常
            log.info("初始化configStorageMap...");
            Map<String, WxMpConfigStorage> configStorages = new HashMap<>(6);
            configStorages.put(appId, config);
            wxMpService.setMultiConfigStorages(configStorages, appId);
        }
    }
    /**
     * 检查公众号配置是否已存在于运行时中
     * @param appid 公众号ID
     * @return 是否存在
     */
    private boolean isAccountInRuntime(String appid) {
        try {
            return wxMpService.switchover(appid);
        } catch (NullPointerException e) {
            // 未添加任何账号时configStorageMap为null会出错
            return false;
        }
    }
    /**
     * 保存公众号信息并添加到运行时服务
     * @param wxAccountAddDTO 新增公众号的数据传输对象
     * @param userId 用户ID
     * @return 新增公众号的ID
     */
    @Override
    public Long saveAndToRuntime(WxAccountAddDTO wxAccountAddDTO, Long userId) {
        // 检查公众号是否已存在于数据库
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq(WxAccount::getAppId, wxAccountAddDTO.getAppId());
        ThrowUtils.throwIf(
                this.count(wrapper) != 0,
                ErrorCode.PARAMS_ERROR,
                "公众号已存在"
        );

        String appId = wxAccountAddDTO.getAppId();
        // 使用字符串对象的intern方法获取字符串池中的对象，确保锁定的是同一个对象
        synchronized (appId.intern()) {
            // 检查公众号是否已存在于运行时
            if (this.isAccountInRuntime(appId)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "公众号已存在");
            }

            // 复制DTO到实体对象
            WxAccount wxAccount = BeanUtil.copyProperties(wxAccountAddDTO, WxAccount.class);
            wxAccount.setUserId(userId);

            // 保存到数据库
            this.save(wxAccount);

            // 添加到wxJava运行时
            this.addAccountToRuntime(wxAccount);
            return wxAccount.getId();
        }
    }
    /**
     * 根据appId列表删除公众号
     * @param appIds 公众号appId列表
     * @return 删除是否成功
     */
    @Override
    public Boolean deleteByAppIds(List<String> appIds) {
        // 先删除wxJava里的数据
        appIds.forEach(wxMpService::removeConfigStorage);

        // 再删除数据库里的数据
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.in(WxAccount::getAppId, appIds);
        return this.remove( wrapper);
    }

    /**
     * 更新公众号信息并更新运行时配置
     * @param wxAccountUpdateDTO 更新的公众号信息
     * @return 更新是否成功
     */
    @Override
    public Boolean updateAndToRuntime(WxAccountUpdateDTO wxAccountUpdateDTO) {
        // 获取数据库中的公众号信息
        WxAccount wxAccountDb = this.getById(wxAccountUpdateDTO.getId());
        ThrowUtils.throwIf(ObjectUtils.isEmpty(wxAccountDb), ErrorCode.PARAMS_ERROR, "公众号不存在");

        String oldAppId = wxAccountDb.getAppId();
        // 检查运行时中是否存在该公众号
        if (!this.isAccountInRuntime(oldAppId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公众号不存在");
        }

        // 如果 appId 为空，则使用原来的 appId
        if (StringUtils.isBlank(wxAccountUpdateDTO.getAppId())) {
            wxAccountUpdateDTO.setAppId(oldAppId);
        }

        // 复制更新数据到实体
        WxAccount wxAccount = BeanUtil.copyProperties(wxAccountUpdateDTO, WxAccount.class);

        // 更新数据库
        boolean result = this.updateById(wxAccount);

        // 先移除旧配置
        wxMpService.removeConfigStorage(oldAppId);
        // 删除后再添加到 wxJava（这里再查一遍目的是拿到最新的微信公众号信息，后面如果又加了字段，不这样做可能会漏掉字段）
        this.addAccountToRuntime(this.getById(wxAccountUpdateDTO.getId()));

        return result;
    }

    @Override
    public Page<WxAccountVO> getPage(Page<WxAccount> wxAccountPage, QueryWrapper wxAccountQueryWrapper) {
        Page<WxAccount> accountPage = this.page(wxAccountPage, wxAccountQueryWrapper);
        Page<WxAccountVO> pageResult = new Page<>();
        BeanUtils.copyProperties(accountPage, pageResult, "records");

        List<WxAccount> accountList = accountPage.getRecords();

        Set<Long> userIdSet = accountList.stream().map(WxAccount::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap;
        if (ObjectUtils.isNotEmpty(userIdSet)) {
            userIdUserListMap = userService.listByIds(userIdSet)
                    .stream()
                    .collect(Collectors.groupingBy(User::getId));
        } else {
            userIdUserListMap = new HashMap<>();
        }

        // 转换为VO
        pageResult.setRecords(
                accountList
                        .stream()
                        .map(wxAccount -> {
                            WxAccountVO wxAccountVO =  BeanUtil.copyProperties(wxAccount, WxAccountVO.class);
                            List<User> userList = userIdUserListMap.get(wxAccount.getUserId());
                            if (ObjectUtils.isNotEmpty(userList)) {
                                wxAccountVO.setUser(userService.getUserVO(userList.getFirst()));
                            }
                            return wxAccountVO;
                        })
                        .collect(Collectors.toList())
        );

        return pageResult;
    }

    @Override
    public QueryWrapper getQueryWrapper(WxAccountPageQueryDTO wxAccountPageQueryDTO) {
        if (wxAccountPageQueryDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String appId = wxAccountPageQueryDTO.getAppId();
        String name = wxAccountPageQueryDTO.getName();
        Boolean verified = wxAccountPageQueryDTO.getVerified();
        String secret = wxAccountPageQueryDTO.getSecret();
        String token = wxAccountPageQueryDTO.getToken();
        String aesKey = wxAccountPageQueryDTO.getAesKey();
        String sortField = wxAccountPageQueryDTO.getSortField();
        String sortOrder = wxAccountPageQueryDTO.getSortOrder();
        final List<PageRequest.Sorter> sorterList = wxAccountPageQueryDTO.getSorterList();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("appId", appId, StringUtils.isNotBlank(appId));
        queryWrapper.eq("verified", verified, ObjectUtils.isNotEmpty(verified));
        queryWrapper.like("name", name, StringUtils.isNotBlank(name));
        queryWrapper.like("secret", secret, StringUtils.isNotBlank(secret));
        queryWrapper.like("token", token, StringUtils.isNotBlank(token));
        queryWrapper.like("aesKey", aesKey, StringUtils.isNotBlank(aesKey));

        WrapperUtil.handleOrder(queryWrapper, sorterList, sortField, sortOrder);
        return queryWrapper;
    }


}
