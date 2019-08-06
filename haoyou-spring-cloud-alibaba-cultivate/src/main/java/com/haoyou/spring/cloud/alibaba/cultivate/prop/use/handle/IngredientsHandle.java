package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
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
    public MapBody handle(PropUseMsg propUseMsg) {

        MapBody rt = new MapBody();

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
                rt.setState(ResponseMsg.MSG_ERR);
                return rt;
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
                rt.setState(ResponseMsg.MSG_ERR);
                return rt;
            }
            if (propIngredientsStar != ingredientsStar) {
                rt.setState(ResponseMsg.MSG_ERR);
                return rt;
            }


            //根据上限调整道具数量
            int toMaxCount = maxIngredientsCount - ingredientsCount;
            if (toMaxCount < propCount) {
                rt.setState(ResponseMsg.MSG_ERR);
                return rt;
            }




//            /**
//             * 下一等级忠诚度信息
//             */
//            String levLoyaltyKey = RedisKeyUtil.getKey(RedisKey.LEV_LOYALTY, Integer.toString(loyaltyLev + 1));
//            LevLoyalty levLoyalty = redisObjectUtil.get(levLoyaltyKey, LevLoyalty.class);
//
//            //获取升级所需食材量
//            int upLevIngredientsCount = levLoyalty.getIngredientsSum();
//            //升级所需数量
//            int upNeedCount = upLevIngredientsCount - allIngredientsCount;
//
//
//            if (upNeedCount <= 0) {
//                rt.setState(ResponseMsg.MSG_ERR);
//                return rt;
//            } else if (upNeedCount < propCount && propIngredientsStar != ingredientsStar) {
//                rt.setState(ResponseMsg.MSG_ERR);
//                return rt;
//            }


            if (propCount > 0) {
                //宠物食材数量添加
                ReflectUtil.setFieldValue(pet,ingredientsCountField,ingredientsCount+propCount);
            } else {
                rt.setState(ResponseMsg.MSG_ERR);
                return rt;
            }

            /**
             * 是否提升忠诚等级
             */

            for(int i = loyaltyLev + 1;i < 103;i++){

                LevLoyalty levLoyalty1 = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.LEV_LOYALTY, Integer.toString(i)), LevLoyalty.class);
                if(levLoyalty1.getIngredientsSum()> pet.allIngredientsCount()){
                    pet.setLoyaltyLev(levLoyalty1.getLoyaltyLev()-1);
                    break;
                }

            }


            //刷新条数
            fightingPet.getPet().initIngredientsPieces();
            //刷新面板数据
            fightingPet.refreshMbByLevel();

            //保存结果
            fightingPet.save();

            //数值系统
            cultivateService.numericalAdd(user,"feeding_pets",propCount);
            cultivateService.numericalAdd(user,"daily_pet_feed",1L);

            rt.setState(ResponseMsg.MSG_SUCCESS);
            return rt;
        }
        //合成道具
        else if (type == COM && propIngredientsStar < 3) {
            List<Prop> propList = new ArrayList<>();

            prop.setPropInstenceUid(IdUtil.simpleUUID());
            prop.setProperty2(Integer.toString(propIngredientsStar + 1));
            prop.setCount(propCount / 5);

            propList.add(prop);
            userUtil.addProps(user,propList);

            Award award = new Award();
            award.setPropsList(propList);
            rt.put("award", award);

            rt.setState(ResponseMsg.MSG_SUCCESS);
            return rt;
        }
        //拆分道具
        else if (type == SPLIT && propIngredientsStar > 1) {
            List<Prop> propList = new ArrayList<>();

            prop.setPropInstenceUid(IdUtil.simpleUUID());
            prop.setProperty2(Integer.toString(propIngredientsStar - 1));
            prop.setCount(propCount * 5);

            propList.add(prop);
            userUtil.addProps(user,propList);

            Award award = new Award();
            award.setPropsList(propList);
            rt.put("award", award);

            rt.setState(ResponseMsg.MSG_SUCCESS);
            return rt;
        }
        rt.setState(ResponseMsg.MSG_ERR);
        return rt;
    }
}
