package com.haoyou.spring.cloud.alibaba.manager.handle;


import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.springframework.stereotype.Service;

/**
 * 战斗信息处理
 */
@Service
public class FightingMsgHandle extends ManagerHandle {
    private static final long serialVersionUID = -6168746102818559103L;

    @Override
    void setHandleType() {
        this.handleType = ManagerHandle.FIGHTING_MSG;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = fightingService.receiveFightingMsg(req);
        return baseMessage;
    }


}
