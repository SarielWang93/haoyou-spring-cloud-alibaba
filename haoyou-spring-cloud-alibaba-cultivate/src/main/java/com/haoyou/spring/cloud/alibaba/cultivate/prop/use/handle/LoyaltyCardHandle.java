package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;


import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import org.springframework.stereotype.Service;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/5 10:48
 * <p>
 * 宠物忠诚卡
 */
@Service
public class LoyaltyCardHandle extends PeopUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = "LoyaltyCard";
    }

    @Override
    public MapBody handle(PropUseMsg propUseMsg) {

        MapBody rt = new MapBody();

        User user = propUseMsg.getUser();
        int propCount = propUseMsg.getPropCount();
        FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, propUseMsg.getPetUid(), redisObjectUtil);

        Pet pet = fightingPet.getPet();

        Integer ingredientsLimit = pet.getIngredientsLimit();
        ingredientsLimit += propCount;
        if (ingredientsLimit >= 100) {
            rt.setState(LIMIT);
            return rt;
        }

        pet.setIngredientsLimit(ingredientsLimit);

        fightingPet.save();

        rt.setState(ResponseMsg.MSG_SUCCESS);
        return rt;
    }
}
