package com.haoyou.spring.cloud.alibaba.manager.handle.login;


import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.pojo.bean.Badge;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 登录处理
 */
@Service
public class LoginHandle extends ManagerHandle {
    private static final long serialVersionUID = 7039822205281201138L;
    private static final Logger logger = LoggerFactory.getLogger(LoginHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.LOGIN;
    }


    @Override
    public BaseMessage handle(MyRequest req) {

        User login = loginService.login(req);

        if (ResponseMsg.MSG_SUCCESS == login.getState() || ResponseMsg.MSG_LOGINOUT_FIGHTING == login.getState()) {
            userUtil.otherMsg(login);
        }
        //存储用户信息B
        redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.USER_SEND, login.getUid()), login);

        return login.notTooLong();
    }



}
