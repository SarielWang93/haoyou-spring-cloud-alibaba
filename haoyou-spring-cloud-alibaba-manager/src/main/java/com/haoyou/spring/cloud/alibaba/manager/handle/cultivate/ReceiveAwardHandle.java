package com.haoyou.spring.cloud.alibaba.manager.handle.cultivate;


import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 技能配置处理
 */
@Service
public class ReceiveAwardHandle extends ManagerHandle {


    private static final long serialVersionUID = -5962430415616625581L;
    private static final Logger logger = LoggerFactory.getLogger(ReceiveAwardHandle.class);
    @Override
    protected void setHandleType() {
        this.handleType = SendType.REC_AWARD;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        return cultivateService.receiveAward(req);
    }
}
