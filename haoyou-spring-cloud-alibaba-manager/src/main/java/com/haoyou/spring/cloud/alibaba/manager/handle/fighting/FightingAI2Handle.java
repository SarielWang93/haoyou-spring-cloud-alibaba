package com.haoyou.spring.cloud.alibaba.manager.handle.fighting;


import com.haoyou.spring.cloud.alibaba.commons.domain.FightingType;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.RewardType;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
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
public class FightingAI2Handle extends ManagerHandle {
    private static final long serialVersionUID = 4622211830483903591L;
    private static final Logger logger = LoggerFactory.getLogger(FightingAI2Handle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.FIGHTING_AI2;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = new BaseMessage();
        User user = req.getUser();
        List<User> users = new ArrayList<>();
        users.add(user);
        HashMap<String, Boolean> allIsAi = new HashMap<>();
        allIsAi.put(user.getUid(), true);
        if (fightingService.start(users, allIsAi, RewardType.PVE, FightingType.PVE)) {
            baseMessage.setState(ResponseMsg.MSG_SUCCESS);
        } else {
            baseMessage.setState(ResponseMsg.MSG_ERR);
        }
        return baseMessage;
    }


}
