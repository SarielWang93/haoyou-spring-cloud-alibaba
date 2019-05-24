package com.haoyou.spring.cloud.alibaba.sofabolt.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.remoting.Connection;
import com.haoyou.spring.cloud.alibaba.service.sofabolt.SendMsgService;
import com.haoyou.spring.cloud.alibaba.sofabolt.connection.Connections;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.sofabolt.server.MyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * 通过链接 发送信息
 */
@Service(version = "${send-msg.service.version}")
public class SendMsgServiceImpl implements SendMsgService {
    private final static Logger logger = LoggerFactory.getLogger(SendMsgServiceImpl.class);

    //默认发送超时时间
    @Value("${sofabolt.server.sendouttime}")
    private int defouttime=30000;
    @Autowired
    Connections connections;

    /**
     * 给一个用户发送信息
     *
     * @param req
     * @return
     */
    @Override
    public MyRequest sendMsgOne(MyRequest req) {
        logger.info(String.format("发送信息：%s",req));

        Connection connection = connections.get(req.getUseruid());
        MyRequest resp = null;
        try {
            resp = (MyRequest)MyServer.server.invokeSync(connection, req, defouttime);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }



    }

    @Override
    public boolean sendMsgOneNoReturn(MyRequest req) {
        logger.info(String.format("发送信息无返回：%s",req));
        Connection connection = connections.get(req.getUseruid());
        try {
            MyServer.server.oneway(connection, req);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 给所有在线用户发送信息
     *
     * @param req
     * @return
     */
    @Override
    public boolean sendMsgAll(MyRequest req) {
        logger.info(String.format("发送信息全部：%s",req));

        connections.getAllMap().forEach((s, connection) -> {
            req.setUseruid(s);
            try {
                MyServer.server.oneway(connection, req);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return true;


    }



}
