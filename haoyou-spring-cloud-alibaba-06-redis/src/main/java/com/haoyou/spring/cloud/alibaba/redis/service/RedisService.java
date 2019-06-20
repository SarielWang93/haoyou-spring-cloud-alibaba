package com.haoyou.spring.cloud.alibaba.redis.service;



import java.util.HashMap;

/**
 * redis存储信息
 */

public interface RedisService {


    boolean put(String key, String value);


    boolean putWithSeconds(String key, String value, long seconds);


    String get(String key);

    /**
     * 返回的是Map的json
     * @param lkkey
     * @return
     */

    HashMap<String,String> getlkMap(String lkkey);


    boolean delete(String key);
}
