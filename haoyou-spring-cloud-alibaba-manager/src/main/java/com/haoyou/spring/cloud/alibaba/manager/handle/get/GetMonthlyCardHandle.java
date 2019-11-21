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

import java.util.*;

/**
 * 月卡
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

        List<Map> monthlyCardMsg = new ArrayList<>();

        Map<String,Object> monthlyCard = new HashMap<>();


        //立得奖励
        monthlyCard.put("award",getAward("monthly_card"));
        //首次奖励
        Award monthly_card_first = getAward("monthly_card_first");
        if(user.getUserData().getMonthlyCardDate()!=null){
            monthly_card_first.setUsed(true);
        }
        monthlyCard.put("first",monthly_card_first);
        //每日奖励
        Award award = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid(), RedisKey.MONTHLY_CARD, user.getUserData().getMonthlyCardAward()), Award.class);
        if(award != null){
            //已发放奖励的天数
            long l = DateUtil.betweenDay(user.getUserData().getMonthlyCardDate(), new Date(), true);
            //剩余天数
            monthlyCard.put("date",29-l);
            //当天奖励
            monthlyCard.put("daily",award);
            if(!award.isUsed()){
                monthlyCard.put("dailyType",RedisKeyUtil.getKey(RedisKey.MONTHLY_CARD, award.getType()));
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
            monthlyCard.put("daily",award);
            //剩余天数
            monthlyCard.put("date",-1);
        }
        monthlyCardMsg.add(monthlyCard);


        Map<String,Object> monthlyCardExtreme = new HashMap<>();
        //立得奖励
        monthlyCardExtreme.put("award",getAward("monthly_card_extreme"));

        Award monthly_card_extreme_once = getAward("monthly_card_extreme_once");
        //每日奖励
        Award award2 = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid(), RedisKey.MONTHLY_CARD_EXTREME, user.getUserData().getMonthlyCardExtremeAward()), Award.class);
        if(award2 != null){
            monthly_card_extreme_once.setUsed(true);
            //已发放奖励的天数
            long l = DateUtil.betweenDay(user.getUserData().getMonthlyCardExtremeDate(), new Date(), true);
            //剩余天数
            monthlyCardExtreme.put("date",29-l);
            //当天奖励
            monthlyCardExtreme.put("daily",award2);
            if(!award2.isUsed()){
                monthlyCardExtreme.put("dailyType",RedisKeyUtil.getKey(RedisKey.MONTHLY_CARD_EXTREME, award2.getType()));
            }
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
            monthlyCardExtreme.put("daily",award2);
            //剩余天数
            monthlyCardExtreme.put("date",-1);
        }

        //本次奖励
        monthlyCardExtreme.put("once",monthly_card_extreme_once);

        monthlyCardMsg.add(monthlyCardExtreme);




        mapBody.put("monthlyCardMsg",monthlyCardMsg);
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }

    public Award getAward(String type){
        Award award =  redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, type),Award.class);
        return award;
    }

}
