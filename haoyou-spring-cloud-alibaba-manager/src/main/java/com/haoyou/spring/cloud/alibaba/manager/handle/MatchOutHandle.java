package com.haoyou.spring.cloud.alibaba.manager.handle;


import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.springframework.stereotype.Service;

/**
 * 退出匹配处理
 */
@Service
public class MatchOutHandle extends ManagerHandle {

    private static final long serialVersionUID = 7044083678101450536L;

    @Override
    void setHandleType() {
        this.handleType = ManagerHandle.MATCH_OUT;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = new BaseMessage();
        if(matchService.removePlayerFromMatchPool(req.getUser())){
            baseMessage.setState(ResponseMsg.MSG_SUCCESS);
        }else{
            baseMessage.setState(ResponseMsg.MSG_ERR);
        }
        return baseMessage;
    }
}
