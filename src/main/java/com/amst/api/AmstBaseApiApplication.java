package com.amst.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author lanzhs
 */
@SpringBootApplication
@MapperScan(basePackages = "com.amst.api.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class AmstBaseApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmstBaseApiApplication.class, args);
    }

}
