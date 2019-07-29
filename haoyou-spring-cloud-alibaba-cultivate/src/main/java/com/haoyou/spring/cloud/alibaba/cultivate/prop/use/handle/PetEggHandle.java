package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.RandomUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/5 10:48
 * <p>
 * 宠物蛋孵化类
 */
@Service
public class PetEggHandle extends PeopUseHandle {


    @Override
    protected void setHandleType() {
        this.handleType = "PetEgg";
    }

    @Override
    public MapBody handle(PropUseMsg propUseMsg) {

        MapBody rt = new MapBody();

        User user = propUseMsg.getUser();
        Prop prop = propUseMsg.getProp();

        //获取蛋对应的配置以及池
        String eggId = prop.getProperty2();
        String petEggKey = RedisKeyUtil.getKey(RedisKey.PET_EGG, eggId);
        PetEgg petEgg = redisObjectUtil.get(petEggKey, PetEgg.class);
        //获取随机宠物
        PetType petType = null;
        if ("true".equals(petEgg.getIsPool())) {
            String petEggPoolKey = RedisKeyUtil.getlkKey(RedisKey.PET_EGG_POOL, eggId);
            HashMap<String, PetEggPool> petEggPoolHashMap = redisObjectUtil.getlkMap(petEggPoolKey, PetEggPool.class);
            petType = this.getPetTypePool(petEgg, petEggPoolHashMap.values());
        } else {
            petType = this.getPetTypeNoPool(petEgg);
        }

        if (petType == null) {
            rt.setState(NO_PETTYPE);
            return rt;
        }

        //孵化获得宠物碎片
        Prop spiritual = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PROP, "Spiritual"), Prop.class);
        spiritual.setProperty1(petType.getL10n());
        spiritual.setProperty2(petType.getUid());
        spiritual.setProperty3(petType.getStarClass().toString());
        //碎片数量
        Integer starClass = petType.getStarClass();
        int count = Integer.MAX_VALUE;
        if (starClass < 6) {
            count = starClass * 10;
        } else if (starClass == 6) {
            count = starClass * 10 * 5;
        }
        spiritual.setCount(count);

        UserUtil.addProp(user,prop);

        //数值系统
        cultivateService.numericalAdd(user,"pet_egg",1L);

        List<Prop> propList = new ArrayList<>();
        propList.add(prop);
        Award award = new Award();
        award.setPropsList(propList);
        rt.put("award",award);
        rt.setState(ResponseMsg.MSG_SUCCESS);
        return rt;


    }

    /**
     * 根据类型获取卡池
     *
     * @param petEgg
     */
    private PetType getPetTypeNoPool(PetEgg petEgg) {
        String petTypet = petEgg.getPetType();

        HashMap<String, PetType> PetTypes = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.PET_TYPE), PetType.class);

        List<PetEggPool> petEggPools = new ArrayList<>();

        for (PetType petType : PetTypes.values()) {
            if (petType.getType().equals(petTypet)) {
                PetEggPool petEggPool = new PetEggPool();
                petEggPool.setPetTypeUid(petType.getUid());
                petEggPool.setWeights(10d);
                petEggPool.setStarClass(petType.getStarClass());
                petEggPool.setEggId(petEgg.getId());

                petEggPools.add(petEggPool);

            }

        }

        return getPetTypePool(petEgg, petEggPools);

    }


    /**
     * 从卡池中获取随机蛋
     *
     * @param petEgg
     * @param petEggPools
     * @return
     */
    private PetType getPetTypePool(PetEgg petEgg, Collection<PetEggPool> petEggPools) {

        Integer star5 = petEgg.getStar5();
        Integer star4 = petEgg.getStar4();

        List<PetEggPool> list = new ArrayList<>();
        //随机数,先随机星级
        int i = RandomUtil.randomInt(100);
        int star = 0;
        if (i < star5) {
            star = 5;
        } else {
            i = RandomUtil.randomInt(100);
            if (i < star4) {
                star = 4;
            } else {
                star = 3;
            }
        }
        //获取星级池
        if (star > 3) {
            for (PetEggPool petEggPool : petEggPools) {
                if (petEggPool.getStarClass().equals(star)) {
                    list.add(petEggPool);
                }
            }
        } else {
            for (PetEggPool petEggPool : petEggPools) {
                if (petEggPool.getStarClass() <= star) {
                    list.add(petEggPool);
                }
            }
        }

        PetType rtPetType = null;
        //获取宠物

        //概率先行
        for (PetEggPool petEggPool : list) {
            if (petEggPool.getProbability() != null) {
                i = RandomUtil.randomInt(100);
                if (i < petEggPool.getProbability()) {
                    PetType petType = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PET_TYPE, petEggPool.getPetTypeUid()), PetType.class);
                    rtPetType = petType;
                    return rtPetType;
                }
            }
        }

        //权重随机
        WeightRandom.WeightObj<PetType>[] weightObjs = new WeightRandom.WeightObj[list.size()];
        for (int x = 0; x < list.size(); x++) {
            PetEggPool petEggPool = list.get(x);
            PetType petType = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PET_TYPE, petEggPool.getPetTypeUid()), PetType.class);
            weightObjs[x] = new WeightRandom.WeightObj(petType, petEggPool.getWeights());
        }
        if (rtPetType == null) {
            WeightRandom<PetType> petTypeWeightRandom = RandomUtil.weightRandom(weightObjs);
            rtPetType = petTypeWeightRandom.next();
        }
        return rtPetType;
    }
}
