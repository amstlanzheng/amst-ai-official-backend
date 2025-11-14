package com.amst.api;

import com.amst.api.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AmstBaseApiApplicationTests {

    @Test
    void contextLoads() {
        ErrorCode errorCode = ErrorCode.PARAMS_ERROR; // 变量类型是 ErrorCode
        String message = errorCode.getMessage(); // 这里应该可以正常调用
        System.out.println(message);
    }

}
