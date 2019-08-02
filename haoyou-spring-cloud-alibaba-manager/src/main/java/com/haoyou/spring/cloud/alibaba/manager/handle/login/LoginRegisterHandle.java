package com.haoyou.spring.cloud.alibaba.manager.handle.login;


import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
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
        this.handleType = SendType.REGISTER;
    }


    @Override
    public BaseMessage handle(MyRequest req) {

        User user = loginService.register(req);
        if(user.getState().equals(ResponseMsg.MSG_SUCCESS)){
            req.setUser(user);
            if(!cultivateService.petGeneration(req)){

            }
        }
        BaseMessage baseMessage = new BaseMessage();
        baseMessage.setState(user.getState());
        return baseMessage;
    }
}
