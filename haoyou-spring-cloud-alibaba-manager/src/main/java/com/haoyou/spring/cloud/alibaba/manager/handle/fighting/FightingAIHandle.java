package com.haoyou.spring.cloud.alibaba.manager.handle.fighting;


import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.RewardType;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 启动AI进行单机战斗
 */
@Service
public class FightingAIHandle extends ManagerHandle {
    private static final long serialVersionUID = 4622211830483903591L;
    private static final Logger logger = LoggerFactory.getLogger(FightingAIHandle.class);
    @Override
    protected void setHandleType() {
        this.handleType = SendType.FIGHTING_AI;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = new BaseMessage();
        User user = req.getUser();
        List<User> users = new ArrayList<>();
        users.add(user);
        if(fightingService.start(users,new HashMap<>(), RewardType.PVE)){
            baseMessage.setState(ResponseMsg.MSG_SUCCESS);
        }else {
            baseMessage.setState(ResponseMsg.MSG_ERR);
        }
        return baseMessage;
    }


}
