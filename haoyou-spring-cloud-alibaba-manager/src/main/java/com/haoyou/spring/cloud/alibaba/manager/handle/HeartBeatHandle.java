package com.haoyou.spring.cloud.alibaba.manager.handle;


import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 心跳信息处理
 */
@Service
public class HeartBeatHandle extends ManagerHandle {
    private static final long serialVersionUID = -5899037112382384750L;
    private static final Logger logger = LoggerFactory.getLogger(HeartBeatHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.BEAT;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = new BaseMessage();
        baseMessage.setState(ResponseMsg.MSG_SUCCESS);
        return baseMessage;
    }
}
