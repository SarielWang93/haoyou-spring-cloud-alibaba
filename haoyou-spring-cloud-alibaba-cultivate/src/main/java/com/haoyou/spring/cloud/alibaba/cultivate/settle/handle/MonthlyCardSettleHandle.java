package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.bean.DailyCheckIn;
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
 * 月卡每日奖励刷新
 */
@Service
public class MonthlyCardSettleHandle extends SettleHandle {



    @Override
    public void handle() {


        for (User user: this.users) {
            //发放月卡奖励
            if(user.getUserData().getMonthlyCardDate() != null
                    && (new Date().getTime()-user.getUserData().getMonthlyCardDate().getTime())<60L*60L*24L*30L*1000L){

                String monthlyCardAward = user.getUserData().getMonthlyCardAward();
                String monthlyCardType = RedisKeyUtil.getKey(RedisKey.MONTHLY_CARD,monthlyCardAward);
                Award award =  rewardService.getAward(monthlyCardAward);
                rewardService.refreshUpAward(user.getUid(),award,monthlyCardType);
            }else{
                redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.USER_AWARD,user.getUid(),RedisKey.MONTHLY_CARD));
            }



            //发放至尊月卡奖励
            if(user.getUserData().getMonthlyCardExtremeDate() != null
                    && (new Date().getTime()-user.getUserData().getMonthlyCardExtremeDate().getTime())<60L*60L*24L*30L*1000L){

                String monthlyCardExtremeAward = user.getUserData().getMonthlyCardExtremeAward();
                String monthlyCardExtremeType = RedisKeyUtil.getKey(RedisKey.MONTHLY_CARD_EXTREME,monthlyCardExtremeAward);
                Award award =  rewardService.getAward(monthlyCardExtremeAward);
                rewardService.refreshUpAward(user.getUid(),award,monthlyCardExtremeType);
            }else{
                redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.USER_AWARD,user.getUid(),RedisKey.MONTHLY_CARD_EXTREME));
            }

        }
    }

    @Override
    public boolean chackDate() {
        int hour = this.date.hour(true);
        return hour == 0;
    }





}
