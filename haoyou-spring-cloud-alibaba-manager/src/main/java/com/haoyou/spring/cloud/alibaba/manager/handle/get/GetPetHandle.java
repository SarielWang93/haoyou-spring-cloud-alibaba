package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.SkillBoard;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取宠物数据
 */
@Service
public class GetPetHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private  static final Logger logger = LoggerFactory.getLogger(GetPetHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_PET;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        User user = req.getUser();

        MapBody mapBody = new MapBody<>();
        mapBody.setState(ResponseMsg.MSG_SUCCESS);

        Map<String, Object> msg = getMsgMap(req);


        String userUidKey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, user.getUid());

        String key = RedisKeyUtil.getKey(userUidKey,(String)msg.get("petUid"));

        FightingPet fightingPet = redisObjectUtil.get(key, FightingPet.class);


        mapBody.put("petSkillBoard",getSkillBoard(fightingPet));


        Pet pet = fightingPet.getPet();
        pet.setSkillBoard(null);
        mapBody.put("pet",fightingPet);

        return mapBody;
    }

    /**
     * 获取技能配置盘
     * @param fightingPet
     * @return
     */
    private SkillBoard getSkillBoard(FightingPet fightingPet){

        if(fightingPet.getPet().getSkillBoard()!=null){
            return redisObjectUtil.deserialize(fightingPet.getPet().getSkillBoard(), SkillBoard.class);
        }else{
            return new SkillBoard(9,9);
        }
    }
}
