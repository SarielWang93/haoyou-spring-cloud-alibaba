package com.haoyou.spring.cloud.alibaba.cultivate.impl;

import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fescar.spring.annotation.GlobalTransactional;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.cultivate.msg.PetUpLevMsg;
import com.haoyou.spring.cloud.alibaba.cultivate.msg.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.cultivate.service.PropUseService;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import org.apache.dubbo.config.annotation.Service;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.msg.SkillConfigMsg;
import com.haoyou.spring.cloud.alibaba.cultivate.service.RewardService;
import com.haoyou.spring.cloud.alibaba.cultivate.service.SkillConfigService;
import com.haoyou.spring.cloud.alibaba.service.cultivate.CultivateService;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import static com.haoyou.spring.cloud.alibaba.cultivate.msg.SkillConfigMsg.*;

/**
 * @Author: wanghui
 * @Date: 2019/5/13 11:37
 * @Version 1.0
 */
@Service(version = "${cultivate.service.version}")
@RefreshScope
public class CultivateServiceImpl implements CultivateService {
    private static final Logger logger = LoggerFactory.getLogger(CultivateServiceImpl.class);
    private static LongAdder la = new LongAdder();
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private SendMsgUtil sendMsgUtil;
    @Autowired
    private PetMapper petMapper;
    @Autowired
    private SkillConfigService skillConfigService;
    @Autowired
    private RewardService rewardService;
    @Autowired
    private PropUseService propUseService;

    /**
     * 技能配置处理
     *
     * @param req
     * @return
     */
    @Override
    public boolean skillConfig(MyRequest req) {

        User user = req.getUser();
        SkillConfigMsg skillConfigMsg = sendMsgUtil.deserialize(req.getMsg(), SkillConfigMsg.class);
        String propInstenceUid = skillConfigMsg.getPropInstenceUid();
        switch (skillConfigMsg.getType()) {
            case ADD_PET_SKILL:
                //获取道具
                Prop prop = getProp(user, propInstenceUid);
                if (prop != null) {
                    return skillConfigService.addPetSkill(user, skillConfigMsg, prop);
                }
                break;
            case REMOVE_PET_SKILL:
                return skillConfigService.removePetSkill(user, skillConfigMsg);
        }

        return false;
    }

    /**
     * 使用道具
     * @param req
     * @return
     */
    public MapBody propUse(MyRequest req) {
        MapBody rt = new MapBody();

        User user = req.getUser();
        PropUseMsg propUseMsg = sendMsgUtil.deserialize(req.getMsg(), PropUseMsg.class);
        String propInstenceUid = propUseMsg.getPropInstenceUid();
        Prop prop = getProp(user, propInstenceUid);

        if(prop!=null){
            propUseMsg.setProp(prop);
            propUseMsg.setUser(user);
            if(propUseService.propUse(propUseMsg)){
                rt.setState(ResponseMsg.MSG_SUCCESS);
                return rt;
            }
        }else{
            rt.put("errMsg","prop not find!");
        }
        rt.setState(ResponseMsg.MSG_ERR);
        return rt;
    }






    /**
     * 注册时生成三个宠物
     *
     * @param req
     * @return
     */
    @Override
    public boolean petGeneration(MyRequest req) {
        logger.debug("注册赠送宠物！！！");
        User user = req.getUser();
        List<Integer> l = new ArrayList<>();
        if (la.longValue() % 2 == 0) {
            l.add(1);
            l.add(2);
            l.add(3);
        } else {
            l.add(4);
            l.add(5);
            l.add(6);
        }
        HashMap<String, PetType> stringPetTypeHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.PET_TYPE), PetType.class);

        for (PetType petType : stringPetTypeHashMap.values()) {
            if (l.contains(petType.getId())) {
                int iswork;
                if (petType.getId() > 3) {
                    iswork = petType.getId() - 3;
                } else {
                    iswork = petType.getId();
                }
                petMapper.insertSelective(new Pet(user, petType, iswork));
            }
        }
        la.add(1);
        return true;
    }

    /**
     * 宠物蛋孵化
     *
     * @param req
     * @return
     */
    @Override
    public boolean petPumping(MyRequest req) {
        User user = req.getUser();
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

        petMapper.insertSelective(new Pet(user, petType, 0));

        return false;
    }

    /**
     * 宠物升级
     */
    @Override
    public MapBody petUpLev(MyRequest req) {
        MapBody rt = new MapBody();
        User user = req.getUser();
        if (user == null) {
            rt.setState(ResponseMsg.MSG_ERR);
        }
        //解析msg
        PetUpLevMsg petUpLevMsg = sendMsgUtil.deserialize(req.getMsg(), PetUpLevMsg.class);
        //获取要升级的宠物
        FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, petUpLevMsg.getPetUid(), redisObjectUtil);

        //当前等级
        Integer level = fightingPet.getPet().getLevel();

        //获取升级所需经验
        String levelUpExpKey = RedisKeyUtil.getKey(RedisKey.LEVEL_UP_EXP, level.toString());
        LevelUpExp levelUpExp = redisObjectUtil.get(levelUpExpKey, LevelUpExp.class);
        //玩家拥有的经验
        Long petExp = user.getCurrency().getPetExp();

        if (levelUpExp.getUpLevExp() > petExp) {
            //经验不足不能升级 ，1
            rt.setState(ResponseMsg.MSG_ERR);
            rt.put("errMsg", 1);
            return rt;
        }


        //忠诚等级
        Integer loyaltyLev = fightingPet.getPet().getLoyaltyLev();
        //获取忠诚等级关系
        String levLoyaltyKey = RedisKeyUtil.getKey(RedisKey.LEV_LOYALTY, loyaltyLev.toString());
        LevLoyalty levLoyalty = redisObjectUtil.get(levLoyaltyKey, LevLoyalty.class);

        if (level >= levLoyalty.getLevelMax()) {
            //忠诚等级不足 ，2
            rt.setState(ResponseMsg.MSG_ERR);
            rt.put("errMsg", 2);
            return rt;
        }

        //升级
        fightingPet.upLevel();
        //减掉经验,保存
        user.getCurrency().setPetExp(petExp - levelUpExp.getUpLevExp());
        redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.USER, user.getUid()), user);

        //修改升级所需经验
        LevelUpExp nextLevelUpExp = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.LEVEL_UP_EXP, Integer.toString(level + 1)), LevelUpExp.class);
        fightingPet.getPet().setLevUpExp(nextLevelUpExp.getUpLevExp());
        fightingPet.save();


        return rt;
    }


    /**
     * 奖励分发，根据type获取不同奖励模式
     *
     * @param user
     * @param type
     * @return
     */
    @Override
    public boolean rewards(User user, int type) {
        return rewardService.rewards(user, type);
    }


    /**
     * 获取道具
     *
     * @param user
     * @param propInstenceUid
     * @return
     */
    private Prop getProp(User user, String propInstenceUid) {

        List<Prop> props = user.propList();

        if (props != null && props.size() > 0) {
            for (Prop propTrue : props) {
                if (propTrue.getPropInstenceUid().equals(propInstenceUid)) {
                    return propTrue;
                }
            }
        }
        return null;
    }
}
