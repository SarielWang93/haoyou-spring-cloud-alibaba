package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.Skill;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
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
 * 技能道具分解
 */
@Service
public class PetSkillHandle extends PeopUseHandle {


    @Override
    protected void setHandleType() {
        this.handleType = "PetSkill";
    }

    @Override
    public MapBody handle(PropUseMsg propUseMsg) {

        MapBody rt = new MapBody();

        User user = propUseMsg.getUser();
        Prop prop = propUseMsg.getProp();

        Integer quality = Integer.valueOf(prop.getProperty3());

        int count = 0;

        switch (quality) {
            case 1:
                count = 1;
                break;
            case 2:
                count = 2;
                break;
            case 3:
                count = 5;
                break;
            case 4:
                count = 10;
                break;
            case 5:
                count = 20;
                break;
        }

        if (count == 0) {
            rt.setState(WRONG_PRO);
            return rt;
        }


        Prop prop1 = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PROP, "PetSkillScrap"), Prop.class);
        prop1.setProperty1("4");
        prop1.setCount(count);
        userUtil.addProp(user,prop1);

        List<Prop> propList = new ArrayList<>();
        propList.add(prop1);
        Award award = new Award();
        award.setPropsList(propList);
        rt.put("award",award);
        rt.setState(ResponseMsg.MSG_SUCCESS);
        return rt;
    }
}
