package com.haoyou.spring.cloud.alibaba.cultivate.impl;

import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.dubbo.config.annotation.Service;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.PetType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetSkillMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
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
    private PetSkillMapper petSkillMapper;
    @Autowired
    private SkillConfigService skillConfigService;
    @Autowired
    private RewardService rewardService;

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
        Prop prop = skillConfigMsg.getProp();
        //验证道具
        if (checkProp(user, prop)) {
            switch (skillConfigMsg.getType()) {
                case ADD_PET_SKILL:
                    return skillConfigService.addPetSkill(user, skillConfigMsg);
                case REMOVE_PET_SKILL:
                    return skillConfigService.removePetSkill(user, skillConfigMsg);
            }
        }
        return false;
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
                int iswork ;
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
     * 校验道具
     *
     * @param user
     * @param prop
     * @return
     */
    private boolean checkProp(User user, Prop prop) {

        List<Prop> props = user.propList();

        if (props != null && props.size() > 0) {
            for (Prop propTrue : props) {
                if (propTrue.getPropInstenceUid().equals(prop.getPropInstenceUid())) {
                    if (propTrue.equals(prop)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
