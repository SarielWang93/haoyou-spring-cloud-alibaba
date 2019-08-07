package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Commodity;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取用户邮件信息
 */
@Service
public class GetLifetimeBreederHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetLifetimeBreederHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_LIFETIME_BREEDER;
    }

    @Override
    public BaseMessage handle(MyRequest req) {

        MapBody mapBody = new MapBody<>();

        User user = req.getUser();

        Award upAward = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid(), RedisKey.LIFETIME_BREEDER), Award.class);

        if (upAward != null) {
            mapBody.put("type", RedisKey.LIFETIME_BREEDER);
            mapBody.put("award", upAward);
        } else {
            mapBody.put("award", redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, RedisKey.LIFETIME_BREEDER), Award.class));
        }


        //商品对象，饲养员特供包
        String storeName = "LifetimeBreeder";
        String commodityName = "BreederSpecialGiftPackage";
        Commodity commodity = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.COMMODITY, storeName, commodityName), Commodity.class);

        Map<String, Object> commodityMap = new HashMap<>();
        //商店名称
        commodityMap.put("storeName", storeName);
        //商品名称
        commodityMap.put("name", commodityName);
        //介绍
        commodityMap.put("description", commodity.getDescription());
        //货币类型
        commodityMap.put("spendType", commodity.getSpendType());
        //价格
        commodityMap.put("price", commodity.getPrice());
        //商品奖励
        commodityMap.put("award", redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, commodity.getAwardType()), Award.class));

        if(commodity.getRefreshTimes() != -1){
            String numericalName = String.format("commodity_%s", commodity.getName());

            Long buyCount = user.getUserNumericalMap().get("numericalName").getValue();
            //商品可买次数
            commodityMap.put("canBuyCount", commodity.getRefreshTimes()-buyCount);
        }


        mapBody.put("commodity",commodityMap);
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }

}
