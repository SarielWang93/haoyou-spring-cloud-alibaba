package com.haoyou.spring.cloud.alibaba.manager.handle.login;



import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 登出处理
 */
@Service
public class LoginOutHandleHandle extends ManagerHandle {
    private static final long serialVersionUID = -76046260146768715L;
    private static final Logger logger = LoggerFactory.getLogger(LoginOutHandleHandle.class);
    @Override
    protected void setHandleType() {
        this.handleType = SendType.LOGINOUT;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        User logOut = loginService.logout(req);
        return logOut.notTooLong();
    }
}
