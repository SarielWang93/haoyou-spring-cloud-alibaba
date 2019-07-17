package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.PetType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/5 10:48
 *
 * 宠物蛋孵化类
 */
@Service
public class PetSkillScrapHandle extends PeopUseHandle {

    //数量不对
    final static public int WRONG_COUNT = 1002;

    @Override
    protected void setHandleType() {
        this.handleType = "PetSkillScrap";
    }

    @Override
    public int handle(PropUseMsg propUseMsg) {

        User user = propUseMsg.getUser();
        Prop prop = propUseMsg.getProp();
        int propCount = propUseMsg.getPropCount();


        //碎片需要数量
        int count = 40;

        if(count > propCount){
            return WRONG_COUNT;
        }


        Prop prop1 = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PROP, "PetSkill"), Prop.class);

        prop1.setProperty1(prop.getProperty1());
        prop1.setProperty2(prop.getProperty2());
        prop1.setProperty3(prop.getProperty3());
        prop1.setProperty4(prop.getProperty4());
        prop1.setProperty5(prop.getProperty5());

        user.addProp(prop1);


        return ResponseMsg.MSG_SUCCESS;
    }
}
