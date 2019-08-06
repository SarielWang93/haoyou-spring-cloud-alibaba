package com.haoyou.spring.cloud.alibaba.cultivate.numerical;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.DailyTask;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.entity.UserNumerical;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/30 14:23
 * 每日任务
 */
@Service
public class DailyTaskCheck extends NumericalCheck {
    @Override
    public void check(User user, String numericalName, long addValue) {

        UserNumerical userNumerical = user.getUserNumericalMap().get(numericalName);


        HashMap<String, DailyTask> stringDailyTaskHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.DAILY_TASK), DailyTask.class);

        for(DailyTask dailyTask:stringDailyTaskHashMap.values()){
            if(dailyTask.getNumericalName().equals(numericalName)){
                Long value = userNumerical.getValue();
                Long aim = dailyTask.getAim();
                if(value+addValue>=aim){

                    String awardType = dailyTask.getAwardType();
                    String type = RedisKeyUtil.getKey(RedisKey.DAILY_TASK, dailyTask.getName());

                    rewardService.refreshUpAward(user.getUid(),rewardService.getAward(awardType),type);

                    if(dailyTask.getIntegral() > 0){
                        numericalService.numericalAdd(user,"daily_task_integral",dailyTask.getIntegral());
                    }

                }
            }
        }
    }
}
