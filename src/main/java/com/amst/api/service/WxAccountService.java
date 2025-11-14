package com.amst.api.service;

import com.amst.api.model.dto.WxAccountAddDTO;
import com.amst.api.model.dto.WxAccountPageQueryDTO;
import com.amst.api.model.dto.WxAccountUpdateDTO;
import com.amst.api.model.vo.WxAccountVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.amst.api.model.entity.WxAccount;

import java.util.List;

/**
 * 微信公众号账号 服务层。
 *
 * @author lanzhs
 */
public interface WxAccountService extends IService<WxAccount> {

    /**
     * 保存公众号信息并添加到运行时服务
     * @param wxAccountAddDTO 新增公众号的数据传输对象
     * @param userId 用户ID
     * @return 新增公众号的ID
     */
    Long saveAndToRuntime(WxAccountAddDTO wxAccountAddDTO, Long userId);

    /**
     * 根据appId列表删除公众号
     * @param appIds 公众号appId列表
     * @return 删除是否成功
     */
    Boolean deleteByAppIds(List<String> appIds);



    /**
     * 更新公众号信息并更新运行时配置
     * @param wxAccountUpdateDTO 更新的公众号信息
     * @return 更新是否成功
     */
    Boolean updateAndToRuntime(WxAccountUpdateDTO wxAccountUpdateDTO);




    /**
     * 分页查询公众号
     * @param wxAccountPage 分页对象
     * @param wxAccountQueryWrapper 查询条件
     * @return 分页结果
     */
    Page<WxAccountVO> getPage(Page<WxAccount> wxAccountPage, QueryWrapper wxAccountQueryWrapper);

    /**
     * 获取查询条件
     * @param wxAccountPageQueryDTO 查询条件
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(WxAccountPageQueryDTO wxAccountPageQueryDTO);

}
