package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/5 10:48
 * <p>
 * 宠物灵魂合成
 */
@Service
public class PetSpiritualHandle extends PeopUseHandle {


    @Override
    protected void setHandleType() {
        this.handleType = "Spiritual";
    }

    @Override
    public MapBody handle(PropUseMsg propUseMsg) {

        MapBody rt = new MapBody();

        User user = propUseMsg.getUser();
        Prop prop = propUseMsg.getProp();
        long propCount = propUseMsg.getPropCount();

        //宠物容纳上限
        List<FightingPet> pets = FightingPet.getByUser(user, redisObjectUtil);
        if (pets.size() >= user.getCurrency().getPetMax()) {
            rt.setState(NO_SPACE);
            return rt;
        }

        String petTypeUid = prop.getProperty2();

        for (FightingPet fightingPet : pets) {
            if (fightingPet.getPet().getTypeUid().equals(petTypeUid)) {
                rt.setState(ALREADY_HAVE);
                return rt;

            }
        }

        //碎片对应宠物种类
        String key = RedisKeyUtil.getKey(RedisKey.PET_TYPE, petTypeUid);
        PetType petType = redisObjectUtil.get(key, PetType.class);

        //碎片需要数量
        Integer starClass = petType.getStarClass();
        int needCount = Integer.MAX_VALUE;
        if (starClass < 6) {
            needCount = starClass * 10;
        } else if (starClass == 6) {
            needCount = starClass * 10 * 5;
        }

        if (needCount > propCount) {
            rt.setState(WRONG_COUNT);
            return rt;
        }


        //生成宠物

        Pet pet = new Pet(user, petType, 0);

        new FightingPet(pet, redisObjectUtil).save();
        //数值系统
        cultivateService.numericalAdd(user, "have_pets", 1L);

        pet.setSkillBoard(null);
        rt.put("pet", pet);
        rt.setState(ResponseMsg.MSG_SUCCESS);
        return rt;
    }
}
