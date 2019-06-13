package com.haoyou.spring.cloud.alibaba.sofabolt.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.remoting.Connection;
import com.haoyou.spring.cloud.alibaba.service.sofabolt.SendMsgService;
import com.haoyou.spring.cloud.alibaba.sofabolt.connection.Connections;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.sofabolt.server.MyServer;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.Date;

/**
 * 通过链接 发送信息
 */
@Service(version = "${send-msg.service.version}")
@RefreshScope
public class SendMsgServiceImpl implements SendMsgService {
    private final static Logger logger = LoggerFactory.getLogger(SendMsgServiceImpl.class);

    //默认发送超时时间
    @Value("${sofabolt.connections.sendouttime:30000}")
    private int defouttime;

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
        logger.info(String.format("发送信息：%s %s", req.getId(), req.getUseruid()));

        Connection connection = connections.get(req.getUseruid());
        if (connection == null) {
            return null;
        }
        MyRequest resp = null;
        try {
            resp = (MyRequest) MyServer.server.invokeSync(connection, req, defouttime);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;


    }

    @Override
    public boolean sendMsgOneNoReturn(MyRequest req) {
        logger.info(String.format("发送信息无返回：%s %s", req.getId(), req.getUseruid()));
        Connection connection = connections.get(req.getUseruid());
        if (connection == null) {
            return false;
        }
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
        logger.info(String.format("发送信息全部：%s %s", req.getId(), req.getUseruid()));

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

    /**
     * 判断用户，链接是否健康
     *
     * @param userUid
     * @return
     */
    @Override
    public boolean connectionIsAlive(String userUid) {
        return connections.connectionIsAlive(userUid);

    }
}
