package com.haoyou.spring.cloud.alibaba.commons.domain;

public class ResponseMsg {
    //错误
    final static public int MSG_ERR = 44;
    //执行成功
    final static public int MSG_SUCCESS = 200;
    //熔断器报错
    final static public int MSG_FALLBACK = 999;
    //协议未找到
    final static public int ID_ERR = 444;
    //用户未登录
    final static public int MSG_NOT_LOGIN = 52;
    //登录报错
    final static public int MSG_LOGIN_WRONG = 45;
    //登录用户名不存在
    final static public int MSG_LOGIN_USERNAME_WRONG = 48;
    //登录密码报错
    final static public int MSG_LOGIN_PASSWORD_WRONG = 47;
    //登出报错
    final static public int MSG_LOGINOUT_WRONG = 46;

    //登出报错
    final static public int MSG_LOGINOUT_FIGHTING = 49;

    //注册用户名已占用
    final static public int MSG_REGISTER_USERNAME_EXIST = 51;


    final static public int MSG_NOT_FIND_FIGHTING_ROOM = 53;


    //执行成功
    final static public int ALREADY_REGISTERED = 61;


}
