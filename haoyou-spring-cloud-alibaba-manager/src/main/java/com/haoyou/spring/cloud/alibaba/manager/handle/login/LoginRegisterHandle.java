package com.haoyou.spring.cloud.alibaba.manager.handle.login;


import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 用户注册
 */
@Service
public class LoginRegisterHandle extends ManagerHandle {
    private static final long serialVersionUID = 7039822205281201138L;
    private static final Logger logger = LoggerFactory.getLogger(LoginRegisterHandle.class);
    @Override
    protected void setHandleType() {
        this.handleType = ManagerHandle.REGISTER;
    }


    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = loginService.register(req);
        if(baseMessage.getState().equals(ResponseMsg.MSG_SUCCESS) ){
            req.setUser(((User)baseMessage));
            if(!cultivateService.petGeneration(req)){

            }
        }

        return baseMessage;
    }
}
