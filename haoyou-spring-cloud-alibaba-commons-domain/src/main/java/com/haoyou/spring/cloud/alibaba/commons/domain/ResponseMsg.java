package com.haoyou.spring.cloud.alibaba.commons.domain;

public class ResponseMsg {
    //错误
    final static public Integer MSG_ERR = 44;
    //执行成功
    final static public Integer MSG_SUCCESS = 200;
    //熔断器报错
    final static public Integer MSG_FALLBACK = 999;
    //登录报错
    final static public Integer MSG_LOGIN_WRONG = 45;
    //登出报错
    final static public Integer MSG_LOGINOUT_WRONG = 46;
}
