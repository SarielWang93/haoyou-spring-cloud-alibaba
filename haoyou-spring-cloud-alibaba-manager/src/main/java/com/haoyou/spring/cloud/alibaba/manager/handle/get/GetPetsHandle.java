package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * 获取背包道具
 */
@Service
public class GetPetsHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetPetsHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_PETS;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        User user = req.getUser();

        MapBody mapBody = new MapBody<>();
        mapBody.setState(ResponseMsg.MSG_SUCCESS);

        String userUidKey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, user.getUid());

        String key = RedisKeyUtil.getlkKey(userUidKey);
        HashMap<String, FightingPet> fightingPets = redisObjectUtil.getlkMap(key, FightingPet.class);

        for(FightingPet fightingPet:fightingPets.values()){
            mapBody.put("fightingPet",fightingPet);
            sendMsgUtil.sendMsgOneNoReturn(user.getUid(),req.getId(),mapBody);
        }
        mapBody.remove("fightingPet");
        return mapBody;
    }
}
