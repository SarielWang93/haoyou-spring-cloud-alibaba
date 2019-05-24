package com.haoyou.spring.cloud.alibaba.manager.test;

import cn.hutool.core.lang.Console;

import com.alibaba.fastjson.JSON;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;

import com.haoyou.spring.cloud.alibaba.service.login.LoginService;
import com.haoyou.spring.cloud.alibaba.serialization.JsonSerializer;
import com.haoyou.spring.cloud.alibaba.service.redis.RedisObjectService;
import com.haoyou.spring.cloud.alibaba.service.redis.RedisService;
import com.haoyou.spring.cloud.alibaba.service.redis.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.service.sofabolt.SendMsgService;
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
    RedisService redisService;
    @Autowired
    JsonSerializer jsonSerializer;
    @Autowired
    RedisObjectService redisObjectService;
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



    }



    @Test
    public void batchAdd() {
        List<User> users=new ArrayList<>();
        Map<String,Long> msgs=new HashMap<>();
        for (int i = 0; i < 100000; i++) {
            User  user = new User();
            user.setUid("张三" + i);
            user.setRank(1);
            users.add(user);

            msgs.put(user.getUid(),user.getRank().longValue());
        }
//        String num = scoreRankService.batchAdd(RedisKey.SCORE_RANK,msgs);
    }

    /**
     * 获取排行列表
     */
    @Test
    public void list() {
        List<String> range = scoreRankService.list(RedisKey.SCORE_RANK,0l,10l);
        System.out.println("获取到的排行列表:" + JSON.toJSONString(range));
    }
    /**
     * 单个新增
     */
    @Test
    public void add() {
        User  user = new User();
        user.setUid("李四");
        user.setRank(8899);
        scoreRankService.add(RedisKey.SCORE_RANK,user.getUid(),user.getRank().longValue());
    }

    /**
     * 获取单个的排行
     */
    @Test
    public void find(){

        Long rankNum = scoreRankService.find(RedisKey.SCORE_RANK,"李四");
        System.out.println("李四的个人排名：" + rankNum);

    }

    /**
     * 使用加法操作分数
     */
    @Test
    public void incrementScore(){
        long score = scoreRankService.incrementScore(RedisKey.SCORE_RANK,"李四",1000l);
        System.out.println("李四分数+1000后：" + score);
    }


}
