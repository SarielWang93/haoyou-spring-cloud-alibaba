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
 * 好友礼物清算，好友助战清算
 */
@Service
public class FriendGiftSettleHandle extends SettleHandle {


    @Override
    public void handle() {
        for(User user:this.users){
            String type = RedisKeyUtil.getlkKey(RedisKey.FRIENDS_GIFT);
            rewardService.deleteUpAwards(user.getUid(), type);


            String hashKey = RedisKeyUtil.getlkKey(RedisKey.HELP_PET, user.getUid(), RedisKey.HAS_HELP);
            redisObjectUtil.deleteAll(hashKey);
        }
    }

    @Override
    public boolean chackDate() {
        int hour = this.date.hour(true);
        return hour == 0;
    }


}
