package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SkillType;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape.Tetromino;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/5 10:48
 * <p>
 * 技能碎片合成
 */
@Service
public class PetSkillScrapHandle extends PeopUseHandle {


    @Override
    protected void setHandleType() {
        this.handleType = "PetSkillScrap";
    }

    @Override
    public MapBody handle(PropUseMsg propUseMsg) {

        MapBody rt = new MapBody();

        User user = propUseMsg.getUser();
        Prop prop = propUseMsg.getProp();
        int propCount = propUseMsg.getPropCount();

        //校验容量
        Integer propMax = user.getCurrency().getPropMax();
        int scount = 0;
        for (Prop propHas : user.propList()) {
            if ("PetSkill".equals(propHas.getName())) {
                scount++;
            }
        }
        if (scount >= propMax) {
            rt.setState(NO_SPACE);
            return rt;
        }



        //碎片需要数量
        Integer quality = Integer.valueOf(prop.getProperty1());
        int count = Integer.MAX_VALUE;

        if (quality < 5) {
            count = quality * 10;
        } else if (quality == 5) {
            count = 100;
        }


        if (count > propCount) {
            rt.setState(WRONG_COUNT);
            return rt;
        }


        String tetromino = prop.getProperty2();

        if (StrUtil.isEmpty(tetromino)) {
            tetromino = Tetromino.randomOne().getType();
        }


        HashMap<String, Skill> skills = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.SKILL), Skill.class);
        List<Skill> skillPool = new ArrayList<>();
        for (Skill skill : skills.values()) {
            if (skill.getQuality().equals(quality)
                    && (SkillType.OVERALL == skill.getType()
                        || SkillType.OPENING == skill.getType()
                        || SkillType.ATTACK_PASSIVE == skill.getType())) {
                skillPool.add(skill);
            }
        }
        //权重随机
        WeightRandom.WeightObj<Skill>[] weightObjs = new WeightRandom.WeightObj[skillPool.size()];
        for (int i = 0; i < skillPool.size(); i++) {
            weightObjs[i] = new WeightRandom.WeightObj(skillPool.get(i), 10d);
        }
        WeightRandom<Skill> petTypeWeightRandom = RandomUtil.weightRandom(weightObjs);
        Skill skill = petTypeWeightRandom.next();
        if(skill == null){
            rt.setState(ResponseMsg.MSG_ERR);
            return rt;
        }

        Prop prop1 = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PROP, "PetSkill"), Prop.class);
        prop1.setProperty1(skill.getL10n());
        prop1.setProperty2(tetromino);
        prop1.setProperty3(quality.toString());
        prop1.setProperty4(skill.getUid());
        prop1.setProperty5(skill.getDescribe());
        UserUtil.addProp(user, prop1);

        List<Prop> propList = new ArrayList<>();
        propList.add(prop1);
        Award award = new Award();
        award.setPropsList(propList);
        rt.put("award", award);
        rt.setState(ResponseMsg.MSG_SUCCESS);
        return rt;

    }
}
