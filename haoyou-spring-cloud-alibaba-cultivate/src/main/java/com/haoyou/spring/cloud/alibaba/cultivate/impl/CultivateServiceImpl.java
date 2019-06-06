package com.haoyou.spring.cloud.alibaba.cultivate.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Service;
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

    @Override
    public boolean petGeneration(MyRequest req) {
        logger.debug("注册赠送宠物！！！");
        User user = req.getUser();
        List<Integer> l = new ArrayList<>();
        if (la.longValue() % 2 == 0) {
            l.add(1);
            l.add(2);
            l.add(3);
        }else{
            l.add(4);
            l.add(5);
            l.add(6);
        }
        HashMap<String, PetType> stringPetTypeHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.PET_TYPE), PetType.class);

        for (PetType petType:stringPetTypeHashMap.values()) {
            if(l.contains(petType.getId())){
                Pet pet=new Pet();
                pet.setUid(IdUtil.simpleUUID());
                pet.setAtn(petType.getAtn());
                pet.setAtnGr(petType.getAtnGr());
                pet.setDef(petType.getDef());
                pet.setDefGr(petType.getDefGr());
                pet.setHp(petType.getHp());
                pet.setHpGr(petType.getHpGr());
                pet.setTypeUid(petType.getUid());
                pet.setUserUid(user.getUid());
                pet.setType(petType.getType());
                pet.setSpd(petType.getSpd());
                pet.setLuk(petType.getLuk());
                pet.setStarClass(petType.getStarClass());
                if(petType.getId()>3){
                    pet.setIswork(petType.getId()-3);
                }else{
                    pet.setIswork(petType.getId());
                }
                pet.setInhSkill(petType.getInhSkill());
                pet.setUniqueSkill(petType.getUniqueSkill());
                pet.setTalentSkill(petType.getTalentSkill());
                pet.setSpecialAttack(petType.getSpecialAttack());
                pet.setSkillBoardJosn(petType.getSkillBoardJosn());
                pet.setExp(0);
                pet.setLevUpExp(260);
                pet.setLevel(1);
                pet.setLoyalty(0);
                pet.setIngredients(0);
                pet.setNickname(petType.getL10n());
                pet.setCreatDate(new Date());

                petMapper.insertSelective(pet);
            }
        }
        la.add(1);
        return true;
    }



    @Override
    public boolean rewards(User user, int type) {
        return rewardService.rewards(user,type);
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
