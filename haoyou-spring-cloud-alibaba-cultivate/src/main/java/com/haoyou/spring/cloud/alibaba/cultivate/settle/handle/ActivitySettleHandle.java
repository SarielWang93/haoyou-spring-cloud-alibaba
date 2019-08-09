package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Activity;
import com.haoyou.spring.cloud.alibaba.commons.entity.ActivityAward;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.bean.DailyCheckIn;
import org.springframework.stereotype.Service;

import java.util.Date;
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
public class ActivitySettleHandle extends SettleHandle {


    @Override
    public void handle() {


        HashMap<String, Activity> stringActivityHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.ACTIVITY), Activity.class);
        //遍历所有的活动
        for (Map.Entry<String, Activity> entry:stringActivityHashMap.entrySet()) {
            Activity activity = entry.getValue();
            //当前启用的活动
            if (activity.isCurrent()) {
                Integer refresh = activity.getRefresh();
                //是否到刷新的天数
                if (isRefresh(refresh)) {
                    //删除所有相关奖励
                    for (User user : this.users) {
                        for(ActivityAward activityAward: activity.getActivityAwards()){
                            String type = RedisKeyUtil.getKey(RedisKey.ACTIVITY, activity.getActivityType(),activityAward.getAwardType());
                            rewardService.deleteUpAward(user.getUid(),type);
                        }
                    }
                    //关闭旧的活动
                    activity.setCurrent(false);
                    redisObjectUtil.save(entry.getKey(),activity);

                    //启用新的预设活动
                    HashMap<String, Activity> stringActivityHashMap1 = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.ACTIVITY, activity.getActivityType()), Activity.class);
                    for(Map.Entry<String, Activity> entry1 : stringActivityHashMap1.entrySet()){
                        Activity value = entry1.getValue();
                        if(value.getPresetEnabled() == 1){
                            value.setCurrent(true);
                        }
                        redisObjectUtil.save(entry1.getKey(),value);
                    }

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
