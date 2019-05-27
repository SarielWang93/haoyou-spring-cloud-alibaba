package com.haoyou.spring.cloud.alibaba.manager.handle.fighting;


import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 战斗信息处理
 */
@Service
public class FightingMsgHandle extends ManagerHandle {
    private static final long serialVersionUID = -6168746102818559103L;
    private static final Logger logger = LoggerFactory.getLogger(FightingMsgHandle.class);
    @Override
    protected void setHandleType() {
        this.handleType = ManagerHandle.FIGHTING_MSG;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = fightingService.receiveFightingMsg(req);
        return baseMessage;
    }


}
