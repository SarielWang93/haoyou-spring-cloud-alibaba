package com.haoyou.spring.cloud.alibaba.cultivate.service;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Commodity;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.currency.use.handle.CurrencyUseHandle;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.CyrrencyUseMsg;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/22 10:04
 *
 * 商品购买操作
 *
 */
@Service
public class CommodityBuyService {

    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private SendMsgUtil sendMsgUtil;
    @Autowired
    private RewardService rewardService;
    @Autowired
    private UserUtil userUtil;
    @Autowired
    private NumericalService numericalService;


    public boolean commodityBuy(User user, Commodity commodity) {


        if(commodity.getRefreshTimes() != -1){
            //检查使用上限
            String numericalName = String.format("commodity_%s", commodity.getName());
            Long count = user.getUserNumericalMap().get(numericalName).getValue();
            if(count >= commodity.getRefreshTimes()){
                return false;
            }
            //添加购买次数
            numericalService.numericalAdd(user,numericalName,1L);
        }

        String awardType = commodity.getAwardType();
        rewardService.doAward(user,rewardService.getAward(awardType));


        return true;
    }

    public Commodity getCommodity(String storeName,String commodityName){
        return redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.COMMODITY,storeName,commodityName),Commodity.class);
    }


}
