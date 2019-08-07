package com.haoyou.spring.cloud.alibaba.manager.handle.get;


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
        mapBody.put("monthlyCardFirst",getAward("monthly_card_first"));
        //每日奖励
        HashMap<String, Award> monthlyCardAwards = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.USER_AWARD, user.getUid(), RedisKey.MONTHLY_CARD), Award.class);
        if(monthlyCardAwards.size()>0){
            mapBody.put("monthlyCardDate",user.getUserData().getMonthlyCardDate());
            for(Award award : monthlyCardAwards.values()){
                mapBody.put("monthlyCardDaily",award);
            }
        }else{
            Award award = null;
            for(int i = 1;;i++){
                Award award1 = this.getAward(String.format("monthly_card_daily_%s", i));
                if(award1 != null){
                    award = award1;
                }else{
                    break;
                }
            }
            mapBody.put("monthlyCardDaily",award);
        }


        //立得奖励
        mapBody.put("monthlyCardExtreme",getAward("monthly_card_extreme"));
        //本次奖励
        mapBody.put("monthlyCardExtremeFirst",getAward("monthly_card_extreme_first"));
        //每日奖励
        HashMap<String, Award> monthlyCardExtremeAwards = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.USER_AWARD, user.getUid(), RedisKey.MONTHLY_CARD_EXTREME), Award.class);
        if(monthlyCardExtremeAwards.size()>0){
            mapBody.put("monthlyCardExtremeDate",user.getUserData().getMonthlyCardExtremeDate());
            for(Award award : monthlyCardExtremeAwards.values()){
                mapBody.put("monthlyCardExtremeDaily",award);
            }
        }else{
            Award award = null;
            for(int i = 1;;i++){
                Award award1 = this.getAward(String.format("monthly_card_extreme_daily_%s", i));
                if(award1 != null){
                    award = award1;
                }else{
                    break;
                }
            }
            mapBody.put("monthlyCardExtremeDate",award);
        }





        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }

    public Award getAward(String type){
        Award award =  redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, type),Award.class);
        return award;
    }

}
