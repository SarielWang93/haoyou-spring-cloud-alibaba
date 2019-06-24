package com.haoyou.spring.cloud.alibaba.sofabolt.connection;


import org.apache.dubbo.config.annotation.Reference;
import com.alipay.remoting.Connection;

import com.alipay.remoting.DefaultConnectionManager;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.service.manager.ManagerService;
import com.haoyou.spring.cloud.alibaba.sofabolt.server.MyServer;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 链接信息存储类
 */
@Component
@Data
@RefreshScope
public class Connections {
    private final static Logger logger = LoggerFactory.getLogger(Connections.class);

    public static final String HEART_BEAT = "heart_beat";
    public static final String DEVICE_UID = "deviceuid";


    @Reference(version = "${manager.service.version}")
    private ManagerService managerService;
    @Autowired
    private RedisObjectUtil redisObjectUtil;


    @Value("${sofabolt.connections.heart:2000}")
    private long heartTime;
    @Value("${sofabolt.connections.hearttry:3}")
    private int heartTry;

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
        this.remove(key);
        this.connections.put(key, value);
        MyServer.server.getConnectionManager().add(value);
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
    public void remove(String key) {
        MyServer.server.getConnectionManager().remove(this.connections.remove(key));
    }

    /**
     * 获取所有连接
     *
     * @return
     */
    public ConcurrentSkipListMap<String, Connection> getAllMap() {
        return this.connections;
    }

    /**
     * 每隔5分钟清除已断开的链接
     */
    @Scheduled(cron = "${sofabolt.connections.cleardelay: 0 */5 * * * ?}")
    public void inspect() {
//        logger.info("清理断链链接！！！");
        for (String uid : this.disconnects) {
            Connection connection = this.get(uid);
            if (connection != null && !connectionIsAlive(uid))
            //待清理中未重连，登出
            {
                try {
                    //登出
                    this.managerService.handle(new MyRequest(SendType.LOGINOUT, uid, null));
                    this.remove(uid);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //已重连从待清理中移除
        this.disconnects = new ArrayList<>();

        //将断链链接放入待清理
        this.connections.forEach((uid, connection) -> {
            if (!connectionIsAlive(uid)) {
                this.disconnects.add(uid);
            }
        });

        /**
         * 清理内存中连接不存在的玩家
         */
        HashMap<String, User> stringUserHashMap = this.redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.USER), User.class);
        for (User user : stringUserHashMap.values()) {
            if (!connections.containsKey(user.getUid())) {
                //登出
                this.managerService.handle(new MyRequest(SendType.LOGINOUT, user.getUid(), null));
            }
        }

        /**
         * 清理不存在的链接
         */
        DefaultConnectionManager connectionManager = MyServer.server.getConnectionManager();
        if (connectionManager != null) {
//            Console.log(connectionManager.getAll());
            for (List<Connection> connectionls : connectionManager.getAll().values()) {
                for (Connection connection : connectionls) {
                    if (!connections.containsValue(connection)) {
                        connectionManager.remove(connection);
                    }
                }
            }
        }


    }


    /**
     * 判断用户，链接是否健康
     *
     * @param userUid
     * @return
     */
    public boolean connectionIsAlive(String userUid) {
        Connection connection = this.get(userUid);
        if (connection != null) {

            Date heart = (Date) connection.getAttribute(HEART_BEAT);
            Date now = new Date();


            if (heart != null && now.getTime() - heart.getTime() < (heartTime * heartTry)) {
//                logger.info(String.format("%s %s",now.getTime(),heart.getTime()));
                return true;
            }
        }
//        logger.info(String.format("连接判断已断开: %s",userUid));
        return false;

    }
}
