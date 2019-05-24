package com.haoyou.spring.cloud.alibaba.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fescar.spring.annotation.GlobalTransactional;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.action.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.action.SendMsgUtil;
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


    private Map<Integer, ManagerHandle> managerHanderMap=new HashMap<>();


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

        logger.info(String.format("manager：%s %s", type, msg));
        User user = null;

        if (type == ManagerHandle.LOGIN)
        //登录，user还未缓存
        {
            user = new User();
            user.setUid(useruid);
        }
        //从redis获取user
        else {
            user = redisObjectUtil.get(RedisKeyUtil.getKey("user", useruid), User.class);
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
        return baseMessage;

    }


    /**
     * 注册信息处理器
     * @param type
     * @param managerHandle
     */
    public void putManagerHanderMap(Integer type, ManagerHandle managerHandle){
        managerHanderMap.put(type, managerHandle);
    }

}
