package com.haoyou.spring.cloud.alibaba.redis.test;

import cn.hutool.core.lang.Console;
import com.haoyou.spring.cloud.alibaba.commons.entity.HiFightingRoom;
import com.haoyou.spring.cloud.alibaba.commons.mapper.HiFightingRoomMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;

import com.haoyou.spring.cloud.alibaba.commons.util.ZIP;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingRoom;
import com.haoyou.spring.cloud.alibaba.serialization.JsonSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "dev")
public class RedisTest {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    HiFightingRoomMapper hiFightingRoomMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String SCORE_RANK = "score_rank";


    @PostConstruct
    private void init() {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(null);
        redisTemplate.setHashValueSerializer(null);
    }


//    @Before
//    public void  before() throws Exception {
//        List<User> users = userMapper.selectAll();
//        for(User user:users){
//            String key = RedisKeyUtil.getKey("user", "uid", user.getUid());
//            String userjson = MapperUtils.obj2jsonIgnoreNull(user);
//            redisTemplate.opsForValue().set(key,userjson);
//        }
//    }

    @Test
    public void test1(){



    }

    @Test
    public void test2(){

    }




}
