package com.haoyou.spring.cloud.alibaba.manager.handle;



import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.springframework.stereotype.Service;

/**
 * 登出处理
 */
@Service
public class LoginOutHandleHandle extends ManagerHandle {
    private static final long serialVersionUID = -76046260146768715L;

    @Override
    void setHandleType() {
        this.handleType = ManagerHandle.LOGINOUT;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = loginService.logout(req);
        return baseMessage;
    }
}
