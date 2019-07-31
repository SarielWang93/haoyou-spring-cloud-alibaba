package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
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
    private static final Logger logger = LoggerFactory.getLogger(GetPetHandle.class);


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

        String type = (String) msg.get("type");


        FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, (String) msg.get("petUid"), redisObjectUtil);
        Pet pet = fightingPet.getPet();


        //忠诚等级
        Integer loyaltyLev = pet.getLoyaltyLev();
        //获取忠诚等级关系
        String levLoyaltyKey = RedisKeyUtil.getKey(RedisKey.LEV_LOYALTY, loyaltyLev.toString());
        LevLoyalty levLoyalty = redisObjectUtil.get(levLoyaltyKey, LevLoyalty.class);
        //等级上限
        mapBody.put("levelMax", levLoyalty.getLevelMax());

        if (StrUtil.isNotEmpty(type)) {
            String[] split = type.split("/*");

            for (String t : split) {
                if ("fightingPet".equals(t)) {
                    //宠物信息
                    mapBody.put("fightingPet", fightingPet);

                } else if ("petSkillBoard".equals(t)) {
                    //技能盘信息
                    mapBody.put("petSkillBoard", this.getSkillBoard(fightingPet));
                } else if ("ingredientsMsg".equals(t)) {
                    //食材信息
                    mapBody.put("ingredientsMsg", this.getIngredientsMsg(user, fightingPet,levLoyalty));
                } else if ("cultureMsg".equals(t)) {
                    //培养信息
                    mapBody.put("cultureMsg", this.getCultureMsg(user, fightingPet));
                }

            }


        } else {
            //宠物信息
            mapBody.put("fightingPet", fightingPet);


            //技能盘信息
            mapBody.put("petSkillBoard", this.getSkillBoard(fightingPet));


            //食材限定
            mapBody.put("ingredientsMsg", this.getIngredientsMsg(user, fightingPet,levLoyalty));

            //食材限定
            mapBody.put("cultureMsg", this.getCultureMsg(user, fightingPet));
        }


        pet.setSkillBoard(null);

        return mapBody;
    }


    private Map<String, Object> getIngredientsMsg(User user, FightingPet fightingPet, LevLoyalty levLoyalty) {
        Map<String, Object> ingredientsMsg = new HashMap<>();
        Pet pet = fightingPet.getPet();

        List<Prop> props = user.propList();

        Integer level = pet.getLevel();
        Integer starClass = pet.getStarClass();
        int allIngredientsCount = pet.allIngredientsCount();

        String nextLevLoyaltyKey = RedisKeyUtil.getKey(RedisKey.LEV_LOYALTY, Integer.toString(pet.getLoyaltyLev() + 1));
        LevLoyalty nextLevLoyalty = redisObjectUtil.get(nextLevLoyaltyKey, LevLoyalty.class);


        //忠诚等级
        ingredientsMsg.put("loyaltyLev", levLoyalty.getLoyaltyLev());
        //忠诚升级
        ingredientsMsg.put("nextLevIngredients", nextLevLoyalty.getIngredients());
        ingredientsMsg.put("nowAllIngredients", nextLevLoyalty.getIngredients() - (nextLevLoyalty.getIngredientsSum() - allIngredientsCount));

        for (Prop prop : props) {
            if (prop.getName().equals("LoyaltyCard")) {
                ingredientsMsg.put("loyaltyCardCount", prop.getCount());
                ingredientsMsg.put("loyaltyCardInstenceUid",prop.getPropInstenceUid());
            }
        }


        List<Map> list = new ArrayList<>();
        for (int i = 1; i < 5; i++) {

            Map<String, Object> ingredient = new HashMap<>();
            String ingredientsAttr = (String) ReflectUtil.getFieldValue(pet, String.format("ingredientsAttr%s", i));
            String ingredientsName = (String) ReflectUtil.getFieldValue(pet, String.format("ingredientsName%s", i));
            //食材名称
            ingredient.put("ingredientsName", ingredientsName);
            //食材加的属性
            ingredient.put("ingredientsAttr", ingredientsAttr);

            //提升基数
            int up = 0;
            if (starClass < 3) {
                up = starClass;
            } else {
                up = (starClass - 2) * 5;
            }

            if (ingredientsAttr.equals("hp")) {
                up *= 5;
            }
            //属性加的量
            ingredient.put("ingredientsAttrCount", up);

            Integer count = (Integer) ReflectUtil.getFieldValue(pet, String.format("ingredientsCount%s", i));
            int maxIngredientsCount = 100 * (level / 10 + 1) + pet.getIngredientsLimit() * 10;
            if (count < 950) {
                if (maxIngredientsCount > 950) {
                    maxIngredientsCount = 950;
                }
                //食材上限
                ingredient.put("maxIngredients", maxIngredientsCount);
                //食材当前数量
                ingredient.put("nowIngredients", count);
                //食材星级
                ingredient.put("starIngredients", 1);
            } else if (count < 3950) {
                if (maxIngredientsCount > 3950) {
                    maxIngredientsCount = 3000;
                }
                ingredient.put("maxIngredients", maxIngredientsCount - 950);
                ingredient.put("nowIngredients", count - 950);
                ingredient.put("starIngredients", 2);
            } else if (count <= 10000 + pet.getIngredientsLimit() * 10) {
                ingredient.put("maxIngredients", maxIngredientsCount - 950 - 3000);
                ingredient.put("nowIngredients", count - 950 - 3000);
                ingredient.put("starIngredients", 3);
            }
            Integer pieces = (Integer) ReflectUtil.getFieldValue(pet, String.format("ingredientsPieces%s", i));
            int thisPiecesNeedCount = (pieces / 20 + 2) * 5;
            int needCount = pet.piecesNeedCount(pieces);

            //食材当当前条的上限
            ingredient.put("piecesNeedCount", thisPiecesNeedCount);
            //食材当当前条的数量
            ingredient.put("piecesNowCount", thisPiecesNeedCount - (needCount - count));

            //可用食材道具数量

            for (Prop prop : props) {
                if (prop.getProperty1().equals(ingredientsName) && Integer.valueOf(prop.getProperty2()).equals(ingredient.get("starIngredients"))) {
                    ingredient.put("ingredientProp", prop);
                }
            }


            list.add(ingredient);
        }

        ingredientsMsg.put("ingredients", list);

        return ingredientsMsg;
    }

    /**
     * 获取技能配置盘
     *
     * @param fightingPet
     * @return
     */
    private SkillBoard getSkillBoard(FightingPet fightingPet) {
        Pet pet = fightingPet.getPet();
        //满格技能添加
        if (StrUtil.isNotEmpty(pet.getFullSkillBoard()))
            fightingPet.getSkills().add(this.redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.SKILL, pet.getFullSkillBoard()), Skill.class));

        if (pet.getSkillBoard() != null) {
            return redisObjectUtil.deserialize(pet.getSkillBoard(), SkillBoard.class);
        } else {
            return new SkillBoard(6, 6);
        }
    }


    private Map<String, Object> getCultureMsg(User user, FightingPet fightingPet) {
        Map<String, Object> cultureMsg = new HashMap<>();
        Pet pet = fightingPet.getPet();
        //当前培养等级
        Integer culture = pet.getCulture();
        cultureMsg.put("cultureCoin", culture / 10 * 400 * culture + 200 * (culture % 10) + 100);
        cultureMsg.put("cultureDiamond", 10);
        cultureMsg.put("culturePropCount", culture / 10 * 4 * culture + (culture % 10) + 1);

        //培养上限
        Integer cultureLimit = pet.getCultureLimit();
        cultureMsg.put("cultureLimitPropCount", cultureLimit / 10 * 10 * cultureLimit + (cultureLimit % 10));
        cultureMsg.put("cultureLimitDiamond", cultureLimit / 10 * 10 * cultureLimit + (cultureLimit % 10) * 10);

        //道具数量
        for (Prop prop : user.propList()) {
            if("CultureMedium".equals(prop.getName())){
                cultureMsg.put("propCount",prop.getCount());
            }
        }


        return cultureMsg;
    }
}
