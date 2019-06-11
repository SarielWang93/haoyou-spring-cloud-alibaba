package com.haoyou.spring.cloud.alibaba.service.redis;



import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.RedisObjKV;

import java.util.List;

/**
 * redis存储信息
 */

public interface RedisObjectService {


    boolean save(RedisObjKV redisObjKV);


    boolean save(RedisObjKV redisObjKV,long timout);


    RedisObjKV get( String key);

    /**
     * 返回的是Map的json
     * @param lkkey
     * @return
     */

    List<RedisObjKV> getlkMap(String lkkey);


    boolean delete(String key);


    boolean refreshTime(String key);
}
