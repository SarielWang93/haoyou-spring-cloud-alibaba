package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import cn.hutool.core.util.IdUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.LevLoyalty;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/3 9:43
 *
 * 食材道具使用控制类
 */
@Service
public class IngredientsHandle extends PeopUseHandle {

    //食用食材
    public final static int USE = 1;
    //合成食材
    public final static int COM = 2;
    //拆分食材
    public final static int SPLIT = 3;


    @Override
    protected void setHandleType() {
        this.handleType = "Ingredients";
    }

    @Override
    public boolean handle(PropUseMsg propUseMsg) {

        User user = propUseMsg.getUser();
        Prop prop = propUseMsg.getProp();
        int propIngredientsStar = Integer.parseInt(prop.getProperty2());
        int propCount = propUseMsg.getPropCount();
        int type = propUseMsg.getType();
        //使用道具
        if(type == USE){
            FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, propUseMsg.getPetUid(), redisObjectUtil);
            Pet pet = fightingPet.getPet();
            Integer loyaltyLev = pet.getLoyaltyLev();
            Integer starClass = pet.getStarClass();
            Integer level = pet.getLevel();
            /**
             * 食材星级控制
             */
            int ingredientsStar = loyaltyLev / 10 + 1;
            if (propIngredientsStar != ingredientsStar) {
                return false;
            }

            //食材总量
            int allIngredientsCount = pet.getIngredientsCount1() + pet.getIngredientsCount2() + pet.getIngredientsCount3();

            /**
             * 当前等级食材上限
             */
            int base = 200;
            if (starClass < 3) {
                base *= 2;
            } else if (starClass < 5) {
                base *= starClass;
            } else {
                base = base * 2 * (starClass - 2);
            }

            int maxIngredientsCount = base * (level / 10 + 1);

            int toMaxCount = maxIngredientsCount - allIngredientsCount;

            if(toMaxCount < propCount){
                propCount = toMaxCount;
            }


            /**
             * 下一等级忠诚度信息
             */
            String levLoyaltyKey = RedisKeyUtil.getKey(RedisKey.LEV_LOYALTY, Integer.toString(loyaltyLev + 1));
            LevLoyalty levLoyalty = redisObjectUtil.get(levLoyaltyKey, LevLoyalty.class);

            int upLevIngredientsCount = Integer.MAX_VALUE;
            //根据星级获取升级所需食材量
            if (starClass < 3) {
                upLevIngredientsCount = levLoyalty.getIngredients12Sum();
            } else if (starClass < 5) {
                upLevIngredientsCount = levLoyalty.getIngredients34Sum();
            } else {
                upLevIngredientsCount = levLoyalty.getIngredients56Sum();
            }
            //升级所需数量
            int upNeedCount =  upLevIngredientsCount - allIngredientsCount;
            //下一等级食材星级
            ingredientsStar = (loyaltyLev+1) / 10 + 1;

            if(upNeedCount <= 0){
                propCount = 0;
            }else if(upNeedCount < propCount && propIngredientsStar != ingredientsStar){
                propCount = upNeedCount;
            }


            if (propCount > 0) {
                //食材名称
                String ingredientName = prop.getProperty1();
                //宠物食材数量添加
                if (ingredientName.equals(pet.getIngredientsName1())) {
                    pet.setIngredientsCount1(pet.getIngredientsCount1() + propCount);
                } else if (ingredientName.equals(pet.getIngredientsName2())) {
                    pet.setIngredientsCount2(pet.getIngredientsCount2() + propCount);
                } else if (ingredientName.equals(pet.getIngredientsName3())) {
                    pet.setIngredientsCount3(pet.getIngredientsCount3() + propCount);
                } else {
                    return false;
                }
            } else {
                return false;
            }

            /**
             * 是否提升忠诚等级
             */
            if (propCount >= upNeedCount) {
                pet.setLoyaltyLev(loyaltyLev + 1);
            }



            //保存结果
            fightingPet.save();
            return true;
        }
        //合成道具
        else if(type == COM && propIngredientsStar < 3){
            List<Prop> propList = new ArrayList<>();

            prop.setPropInstenceUid(IdUtil.simpleUUID());
            prop.setProperty2(Integer.toString(propIngredientsStar+1));
            prop.setCount(propCount/5);

            propList.add(prop);
            user.addProps(propList);
            return true;
        }
        //拆分道具
        else if(type == SPLIT && propIngredientsStar > 1){
            List<Prop> propList = new ArrayList<>();

            prop.setPropInstenceUid(IdUtil.simpleUUID());
            prop.setProperty2(Integer.toString(propIngredientsStar-1));
            prop.setCount(propCount*5);

            propList.add(prop);
            user.addProps(propList);
            return true;
        }
        return false;
    }
}
