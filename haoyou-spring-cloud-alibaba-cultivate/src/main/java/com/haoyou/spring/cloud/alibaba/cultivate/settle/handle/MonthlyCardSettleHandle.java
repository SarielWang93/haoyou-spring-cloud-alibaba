package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import cn.hutool.core.date.DateUtil;
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

            boolean refreshUpAward = false;
            //发放月卡奖励
            if(user.getUserData().getMonthlyCardDate() != null){
                //已发放奖励的天数
                long l = DateUtil.betweenDay(user.getUserData().getMonthlyCardDate(), this.date, true);
                if(l<30){
                    //发放奖励
                    String monthlyCardAward = user.getUserData().getMonthlyCardAward();
                    String monthlyCardType = RedisKeyUtil.getKey(RedisKey.MONTHLY_CARD,monthlyCardAward);
                    Award award =  rewardService.getAward(monthlyCardAward);
                    refreshUpAward = rewardService.refreshUpAward(user.getUid(),award,monthlyCardType);
                }
            }
            //未发放奖励则清空奖励记录
            if(!refreshUpAward){
                redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.USER_AWARD,user.getUid(),RedisKey.MONTHLY_CARD));
            }


            refreshUpAward = false;
            //发放至尊月卡奖励
            if(user.getUserData().getMonthlyCardExtremeDate() != null){
                //已发放奖励的天数
                long l = DateUtil.betweenDay(user.getUserData().getMonthlyCardExtremeDate(), this.date, true);
                if(l<30) {
                    //发放奖励
                    String monthlyCardExtremeAward = user.getUserData().getMonthlyCardExtremeAward();
                    String monthlyCardExtremeType = RedisKeyUtil.getKey(RedisKey.MONTHLY_CARD_EXTREME, monthlyCardExtremeAward);
                    Award award = rewardService.getAward(monthlyCardExtremeAward);
                    rewardService.refreshUpAward(user.getUid(), award, monthlyCardExtremeType);
                }
            }
            //未发放奖励则清空奖励记录
            if(!refreshUpAward){
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
