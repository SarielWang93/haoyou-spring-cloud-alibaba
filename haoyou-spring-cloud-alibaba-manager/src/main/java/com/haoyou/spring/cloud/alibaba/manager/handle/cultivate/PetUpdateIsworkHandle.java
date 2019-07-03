package com.haoyou.spring.cloud.alibaba.manager.handle.cultivate;

import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/6/28 16:14
 */
public class PetUpdateIsworkHandle extends ManagerHandle {


    private static final long serialVersionUID = -6086584787045513214L;

    @Override
    protected void setHandleType() {
        this.handleType = SendType.PET_UPDATE_ISWORK;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = new BaseMessage();
        if(cultivateService.updateIsWork(req)){
            baseMessage.setState(ResponseMsg.MSG_SUCCESS);
        }else{
            baseMessage.setState(ResponseMsg.MSG_ERR);
        }
        return baseMessage;
    }
}
