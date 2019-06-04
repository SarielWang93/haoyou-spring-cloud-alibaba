package com.haoyou.spring.cloud.alibaba.manager.test;

import cn.hutool.core.lang.Console;

import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.RandomUtil;
import com.haoyou.spring.cloud.alibaba.commons.entity.HiFightingRoom;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.HiFightingRoomMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;

import com.haoyou.spring.cloud.alibaba.service.login.LoginService;
import com.haoyou.spring.cloud.alibaba.serialization.JsonSerializer;
import com.haoyou.spring.cloud.alibaba.service.redis.RedisService;
import com.haoyou.spring.cloud.alibaba.service.redis.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.service.sofabolt.SendMsgService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.commons.util.ZIP;
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
    SendMsgService sendMsgService;
    @Autowired
    UserMapper userMapper;
    @Autowired
    HiFightingRoomMapper hiFightingRoomMapper;
    @Autowired
    ScoreRankService scoreRankService;

    @Autowired
    protected LoginService loginService;

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
    @Test
    public void contextLoads4() throws Exception {




    }

    public static void main(String[] args) throws InterruptedException {

        for(int i = 10;i>=0;i--){
            WeightRandom.WeightObj<Integer>[] weightObjs = new WeightRandom.WeightObj[4];
            weightObjs[0] = new WeightRandom.WeightObj(1, i);
            weightObjs[1] = new WeightRandom.WeightObj(2, i);
            weightObjs[2] = new WeightRandom.WeightObj(3, i);
            weightObjs[3] = new WeightRandom.WeightObj(4, i);
            WeightRandom<Integer> weightRandom = RandomUtil.weightRandom(weightObjs);
            Console.log(weightRandom.next());
        }


    }



}
