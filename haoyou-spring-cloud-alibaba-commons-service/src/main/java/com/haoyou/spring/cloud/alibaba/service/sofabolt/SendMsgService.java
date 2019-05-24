package com.haoyou.spring.cloud.alibaba.service.sofabolt;


import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;

/**
 * 基于SOFABolt向用户发送信息
 */

public interface SendMsgService {


    MyRequest sendMsgOne(MyRequest req);


    boolean sendMsgAll(MyRequest req);


    boolean sendMsgOneNoReturn(MyRequest req);
}
