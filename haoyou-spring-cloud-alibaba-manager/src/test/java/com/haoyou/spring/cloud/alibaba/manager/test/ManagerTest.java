package com.haoyou.spring.cloud.alibaba.manager.test;

import cn.hutool.core.lang.Console;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.HiFightingRoomMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.redis.service.RedisService;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.serialization.JsonSerializer;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ManagerTest {
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    RedisService redisService;
    @Autowired
    JsonSerializer jsonSerializer;
    @Autowired
    UserMapper userMapper;
    @Autowired
    HiFightingRoomMapper hiFightingRoomMapper;
    @Autowired
    ScoreRankService scoreRankService;


    @Test
    public void contextLoads() throws Exception {

    }
    @Test
    public void contextLoads2() throws Exception {
        HashMap<String, String> map = redisService.getlkMap("user:name:*");
        Console.log(map);
    }
    @Test
    public void contextLoads3() throws Exception {
        User user =new User();
        user.setUid("ec12ffde5b2447d6bbc758421ba9");

        user = userMapper.selectOne(user);

        //sendMsgService.sendMsgAll(SendType.MATCH_READY,user.toJson());
        Console.log(user);
    }


    public static void main(String[] args) throws InterruptedException {





    }



}
