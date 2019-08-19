package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import cn.hutool.core.date.DateUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Fund;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/30 10:42
 * <p>
 * 每个小时进行体力回复
 */
@Service
public class VitalityRestoreSettleHandle extends SettleHandle {


    @Override
    public void handle() {
        for (User user : this.users) {

            Integer vitality = user.getCurrency().getVitality();

            if(vitality < UserUtil.vitalityMaxCount){
                vitality += 10;
            }
            if(vitality > UserUtil.vitalityMaxCount){
                vitality = UserUtil.vitalityMaxCount;
            }
            userUtil.getUserByUid(user.getUid());
            user.getCurrency().setVitality(vitality);
            userUtil.saveUser(user);
        }
    }

    @Override
    public boolean chackDate() {
        return true;
    }


}
