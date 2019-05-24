package com.haoyou.spring.cloud.alibaba.manager.handle;


import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.springframework.stereotype.Service;

/**
 * 登录处理
 */
@Service
public class LoginHandle extends ManagerHandle {
    private static final long serialVersionUID = 7039822205281201138L;

    @Override
    void setHandleType() {
        this.handleType = ManagerHandle.LOGIN;
    }


    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = loginService.login(req);
        return baseMessage;
    }
}
