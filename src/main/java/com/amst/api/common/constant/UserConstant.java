package com.amst.api.common.constant;

/**
 * @author lanzhs
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 密码加密的盐
     */
    String SALT = "Icarus";

    /**
     * 默认密码
     */
    String DEFAULT_PASSWORD = "12345678";

}
