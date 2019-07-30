package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Numerical;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.entity.UserNumerical;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/30 10:42
 * <p>
 * 每日任务清除奖励
 */
@Service
public class DailyTaskAwardRemoveHandle extends SettleHandle {


    @Override
    public void handle() {
        HashMap<String, Award> stringAwardHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.USER_AWARD), Award.class);
        for(Map.Entry<String, Award> entry : stringAwardHashMap.entrySet()){
            String key = entry.getKey();
            String[] split = key.split(":");
            if(RedisKey.DAILY_TASK.equals(split[2])){
                redisObjectUtil.delete(key);
            }
        }
    }

    @Override
    public boolean chackDate() {
        int hour = this.date.hour(true);
        return hour == 0;
    }


}
