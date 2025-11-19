package com.amst.api.common.utls;

import com.amst.api.common.constant.ReplyConstant;

public class RedisKeyUtil {
  
    public static String getUserKey(String userId) {
        return ReplyConstant.REPLY_STATUS_WAITING + userId;
    }  
  

}
