package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Fund;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/30 10:42
 * <p>
 * 基金每日奖励刷新
 */
@Service
public class FundsSettleHandle extends SettleHandle {


    @Override
    public void handle() {

        for (User user : this.users) {

            TreeMap<Date, Fund> funds = userUtil.getFunds(user);
            for (Map.Entry<Date, Fund> entry : funds.entrySet()) {

                Date key = entry.getKey();
                Fund fund = entry.getValue();

                //已发放使用天数
                long l = DateUtil.betweenDay(key, this.date, true);

                //前一天的奖励结算
                if(l <= fund.getDays()){
                    String type = RedisKeyUtil.getKey(RedisKey.FUNDS,fund.getName());
                    Award upAward = rewardService.getUpAward(user.getUid(), type);
                    Award award = rewardService.getAward(fund.getAwardType());

                    //未领取则发送邮件
                    if(upAward == null || !upAward.isUsed()){
                        emailService.sendEmail(user.getUid(),fund.getL10n(),fund.getDescription(),award);
                    }
                    rewardService.deleteUpAward(user.getUid(),type);

                    //未发放完毕则继续发放
                    if(l < fund.getDays()){
                        rewardService.refreshUpAward(user.getUid(),award,type);
                    }
                }

            }

            userUtil.deleteFunds(user);

            userUtil.saveUser(user);
        }
    }

    @Override
    public boolean chackDate() {
        int hour = this.date.hour(true);
        return hour == 0;
    }


}
