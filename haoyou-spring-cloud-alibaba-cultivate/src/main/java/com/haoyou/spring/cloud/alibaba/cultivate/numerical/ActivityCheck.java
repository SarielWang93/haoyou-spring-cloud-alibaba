package com.haoyou.spring.cloud.alibaba.cultivate.numerical;

import cn.hutool.core.date.DateUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/30 14:23
 * 每日任务
 */
@Service
public class ActivityCheck extends NumericalCheck {
    @Override
    public void check(User user, String numericalName, long addValue) {

        UserNumerical userNumerical = user.getUserNumericalMap().get(numericalName);


        HashMap<String, Activity> stringActivityHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.ACTIVITY), Activity.class);
        //遍历所有的活动
        for (Activity activity : stringActivityHashMap.values()) {
            //活动是否当前启用
            if(activity.isCurrent()){
                //单笔充值另外操作
                if ("SingleRecharge".equals(activity.getActivityType())) {
                    checkSingleRecharge(user, activity, userNumerical, addValue);
                } else {
                    //是否当前变化数值影响的活动
                    if (activity.getNumericalName().equals(numericalName)) {
                        //分type进行不同处理
                        String type = activity.getActivityType();

                        switch (type) {
                            case "DailyRecharge":
                                checkDailyRecharge(user, activity, userNumerical, addValue);
                                break;
                            case "AccumulatedRecharge":
                                checkAccumulatedRecharge(user, activity, userNumerical, addValue);
                                break;
                        }
                    }
                }
            }


        }
    }

    /**
     * 天天充值
     *
     * @param user
     * @param activity
     * @param userNumerical
     * @param addValue
     */
    private void checkDailyRecharge(User user, Activity activity, UserNumerical userNumerical, long addValue) {
        //根据进度排序
        List<ActivityAward> activityAwards = activity.getActivityAwards();

        //查找
        for (ActivityAward activityAward : activityAwards) {
            //大于目标值才考虑发放
            if (userNumerical.getValue() + addValue >= activityAward.getAim()) {
                String type = RedisKeyUtil.getKey(RedisKey.ACTIVITY, activity.getActivityType(),activityAward.getAwardType());
                //查找发放的奖励
                Award upAward = rewardService.getUpAward(user.getUid(), type);

                //先找到未发放则发放，先找到今天发放的则已发放
                if (upAward == null) {
                    rewardService.upAward(user.getUid(), rewardService.getAward(activityAward.getAwardType()), type);
                    break;
                } else {
                    if (DateUtil.betweenDay(upAward.getUpAwardDate(),new Date(),true) == 0) {
                        break;
                    }
                }
            }

        }


    }

    /**
     * 累计结算
     *
     * @param user
     * @param activity
     * @param userNumerical
     * @param addValue
     */
    private void checkAccumulatedRecharge(User user, Activity activity, UserNumerical userNumerical, long addValue) {

        for (ActivityAward activityAward : activity.getActivityAwards()) {
            //大于目标值才考虑发放
            if (userNumerical.getValue() + addValue >= activityAward.getAim()) {
                String type = RedisKeyUtil.getKey(RedisKey.ACTIVITY, activity.getActivityType(),activityAward.getAwardType());
                //查找发放的奖励
                Award upAward = rewardService.getUpAward(user.getUid(), type);

                if (upAward == null) {
                    rewardService.upAward(user.getUid(), rewardService.getAward(activityAward.getAwardType()), type);
                }
            }

        }

    }

    /**
     * 单笔充值操作
     *
     * @param user
     * @param activity
     * @param userNumerical
     * @param addValue
     */
    private void checkSingleRecharge(User user, Activity activity, UserNumerical userNumerical, long addValue) {
        String numericalName = userNumerical.getNumericalName();
        //购买充值商品时
        if (numericalName.startsWith("commodity_Recharge")) {

            long yuan = Long.parseLong(numericalName.replaceAll("commodity_Recharge", ""));

            for (ActivityAward activityAward : activity.getActivityAwards()) {
                if(yuan == activityAward.getAim()){
                    //次数不超过上限时
                    Integer times = activityAward.getTimes();
                    if(userNumerical.getValue()+addValue<=times){
                        String type = RedisKeyUtil.getKey(RedisKey.ACTIVITY, activity.getActivityType(),activityAward.getAwardType(),Long.toString(userNumerical.getValue()+addValue));
                        rewardService.refreshUpAward(user.getUid(), rewardService.getAward(activityAward.getAwardType()), type);
                    }
                }
            }
        }
    }
}
