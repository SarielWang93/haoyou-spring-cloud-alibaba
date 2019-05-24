package com.haoyou.spring.cloud.alibaba.sofabolt.connection;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alipay.remoting.Connection;

import com.haoyou.spring.cloud.alibaba.action.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.service.manager.ManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 链接信息存储类
 */
@Component
public class Connections {
    private final static Logger logger = LoggerFactory.getLogger(Connections.class);

    @Autowired
    private SendMsgUtil sendMsgUtil;


    @Reference(version = "${manager.service.version}")
    private ManagerService managerService;

    /**
     * 用户的连接对象
     */
    private ConcurrentSkipListMap<String, Connection> connections = new ConcurrentSkipListMap<>();

    /**
     * 断连缓冲
     */
    private List<String> disconnects = new ArrayList<>();

    /**
     * 添加链接
     *
     * @param key
     * @param value
     */
    public void put(String key, Connection value) {
        connections.put(key, value);
    }

    /**
     * 获取对应用户的链接
     *
     * @param key
     * @return
     */
    public Connection get(String key) {
        return connections.get(key);
    }

    /**
     * 删除对应用户的链接
     *
     * @param key
     * @return
     */
    public Connection remove(String key) {
        return connections.remove(key);
    }

    /**
     * 获取所有连接
     *
     * @return
     */
    public ConcurrentSkipListMap<String, Connection> getAllMap() {
        return connections;
    }

    /**
     * 每隔5分钟清除已断开的链接
     */
    @Scheduled(cron = "${sofabolt.connections.cleardelay: 0 */5 * * * ?}")
    public void inspect() {
        logger.info("清理断链链接！！！");
        for (String uid : disconnects) {
            Connection connection = connections.get(uid);
            if (connection != null && !connection.getChannel().isActive())
            //待清理中未重连，登出
            {
                User user = new User();
                user.setUid(uid);
                try {
                    //登出
                    BaseMessage respmsg = managerService.handle(new MyRequest(1, uid, null));
                    if(ResponseMsg.MSG_SUCCESS==respmsg.getState()){
                        //登出成功，删除链接
                        connections.remove(uid);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //已重连从待清理中移除
        disconnects = new ArrayList<>();


        //将断链链接放入待清理
        connections.forEach((uid, connection) -> {
            if (!connection.getChannel().isActive()) {
                disconnects.add(uid);
            }
        });


    }
}
