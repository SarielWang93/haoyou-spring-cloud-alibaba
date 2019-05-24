package com.haoyou.spring.cloud.alibaba.redis.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.haoyou.spring.cloud.alibaba.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service(version = "${redis.service.version}")
public class RedisServiceImpl implements RedisService {
    @Autowired
    private StringRedisTemplate  redisTemplate;

    /**
     * 默认过期时长，单位：秒
     */
    public static final long DEFAULT_EXPIRE = 60 * 60 * 24;

    /**
     * 不设置过期时长
     */
    public static final long NOT_EXPIRE = -1;


    /**
     * 存入数据，默认过期时长
     * @param key
     * @param value
     * @return
     */
    @Override
    public boolean put(String key, String value) {
        this.putWithSeconds(key,value,DEFAULT_EXPIRE);
        return true;
    }

    /**
     * 存入数据并设置过期时长
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    @Override
    public boolean putWithSeconds(String key, String value, long seconds) {
        try {
            redisTemplate.opsForValue().set(key,value,seconds,TimeUnit.SECONDS);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    /**
     * 查询存储内容
     * @param key
     * @return
     */
    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 模糊查询
     * @param lkkey
     * @return
     */
    @Override
    public HashMap<String, String> getlkMap(String lkkey) {
        HashMap<String, String> map=new HashMap<>();
        Set<String> keys = redisTemplate.keys(lkkey);
        for(String key:keys){
            String value = redisTemplate.opsForValue().get(key);
            map.put(key,value);
        }
        return map;
    }

    @Override
    public boolean delete(String key) {
        return deleteKey(key);
    }

    public boolean existsKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 重名名key，如果newKey已经存在，则newKey的原值被覆盖
     *
     * @param oldKey
     * @param newKey
     */
    public void renameKey(String oldKey, String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    /**
     * newKey不存在时才重命名
     *
     * @param oldKey
     * @param newKey
     * @return 修改成功返回true
     */
    public boolean renameKeyNotExist(String oldKey, String newKey) {
        return redisTemplate.renameIfAbsent(oldKey, newKey);
    }

    /**
     * 删除key
     *
     * @param key
     */
    public boolean deleteKey(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 删除多个key
     *
     * @param keys
     */
    public void deleteKey(String... keys) {
        Set<String> kSet = Stream.of(keys).map(k -> k).collect(Collectors.toSet());
        redisTemplate.delete(kSet);
    }

    /**
     * 删除Key的集合
     *
     * @param keys
     */
    public void deleteKey(Collection<String> keys) {
        Set<String> kSet = keys.stream().map(k -> k).collect(Collectors.toSet());
        redisTemplate.delete(kSet);
    }

    /**
     * 设置key的生命周期
     *
     * @param key
     * @param time
     * @param timeUnit
     */
    public void expireKey(String key, long time, TimeUnit timeUnit) {
        redisTemplate.expire(key, time, timeUnit);
    }

    /**
     * 指定key在指定的日期过期
     *
     * @param key
     * @param date
     */
    public void expireKeyAt(String key, Date date) {
        redisTemplate.expireAt(key, date);
    }

    /**
     * 查询key的生命周期
     *
     * @param key
     * @param timeUnit
     * @return
     */
    public long getKeyExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 将key设置为永久有效
     *
     * @param key
     */
    public void persistKey(String key) {
        redisTemplate.persist(key);
    }


}