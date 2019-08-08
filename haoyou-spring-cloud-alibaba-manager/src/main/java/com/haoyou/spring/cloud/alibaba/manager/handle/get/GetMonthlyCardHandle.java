package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.date.DateUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.pojo.bean.DailyCheckIn;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 获取用户邮件信息
 */
@Service
public class GetMonthlyCardHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetMonthlyCardHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_MONTHLT_CARD;
    }

    @Override
    public BaseMessage handle(MyRequest req) {

        MapBody mapBody = new MapBody<>();

        User user = req.getUser();

        //立得奖励
        mapBody.put("monthlyCard",getAward("monthly_card"));
        //首次奖励
        Award monthly_card_first = getAward("monthly_card_first");
        if(user.getUserData().getMonthlyCardDate()!=null){
            monthly_card_first.setUsed(true);
        }
        mapBody.put("monthlyCardFirst",monthly_card_first);
        //每日奖励
        Award award = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid(), RedisKey.MONTHLY_CARD, user.getUserData().getMonthlyCardAward()), Award.class);
        if(award != null){
            //已发放奖励的天数
            long l = DateUtil.betweenDay(user.getUserData().getMonthlyCardDate(), new Date(), true);
            //剩余天数
            mapBody.put("monthlyCardDate",29-l);
            //当天奖励
            mapBody.put("monthlyCardDaily",award);
            if(!award.isUsed()){
                mapBody.put("monthlyCardDailyType",RedisKeyUtil.getKey(RedisKey.MONTHLY_CARD, award.getType()));
            }

        }else{
            for(int i = 1;;i++){
                Award award1 = this.getAward(String.format("monthly_card_daily_%s", i));
                if(award1 != null){
                    award = award1;
                }else{
                    break;
                }
            }
            //当天奖励
            mapBody.put("monthlyCardDaily",award);
        }


        //立得奖励
        mapBody.put("monthlyCardExtreme",getAward("monthly_card_extreme"));
        //本次奖励
        mapBody.put("monthlyCardExtremeFirst",getAward("monthly_card_extreme_first"));
        //每日奖励
        Award award2 = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid(), RedisKey.MONTHLY_CARD_EXTREME, user.getUserData().getMonthlyCardExtremeAward()), Award.class);
        if(award2 != null){
            //已发放奖励的天数
            long l = DateUtil.betweenDay(user.getUserData().getMonthlyCardExtremeDate(), new Date(), true);
            //剩余天数
            mapBody.put("monthlyCardExtremeDate",29-l);
            //当天奖励
            mapBody.put("monthlyCardExtremeDaily",award2);
        }else{
            for(int i = 1;;i++){
                Award award1 = this.getAward(String.format("monthly_card_extreme_daily_%s", i));
                if(award1 != null){
                    award2 = award1;
                }else{
                    break;
                }
            }
            //当天奖励
            mapBody.put("monthlyCardExtremeDaily",award2);
        }





        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }

    public Award getAward(String type){
        Award award =  redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, type),Award.class);
        return award;
    }

}
