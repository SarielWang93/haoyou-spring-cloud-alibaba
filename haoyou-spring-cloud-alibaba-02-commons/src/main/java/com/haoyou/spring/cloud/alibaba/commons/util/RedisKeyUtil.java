package com.haoyou.spring.cloud.alibaba.commons.util;

import cn.hutool.core.text.StrBuilder;

/**
 * redisKey设计
 */
public class RedisKeyUtil {

    /**
     * redis的key
     *
     * @return
     */
    public static String getKey(String... strs){
        StrBuilder buffer = StrBuilder.create();
        for(String str : strs){
            buffer.append(str).append(":");
        }
        buffer.subSequence(0,buffer.length()-1);
        String s = buffer.toString();
        buffer.reset();
        return s;
    }

    /**
     * 获取模糊查询的key
     * @param st1
     * @return
     */
    public static String getlkKey(String st1){
        StrBuilder buffer = StrBuilder.create();
        buffer.append(st1).append(":");
        buffer.append("*");
        String s = buffer.toString();
        buffer.reset();
        return s;
    }


}
