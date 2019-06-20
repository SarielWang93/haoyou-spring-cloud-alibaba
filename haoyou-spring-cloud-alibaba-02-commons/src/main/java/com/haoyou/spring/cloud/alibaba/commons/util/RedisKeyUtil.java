package com.haoyou.spring.cloud.alibaba.commons.util;

import cn.hutool.core.text.StrBuilder;

/**
 * redisKey设计
 */
public class RedisKeyUtil {

    /**
     * redis的key
     * 形式为：
     * 表名:主键名:主键值
     *
     * @param st1 表名
     * @param st2 主键值
     * @return
     */
    public static String getKey(String st1,String st2){
        StrBuilder buffer = StrBuilder.create();
        buffer.append(st1).append(":");
        buffer.append(st2);
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