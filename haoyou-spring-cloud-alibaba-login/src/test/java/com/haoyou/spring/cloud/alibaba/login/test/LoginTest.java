package com.haoyou.spring.cloud.alibaba.login.test;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/6/25 16:11
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "dev")
public class LoginTest {

    @Test
    public void contextLoads() throws Exception {


    }
}
