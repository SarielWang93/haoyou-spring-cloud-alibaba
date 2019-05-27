package com.haoyou.spring.cloud.alibaba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author: wanghui
 * @Date: 2019/5/13 11:29
 * @Version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@MapperScan(basePackages = "com.haoyou.spring.cloud.alibaba.commons.mapper")
public class CultivateApplication {
    public static void main(String[] args) {
        SpringApplication.run(CultivateApplication.class, args);
    }
}
