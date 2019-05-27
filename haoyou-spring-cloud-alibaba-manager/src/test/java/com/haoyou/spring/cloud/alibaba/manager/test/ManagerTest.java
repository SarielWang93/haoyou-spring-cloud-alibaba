package com.haoyou.spring.cloud.alibaba.manager.test;

import cn.hutool.core.lang.Console;

import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;

import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.SkillBoard;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape.Cell;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape.Tetromino;
import com.haoyou.spring.cloud.alibaba.service.login.LoginService;
import com.haoyou.spring.cloud.alibaba.serialization.JsonSerializer;
import com.haoyou.spring.cloud.alibaba.service.redis.RedisObjectService;
import com.haoyou.spring.cloud.alibaba.service.redis.RedisService;
import com.haoyou.spring.cloud.alibaba.service.redis.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.service.sofabolt.SendMsgService;
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
    SendMsgService sendMsgService;
    @Autowired
    UserMapper userMapper;
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
        Integer a = 3, b = 3, c = 128, d = 128,e = new Integer(1),f = new Integer(1);

        System.out.println(a == b);
        System.out.println(c == d);
        System.out.println(e == f);
        System.out.println(e+0 == f);

    }



}
