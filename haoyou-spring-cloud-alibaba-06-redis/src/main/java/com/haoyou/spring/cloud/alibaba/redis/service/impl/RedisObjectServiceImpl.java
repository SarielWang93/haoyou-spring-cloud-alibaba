package com.haoyou.spring.cloud.alibaba.redis.service.impl;


import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.redis.RedisObjKV;
import com.haoyou.spring.cloud.alibaba.redis.service.RedisObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisObjectServiceImpl implements RedisObjectService {
    private final static Logger logger = LoggerFactory.getLogger(RedisObjectServiceImpl.class);


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
    public static final long DEFAULT_EXPIRE = 60 * 60 * 24 * 3;

    /**
     * 不设置过期时长
     */
    public static final long NOT_EXPIRE = -1;


    @Override
    public boolean save(RedisObjKV redisObjKV) {
        logger.debug(String.format("save  %s", redisObjKV.getKey()));
        redisTemplate.opsForValue().set(redisObjKV.getKey(), redisObjKV.getVal(), DEFAULT_EXPIRE, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean save(RedisObjKV redisObjKV, long timeout) {
        logger.debug(String.format("save  %s:%s", redisObjKV.getKey(), timeout));
        if (timeout == -1) {
            redisTemplate.opsForValue().set(redisObjKV.getKey(), redisObjKV.getVal());
            redisTemplate.persist(redisObjKV.getKey());
        } else {
            redisTemplate.opsForValue().set(redisObjKV.getKey(), redisObjKV.getVal(), timeout, TimeUnit.SECONDS);
        }
        return true;
    }

    public RedisObjKV get(String key) {
        logger.debug(String.format("get %s", key));
        return new RedisObjKV(key, (byte[]) redisTemplate.opsForValue().get(key));
    }

    @Override
    public boolean delete(String key) {
        logger.debug(String.format("delete %s", key));
        return redisTemplate.delete(key);
    }

    @Override
    public List<RedisObjKV> getlkMap(String lkkey) {
        if (!lkkey.contains(RedisKey.MATCH_PLAYER_POOL)) {
            logger.debug(String.format("getlkMap %s", lkkey));
        }
        List<RedisObjKV> list = new ArrayList<>();

        Set<String> keys = redisTemplate.keys(lkkey);
        for (String key : keys) {
            byte[] value = (byte[]) redisTemplate.opsForValue().get(key);
            list.add(new RedisObjKV(key, value));
        }
        return list;
    }

    @Override
    public boolean refreshTime(String key) {
        return redisTemplate.expire(key, DEFAULT_EXPIRE, TimeUnit.SECONDS);
    }

}
