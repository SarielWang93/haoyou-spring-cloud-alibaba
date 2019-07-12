package com.haoyou.spring.cloud.alibaba.manager.handle.match;


import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 匹配拒绝处理
 */
@Service
public class MatchResufeHandle extends ManagerHandle {

    private static final long serialVersionUID = -5019164885937253177L;
    private static final Logger logger = LoggerFactory.getLogger(MatchResufeHandle.class);
    @Override
    protected void setHandleType() {
        this.handleType = SendType.MATCH_REFUSE;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = new BaseMessage();
        if(matchService.playerAccept(req, 2)){
            baseMessage.setState(ResponseMsg.MSG_SUCCESS);
        }else{
            baseMessage.setState(ResponseMsg.MSG_ERR);
        }
        return baseMessage;
    }
}
