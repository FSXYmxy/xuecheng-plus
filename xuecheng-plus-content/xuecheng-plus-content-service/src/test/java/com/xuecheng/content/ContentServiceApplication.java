package com.xuecheng.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/4/6 13:53
 */

@EnableFeignClients(basePackages = "com.xuecheng.content.feignclient")
@SpringBootTest
public class ContentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentServiceApplication.class);
    }
}
