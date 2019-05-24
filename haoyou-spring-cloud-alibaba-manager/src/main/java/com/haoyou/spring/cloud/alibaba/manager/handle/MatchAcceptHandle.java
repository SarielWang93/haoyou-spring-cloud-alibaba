package com.haoyou.spring.cloud.alibaba.manager.handle;



import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.springframework.stereotype.Service;

/**
 * 匹配接受处理
 */
@Service
public class MatchAcceptHandle extends ManagerHandle {


    private static final long serialVersionUID = -3878153190880725986L;

    @Override
    void setHandleType() {
        this.handleType = ManagerHandle.MATCH_ACCEPT;
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
