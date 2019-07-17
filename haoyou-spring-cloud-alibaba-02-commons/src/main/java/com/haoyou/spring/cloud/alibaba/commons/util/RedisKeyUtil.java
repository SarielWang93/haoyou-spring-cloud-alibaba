package com.haoyou.spring.cloud.alibaba.commons.util;

import cn.hutool.core.text.StrBuilder;

/**
 * redisKey设计
 */
public class RedisKeyUtil {

    /**
     * redis的key
     * @param strs
     * @return
     */
    public static String getKey(String... strs){
        StrBuilder buffer = StrBuilder.create();
        for(String str : strs){
            buffer.append(str).append(":");
        }

        String s = buffer.subString(0,buffer.length()-1);
        buffer.reset();
        return s;
    }

    /**
     * 获取模糊查询的key
     * @param strs
     * @return
     */
    public static String getlkKey(String... strs){
        StrBuilder buffer = StrBuilder.create(getKey(strs));
        buffer.append("*");
        String s = buffer.toString();
        buffer.reset();
        return s;
    }


}
