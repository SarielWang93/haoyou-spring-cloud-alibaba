package com.haoyou.spring.cloud.alibaba.redis.service;



import com.haoyou.spring.cloud.alibaba.redis.RedisObjKV;

import java.util.List;

/**
 * redis存储信息
 */

public interface RedisObjectService {


    boolean save(RedisObjKV redisObjKV);


    boolean save(RedisObjKV redisObjKV, long timout);


    RedisObjKV get(String key);

    /**
     * 返回的是Map的json
     * @param lkkey
     * @return
     */

    List<RedisObjKV> getlkMap(String lkkey);


    boolean delete(String key);


    boolean refreshTime(String key);
}
