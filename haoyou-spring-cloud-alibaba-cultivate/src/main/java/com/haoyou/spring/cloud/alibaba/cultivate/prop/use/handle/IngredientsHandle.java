package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.LevLoyalty;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/3 9:43
 * <p>
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
    public int handle(PropUseMsg propUseMsg) {

        User user = propUseMsg.getUser();
        Prop prop = propUseMsg.getProp();
        int propIngredientsStar = Integer.parseInt(prop.getProperty2());
        int propCount = propUseMsg.getPropCount();
        int type = propUseMsg.getType();
        //使用道具
        if (type == USE) {

            FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, propUseMsg.getPetUid(), redisObjectUtil);
            Pet pet = fightingPet.getPet();
            Integer loyaltyLev = pet.getLoyaltyLev();

            Integer level = pet.getLevel();
            //食材名称
            String ingredientName = prop.getProperty1();
            //食材总量
            int allIngredientsCount = pet.allIngredientsCount();



            /**
             * 当前等级食材上限
             */

            int maxIngredientsCount = 100 * (level / 10 + 1)+pet.getIngredientsLimit()*10;

            Field ingredientsCountField = null ;
            int ingredientsCount = Integer.MAX_VALUE;

            if (ingredientName.equals(pet.getIngredientsName1())) {
                ingredientsCount = pet.getIngredientsCount1();
                ingredientsCountField = ReflectUtil.getField(Pet.class, "ingredientsCount1");
            } else if (ingredientName.equals(pet.getIngredientsName2())) {
                ingredientsCount = pet.getIngredientsCount2();
                ingredientsCountField = ReflectUtil.getField(Pet.class, "ingredientsCount2");
            } else if (ingredientName.equals(pet.getIngredientsName3())) {
                ingredientsCount = pet.getIngredientsCount3();
                ingredientsCountField = ReflectUtil.getField(Pet.class, "ingredientsCount3");
            } else if (ingredientName.equals(pet.getIngredientsName4())) {
                ingredientsCount = pet.getIngredientsCount4();
                ingredientsCountField = ReflectUtil.getField(Pet.class, "ingredientsCount4");
            }else {
                return ResponseMsg.MSG_ERR;
            }

            /**
             * 食材星级控制
             */
            int ingredientsStar = 0;
            if (ingredientsCount < 950) {
                ingredientsStar = 1;
            }else if(ingredientsCount < 3950){
                ingredientsStar = 2;
            }else if(ingredientsCount < 10000 + pet.getIngredientsLimit()*10){
                ingredientsStar = 3;
            }else{
                return ResponseMsg.MSG_ERR;
            }
            if (propIngredientsStar != ingredientsStar) {
                return ResponseMsg.MSG_ERR;
            }


            //根据上限调整道具数量
            int toMaxCount = maxIngredientsCount - ingredientsCount;
            if (toMaxCount < propCount) {
                return ResponseMsg.MSG_ERR;
            }




            /**
             * 下一等级忠诚度信息
             */
            String levLoyaltyKey = RedisKeyUtil.getKey(RedisKey.LEV_LOYALTY, Integer.toString(loyaltyLev + 1));
            LevLoyalty levLoyalty = redisObjectUtil.get(levLoyaltyKey, LevLoyalty.class);

            //获取升级所需食材量
            int upLevIngredientsCount = levLoyalty.getIngredientsSum();
            //升级所需数量
            int upNeedCount = upLevIngredientsCount - allIngredientsCount;
            //下一等级食材星级
            ingredientsStar = (loyaltyLev + 1) / 10 + 1;

            if (upNeedCount <= 0) {
                return ResponseMsg.MSG_ERR;
            } else if (upNeedCount < propCount && propIngredientsStar != ingredientsStar) {
                return ResponseMsg.MSG_ERR;
            }


            if (propCount > 0) {
                //宠物食材数量添加
                ReflectUtil.setFieldValue(pet,ingredientsCountField,ingredientsCount+propCount);
            } else {
                return ResponseMsg.MSG_ERR;
            }

            /**
             * 是否提升忠诚等级
             */
            if (propCount >= upNeedCount) {
                pet.setLoyaltyLev(loyaltyLev + 1);
            }

            //刷新面板数据
            fightingPet.refreshMbByLevel();
            //刷新条数
            fightingPet.getPet().initIngredientsPieces();

            //保存结果
            fightingPet.save();
            return ResponseMsg.MSG_SUCCESS;
        }
        //合成道具
        else if (type == COM && propIngredientsStar < 3) {
            List<Prop> propList = new ArrayList<>();

            prop.setPropInstenceUid(IdUtil.simpleUUID());
            prop.setProperty2(Integer.toString(propIngredientsStar + 1));
            prop.setCount(propCount / 5);

            propList.add(prop);
            user.addProps(propList);
            return ResponseMsg.MSG_SUCCESS;
        }
        //拆分道具
        else if (type == SPLIT && propIngredientsStar > 1) {
            List<Prop> propList = new ArrayList<>();

            prop.setPropInstenceUid(IdUtil.simpleUUID());
            prop.setProperty2(Integer.toString(propIngredientsStar - 1));
            prop.setCount(propCount * 5);

            propList.add(prop);
            user.addProps(propList);
            return ResponseMsg.MSG_SUCCESS;
        }
        return ResponseMsg.MSG_ERR;
    }
}
