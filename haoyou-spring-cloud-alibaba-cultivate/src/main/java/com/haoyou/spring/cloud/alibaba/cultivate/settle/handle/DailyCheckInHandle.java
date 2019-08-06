package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import cn.hutool.core.util.RandomUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.bean.DailyCheckIn;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/30 10:42
 * <p>
 * 每日签到刷新
 */
@Service
public class DailyCheckInHandle extends SettleHandle {



    @Override
    public void handle() {
        HashMap<String, User> userAllHive = userUtil.getUserAllCatch();

        for (Map.Entry<String, User> entry: userAllHive.entrySet()) {
            User user = entry.getValue();
            DailyCheckIn dailyCheckIn = userUtil.getDailyCheckIn(user);
            for(Award award:dailyCheckIn.getAwards()){
                if(!award.isUsed()){
                    String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid(),RedisKey.DAILY_CHECK_IN,award.getType());
                    Award award1 = redisObjectUtil.get(key, Award.class);
                    if(award1 != null && award1.isUsed()) {
                        award.setUsed(true);
                    }
                    break;
                }
            }

            if(dailyCheckIn.allUsed()){
                userUtil.setDailyCheckIn(user);
            }else{
                userUtil.setDailyCheckIn(user,dailyCheckIn);
            }
            user.setLastUpdateDate(new Date());
            redisObjectUtil.save(entry.getKey(),user);
        }
    }

    @Override
    public boolean chackDate() {
        int hour = this.date.hour(true);
        return hour == 0;
    }





}
