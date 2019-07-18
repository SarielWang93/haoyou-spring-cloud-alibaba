package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/5 10:48
 *
 * 宠物灵魂合成
 */
@Service
public class PetSpiritualHandle extends PeopUseHandle {



    @Override
    protected void setHandleType() {
        this.handleType = "Spiritual";
    }

    @Override
    public int handle(PropUseMsg propUseMsg) {

        User user = propUseMsg.getUser();
        Prop prop = propUseMsg.getProp();
        int propCount = propUseMsg.getPropCount();

        //宠物容纳上限
        List<FightingPet> pets = FightingPet.getByUser(user, redisObjectUtil);
        if(pets.size()>=user.getCurrency().getPetMax()){
            return NO_SPACE;
        }

        String petTypeUid = prop.getProperty2();

        for(FightingPet fightingPet : pets){
            if(fightingPet.getPet().getTypeUid().equals(petTypeUid)){
                return ALREADY_HAVE;
            }
        }

        //碎片对应宠物种类
        String key = RedisKeyUtil.getKey(RedisKey.PET_TYPE, petTypeUid);
        PetType petType = redisObjectUtil.get(key, PetType.class);

        //碎片需要数量
        Integer starClass = petType.getStarClass();
        int needCount = Integer.MAX_VALUE;
        if(starClass<6){
            needCount = starClass*10;
        }else if(starClass == 6){
            needCount = starClass*10*5;
        }

        if(needCount > propCount ){
            return WRONG_COUNT;
        }


        //生成宠物

        Pet pet = new Pet(user, petType, 0);

        new FightingPet(pet,redisObjectUtil).save();

        return ResponseMsg.MSG_SUCCESS;
    }
}
