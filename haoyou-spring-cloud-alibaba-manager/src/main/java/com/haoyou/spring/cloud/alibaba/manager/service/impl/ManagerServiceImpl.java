package com.haoyou.spring.cloud.alibaba.manager.service.impl;

import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.apache.dubbo.config.annotation.Service;
import com.alibaba.fescar.spring.annotation.GlobalTransactional;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
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

    /**
     * 处理器
     */
    private static Map<Integer, ManagerHandle> managerHanderMap = new HashMap<>();

    /**
     * 注册信息处理器
     *
     * @param managerHandle
     */
    public static void putManagerHanderMap(ManagerHandle managerHandle) {
        managerHanderMap.put(managerHandle.getHandleType(), managerHandle);
    }

    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private SendMsgUtil sendMsgUtil;
    @Autowired
    private UserUtil userUtil;

    @Override
    @GlobalTransactional
    public BaseMessage handle(MyRequest req) {

        Integer type = req.getId();
        byte[] msg = req.getMsg();
        String msgJson = "";
        if (msg != null) {
            msgJson = new String(msg);
        }
        String useruid = req.getUseruid();


        if(type != 2){
            logger.info(String.format("manager-receive：%s %s %s", type, useruid, msgJson));
        }


        User user = null;
        /**
         * 登录验证
         */
        //登录和注册传入user
        if (type == SendType.LOGIN || type == SendType.REGISTER) {
            user = sendMsgUtil.deserialize(req.getMsg(), User.class);
        }
        //心跳和版本验证不需要登录
        else if (type == SendType.BEAT || type == SendType.VERSION_CONTROLLER) {
            user = new User();
        }
        //登录验证
        else {
            String inCatch = userUtil.isInCatch(useruid);
            if(inCatch != null && inCatch.equals(RedisKeyUtil.getKey(RedisKey.USER, useruid))){
                user = redisObjectUtil.get(inCatch,User.class);
            }
        }
        //无法获取user，返回错误
        if (user == null) {
            BaseMessage baseMessage = new BaseMessage();
            baseMessage.setState(ResponseMsg.MSG_NOT_LOGIN);
            return baseMessage;
        }
        req.setUser(user);
        /**
         * 根据type处理信息
         */

        //获取处理对象
        ManagerHandle managerHandle = managerHanderMap.get(type);
        //处理并返回信息
        BaseMessage baseMessage = managerHandle.handle(req);

        if(type != 2){
            logger.info(String.format("manager-return：%s %s %s", type, useruid, baseMessage));
        }

        return baseMessage;

    }


}
