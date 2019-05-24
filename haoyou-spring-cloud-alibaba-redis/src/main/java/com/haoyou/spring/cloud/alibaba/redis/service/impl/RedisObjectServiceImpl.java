package com.haoyou.spring.cloud.alibaba.redis.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.RedisObjKV;
import com.haoyou.spring.cloud.alibaba.service.redis.RedisObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service(version = "${redis-object.service.version}")
public class RedisObjectServiceImpl implements RedisObjectService {



    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    private void init() {
        /**
         * 初始化序列化器，key用String，value直接传入Hessen2的byte数组不需要序列化器
         */
        redisTemplate.setKeySerializer(StringRedisSerializer.UTF_8);
        redisTemplate.setHashKeySerializer(StringRedisSerializer.UTF_8);
        redisTemplate.setValueSerializer(null);
        redisTemplate.setHashValueSerializer(null);
    }


    /**
     * 默认过期时长，单位：秒
     */
    public static final long DEFAULT_EXPIRE = 60 * 60 * 24;

    /**
     * 不设置过期时长
     */
    public static final long NOT_EXPIRE = -1;



    @Override
    public boolean save(RedisObjKV redisObjKV) {

        redisTemplate.opsForValue().set(redisObjKV.getKey(),redisObjKV.getVal(),DEFAULT_EXPIRE, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean save(RedisObjKV redisObjKV,long timeout) {

        if(timeout==-1){
            redisTemplate.opsForValue().set(redisObjKV.getKey(),redisObjKV.getVal());
            redisTemplate.persist(redisObjKV.getKey());
        }else{
            redisTemplate.opsForValue().set(redisObjKV.getKey(),redisObjKV.getVal(),timeout,TimeUnit.SECONDS);
        }
        return true;
    }

    public RedisObjKV get(String key) {

        return new RedisObjKV(key,(byte[]) redisTemplate.opsForValue().get(key));
    }

    @Override
    public boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public List<RedisObjKV> getlkMap(String lkkey) {

        List<RedisObjKV> list = new ArrayList<>();

        Set<String> keys = redisTemplate.keys(lkkey);
        for(String key:keys){
            byte[] value = (byte[]) redisTemplate.opsForValue().get(key);
            list.add(new RedisObjKV(key,value));
        }
        return list;
    }


}
