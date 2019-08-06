package com.haoyou.spring.cloud.alibaba.cultivate.rmb.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.RMBUseMsg;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/8/5 16:35
 * 购买月卡
 */
@Service
public class MonthlyCardHandle extends RMBUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = MONTHLI_CARD;
    }

    @Override
    public MapBody handle(RMBUseMsg rmbUseMsg) {

        User user = rmbUseMsg.getUser();

        //立得奖励
        Award monthly_card = rewardService.getAward("monthly_card");

        rewardService.doAward(user,monthly_card);
        //首次奖励
        if(user.getUserData().getMonthlyCardDate() == null){
            rewardService.doAward(user,rewardService.getAward("monthly_card_first"));
        }
        user.getUserData().setMonthlyCardDate(new Date());

        //每日奖励
        Award award = null;
        for(int i = 1;;i++){
            Award award1 = rewardService.getAward(String.format("monthly_card_daily_%s", i));
            if(award1 != null){
                award = award1;
            }else{
                break;
            }
        }
        String monthlyCardType = RedisKeyUtil.getKey(RedisKey.MONTHLY_CARD,award.getType());
        rewardService.upAward(user.getUid(),award,monthlyCardType);
        user.getUserData().setMonthlyCardAward(award.getType());

        this.save(user);


        MapBody mapBody = new MapBody();
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }
}
