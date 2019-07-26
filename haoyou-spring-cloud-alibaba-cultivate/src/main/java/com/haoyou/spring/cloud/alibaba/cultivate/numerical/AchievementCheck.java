package com.haoyou.spring.cloud.alibaba.cultivate.numerical;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/26 10:55
 * 成就奖励数值检测
 */
@Service
public class AchievementCheck extends NumericalCheck {
    @Override
    public void check(User user, String numericalName, long addValue) {
        //查找数值对应的成就
        HashMap<String, Achievement> stringAchievementHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.ACHIEVEMENT), Achievement.class);
        if(stringAchievementHashMap!=null){
            UserNumerical userNumerical = user.getUserNumericalMap().get(numericalName);
            for(Achievement achievement:stringAchievementHashMap.values()){
                if(achievement.getNumericalName().equals(numericalName)){
                    this.checkAchievement(achievement,userNumerical,addValue);
                }
            }
        }
    }

    /**
     * 数值查询
     * @param achievement
     * @param userNumerical
     */
    private void checkAchievement(Achievement achievement,UserNumerical userNumerical, long addValue){

        List<AchievementAims> achievementAims = achievement.getAchievementAims();
        Long oldvalue = userNumerical.getValue();

        Long newvalue = oldvalue + addValue;


        for(AchievementAims achievementAim : achievementAims){
            if(achievementAim.getAim() > oldvalue && achievementAim.getAim()<newvalue){

                Award award = rewardService.getAward(achievementAim.getAwardType());

                String type = RedisKeyUtil.getKey(RedisKey.ACHIEVEMENT,achievement.getName(),achievementAim.getPriorityOrder().toString());

                rewardService.upAward(userNumerical.getUserUid(),award,type);
            }
        }



    }



}
