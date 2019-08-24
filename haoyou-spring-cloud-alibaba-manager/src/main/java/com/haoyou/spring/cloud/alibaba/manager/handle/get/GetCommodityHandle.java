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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取商品信息
 */
@Service
public class GetCommodityHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetCommodityHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_COMMODITY;
    }

    @Override
    public BaseMessage handle(MyRequest req) {

        MapBody mapBody = new MapBody<>();

        User user = req.getUser();

        Map<String, Object> msgMap = this.getMsgMap(req);

        //商店名称
        String storeName = (String) msgMap.get("storeName");


        HashMap<String, Commodity> stringCommodityHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.COMMODITY, storeName), Commodity.class);

        List<Map> commodities = new ArrayList<>();

        for (Commodity commodity : stringCommodityHashMap.values()) {
            if(commodity.getShelf().equals(1)){
                commodities.add(this.getCommodityMsg(user,commodity));
            }
        }


        mapBody.put("commodities", commodities);
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }


    public Map<String, Object> getCommodityMsg(User user, Commodity commodity) {
        Map<String, Object> commodityMsg = new HashMap<>();
        //商店名称
        commodityMsg.put("storeName", commodity.getStoreName());
        //商品名称
        commodityMsg.put("name", commodity.getName());
        //商品名称
        commodityMsg.put("l10n", commodity.getL10n());
        //介绍
        commodityMsg.put("description", commodity.getDescription());
        //货币类型
        commodityMsg.put("spendType", commodity.getSpendType());
        //价格
        commodityMsg.put("price", commodity.getPrice());
        //商品奖励
        commodityMsg.put("award", redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, commodity.getAwardType()), Award.class));

        if (commodity.getRefreshTimes() != -1) {
            String numericalName = String.format("commodity_%s", commodity.getName());

            Long buyCount = user.getUserNumericalMap().get(numericalName).getValue();
            //商品可买次数
            commodityMsg.put("canBuyCount", commodity.getRefreshTimes() - buyCount);
        }
        return commodityMsg;
    }

}
