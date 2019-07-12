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
 * 匹配接受处理
 */
@Service
public class MatchAcceptHandle extends ManagerHandle {


    private static final long serialVersionUID = -3878153190880725986L;
    private static final Logger logger = LoggerFactory.getLogger(MatchAcceptHandle.class);
    @Override
    protected void setHandleType() {
        this.handleType = SendType.MATCH_ACCEPT;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = new BaseMessage();
        if(matchService.playerAccept(req, 1)){
            baseMessage.setState(ResponseMsg.MSG_SUCCESS);
        }else{
            baseMessage.setState(ResponseMsg.MSG_ERR);
        }
        return baseMessage;
    }
}
