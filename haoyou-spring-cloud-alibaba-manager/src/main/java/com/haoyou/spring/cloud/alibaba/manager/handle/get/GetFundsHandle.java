package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Fund;
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
 * 获取用户邮件信息
 */
@Service
public class GetFundsHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetFundsHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_FUNDS;
    }

    @Override
    public BaseMessage handle(MyRequest req) {

        MapBody mapBody = new MapBody<>();

        Date now = new Date();

        User user = req.getUser();

        TreeMap<Date, Fund> funds = userUtil.getFunds(user);


        HashMap<String, Fund> stringFundHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.FUNDS), Fund.class);


        //已购买的基金信息
        List<Map> hasBuyFunds = new ArrayList<>();
        for (Map.Entry<Date, Fund> entry : funds.entrySet()) {
            Date buyDate = entry.getKey();
            Fund fund = entry.getValue();

            String stringFundHashMapKey = RedisKeyUtil.getKey(RedisKey.FUNDS,fund.getName());
            stringFundHashMap.remove(stringFundHashMapKey);


            String type = RedisKeyUtil.getKey(RedisKey.FUNDS,fund.getName());
            String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid(), type);
            Award upAward = redisObjectUtil.get(key, Award.class);

            if(upAward!=null){

                Map<String,Object> fundMap = new HashMap<>();

                //基金信息
                fundMap.put("fund",fund);
                //奖励信息
                fundMap.put("award",upAward);
                //奖励领取type
                fundMap.put("type",type);

                DateTime dateTime = DateUtil.offsetDay(buyDate, fund.getDays()-1);

                long l = DateUtil.betweenDay(dateTime, now , false);

                fundMap.put("days",l);

                hasBuyFunds.add(fundMap);
            }

        }

        //未购买的基金
        List<Map> notBuyFunds = new ArrayList<>();
        for (Fund fund : stringFundHashMap.values()){

            Date overTime = fund.getOverTime();

            if(overTime.getTime() > now.getTime()){
                Map<String,Object> fundMap = new HashMap<>();

                Award award = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, fund.getAwardType()), Award.class);

                //基金信息
                fundMap.put("fund",fund);
                //奖励信息
                fundMap.put("award",award);

                notBuyFunds.add(fundMap);
            }

        }
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }

}
