package com.haoyou.spring.cloud.alibaba.manager.handle.login;


import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        List<FightingPet> byUser = FightingPet.getByUser(login, redisObjectUtil);

        Map<String,Object> otherMsg = new HashMap<>();

        otherMsg.put("petsCount",byUser.size());

        login.setOtherMsg(otherMsg);

        return login;
    }
}
