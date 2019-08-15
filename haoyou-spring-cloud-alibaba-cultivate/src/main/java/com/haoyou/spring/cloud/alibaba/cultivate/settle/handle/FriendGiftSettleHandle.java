package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Activity;
import com.haoyou.spring.cloud.alibaba.commons.entity.ActivityAward;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/30 10:42
 * <p>
 * 活动结算刷新
 */
@Service
public class FriendGiftSettleHandle extends SettleHandle {


    @Override
    public void handle() {
        for(User user:this.users){
            String type = RedisKeyUtil.getlkKey(RedisKey.FRIENDS_GIFT);
            rewardService.deleteUpAwards(user.getUid(), type);
        }
    }

    @Override
    public boolean chackDate() {
        int hour = this.date.hour(true);
        return hour == 0;
    }


}
