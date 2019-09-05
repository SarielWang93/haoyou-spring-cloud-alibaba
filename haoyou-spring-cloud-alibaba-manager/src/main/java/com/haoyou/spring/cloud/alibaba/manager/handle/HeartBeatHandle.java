package com.haoyou.spring.cloud.alibaba.manager.handle;


import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        BaseMessage baseMessage = BaseMessage.beSuccess();




//        Map<String, Object> otherMsg = new HashMap<>();
//        服务器时间
//        otherMsg.put("serverDate", new Date());
//        baseMessage.setOtherMsg(otherMsg);


        return baseMessage;
    }
}
