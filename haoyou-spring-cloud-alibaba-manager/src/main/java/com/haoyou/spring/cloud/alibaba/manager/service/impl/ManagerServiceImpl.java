package com.haoyou.spring.cloud.alibaba.manager.service.impl;

import cn.hutool.core.lang.Console;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fescar.spring.annotation.GlobalTransactional;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.service.manager.ManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * 通信处理中心，根据类型调用对应服务
 */
@Service(version = "${manager.service.version}")
public class ManagerServiceImpl implements ManagerService {
    private final static Logger logger = LoggerFactory.getLogger(ManagerServiceImpl.class);


    private Map<Integer, ManagerHandle> managerHanderMap = new HashMap<>();


    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private SendMsgUtil sendMsgUtil;

    @Override
    @GlobalTransactional
    public BaseMessage handle(MyRequest req) {


        Integer type = req.getId();
        byte[] msg = req.getMsg();
        String useruid = req.getUseruid();


        logger.info(String.format("manager：%s %s", type, useruid));


        User user = null;
        /**
         * 登录验证
         */
        //登录和注册传入user
        if (type == ManagerHandle.LOGIN || type == ManagerHandle.REGISTER) {
            user = sendMsgUtil.deserialize(req.getMsg(), User.class);
        }
        //心跳和版本验证不需要登录
        else if (type == ManagerHandle.BEAT || type == ManagerHandle.VERSION_CONTROLLER) {
            user = new User();
        }
        //登录验证
        else {
            user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, useruid), User.class);
        }
        //无法获取user，返回错误
        if (user == null) {
            BaseMessage baseMessage = new BaseMessage();
            baseMessage.setState(ResponseMsg.MSG_ERR);
            return baseMessage;
        }
        req.setUser(user);
        /**
         * 根据type处理信息
         */

        //获取处理对象
        ManagerHandle managerHandle = this.managerHanderMap.get(type);
        //处理并返回信息
        BaseMessage baseMessage = managerHandle.handle(req);

        //Console.log(baseMessage);
        return baseMessage;

    }


    /**
     * 注册信息处理器
     *
     * @param managerHandle
     */
    public void putManagerHanderMap(ManagerHandle managerHandle) {
        managerHanderMap.put(managerHandle.getHandleType(), managerHandle);
    }

}
