package com.haoyou.spring.cloud.alibaba.manager.handle;


import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.springframework.stereotype.Service;

/**
 * 心跳信息处理
 */
@Service
public class HeartBeatHandle extends ManagerHandle {
    private static final long serialVersionUID = -5899037112382384750L;

    @Override
    void setHandleType() {
        this.handleType = ManagerHandle.BEAT;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = new BaseMessage();
        baseMessage.setState(ResponseMsg.MSG_SUCCESS);
        return baseMessage;
    }
}
