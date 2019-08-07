package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Fund;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/30 10:42
 * <p>
 * 基金每日奖励刷新
 */
@Service
public class LifetimeBreederSettleHandle extends SettleHandle {


    @Override
    public void handle() {
        for (User user : this.users) {
            if(user.getUserData().getLifetimeBreederDate() != null){

                Award upAward = rewardService.getUpAward(user.getUid(), RedisKey.LIFETIME_BREEDER);

                if(upAward == null || upAward.isUsed()){
                    //发放每日奖励
                    Award award = rewardService.getAward(RedisKey.LIFETIME_BREEDER);
                    rewardService.refreshUpAward(user.getUid(),award,RedisKey.LIFETIME_BREEDER);
                }
            }
        }
    }

    @Override
    public boolean chackDate() {
        int hour = this.date.hour(true);
        return hour == 0;
    }


}
