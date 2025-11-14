package com.amst.api.common.utls;

public class WxMpUtils {

    /**
     * 判断数据库操作是否成功
     * @param count 执行数据库操作影响行数
     * @return boolean
     */
    public static Boolean SqlHelper(Integer count) {
        return count > 0;
    }
}
