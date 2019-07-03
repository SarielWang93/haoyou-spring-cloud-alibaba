package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RewardType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.cultivate.msg.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/3 9:43
 *
 * 食材道具使用控制类
 */
public class IngredientsHandle extends PeopUseHandle{
    @Override
    protected void setHandleType() {
        this.handleType =  "Ingredients";
    }

    @Override
    public boolean handle(PropUseMsg propUseMsg) {

        User user=propUseMsg.getUser();
        Prop prop=propUseMsg.getProp();
        FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, propUseMsg.getPetUid(), redisObjectUtil);
        Pet pet = fightingPet.getPet();

        //食材名称
        String ingredientName = prop.getProperty1();

        //宠物食材数量添加
        if(ingredientName.equals(pet.getIngredientsName1())){
            pet.setIngredientsCount1(pet.getIngredientsCount1()+1);
        }else if(ingredientName.equals(pet.getIngredientsName2())){
            pet.setIngredientsCount2(pet.getIngredientsCount2()+1);
        }else if(ingredientName.equals(pet.getIngredientsName3())){
            pet.setIngredientsCount3(pet.getIngredientsCount3()+1);
        }else {
            return false;
        }
        //保存结果
        fightingPet.save();
        return true;
    }
}
