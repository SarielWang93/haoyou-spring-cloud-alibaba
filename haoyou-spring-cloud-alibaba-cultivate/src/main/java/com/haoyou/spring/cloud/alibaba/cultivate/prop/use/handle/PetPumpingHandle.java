package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.RandomUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.PetType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/5 10:48
 *
 * 宠物蛋孵化类
 */
@Service
public class PetPumpingHandle extends PeopUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = "PetEgg";
    }

    @Override
    public boolean handle(PropUseMsg propUseMsg) {

        User user = propUseMsg.getUser();
        HashMap<String, PetType> stringPetTypeHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.PET_TYPE), PetType.class);
        PetType[] petTypes = (PetType[]) stringPetTypeHashMap.values().toArray();
        //权重随机
        WeightRandom.WeightObj<PetType>[] weightObjs = new WeightRandom.WeightObj[petTypes.length];
        /**
         * TODO 权重策略，临时待定，
         */
        for (int i = 0; i < petTypes.length; i++) {
            PetType petType = petTypes[i];
            weightObjs[i] = new WeightRandom.WeightObj(petType, 100 / petType.getStarClass());
        }
        WeightRandom<PetType> weightRandom = RandomUtil.weightRandom(weightObjs);

        PetType petType = weightRandom.next();
        Pet pet = new Pet(user, petType, 0);
        String userUidKey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, user.getUid());
        String key = RedisKeyUtil.getKey(userUidKey, pet.getUid());
        new FightingPet(pet,redisObjectUtil).save(key);


        return false;
    }
}
