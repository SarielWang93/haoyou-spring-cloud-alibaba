package com.haoyou.spring.cloud.alibaba.redis.test;

import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;

import com.haoyou.spring.cloud.alibaba.action.RedisObjectUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;



@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisObjectUtil redisObjectUtil;


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
