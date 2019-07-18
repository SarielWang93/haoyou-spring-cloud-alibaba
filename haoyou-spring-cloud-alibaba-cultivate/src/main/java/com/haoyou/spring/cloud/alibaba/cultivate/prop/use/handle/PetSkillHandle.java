package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.Skill;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape.Tetromino;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
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
    public int handle(PropUseMsg propUseMsg) {

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
            return WRONG_PRO;
        }


        Prop prop1 = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PROP, "PetSkillScrap"), Prop.class);
        prop1.setProperty1("4");
        prop1.setCount(count);
        user.addProp(prop1);

        return ResponseMsg.MSG_SUCCESS;
    }
}
