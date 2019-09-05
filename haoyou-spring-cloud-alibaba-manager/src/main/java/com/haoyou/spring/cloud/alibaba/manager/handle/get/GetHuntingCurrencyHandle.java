package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.collection.CollUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.HuntingAssociation;
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
import java.util.TreeMap;

/**
 * 获取狩猎协会数据
 */
@Service
public class GetHuntingCurrencyHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetHuntingCurrencyHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_HUNTING_ASSOCIATION;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        User user = req.getUser();
        MapBody mapBody = MapBody.beSuccess();

        //当前贡献
        Long huntingCurrency = user.getUserNumericalMap().get("hunting_currency").getValue();
        mapBody.put("huntingCurrency", huntingCurrency);

        //当前狩猎等级
        Long huntingCurrencyLevel = user.getUserNumericalMap().get("hunting_currency_level").getValue();
        mapBody.put("huntingCurrencyLevel", huntingCurrencyLevel);

        String huntingCurrencylkKeys = RedisKeyUtil.getlkKey(RedisKey.HUNTING_ASSOCIATION);
        HashMap<String, HuntingAssociation> stringHuntingAssociationHashMap = redisObjectUtil.getlkMap(huntingCurrencylkKeys, HuntingAssociation.class);

        TreeMap<Integer, HuntingAssociation> huntingAssociationTreeMap = new TreeMap<>();
        for (HuntingAssociation huntingAssociation : stringHuntingAssociationHashMap.values()) {
            if(huntingAssociation.getIdNum()<=huntingCurrencyLevel){
                huntingAssociationTreeMap.put(huntingAssociation.getIdNum(),huntingAssociation);
            }
        }

        ArrayList<HuntingAssociation> huntingAssociations = CollUtil.newArrayList(huntingAssociationTreeMap.values());
        //狩猎协会
        mapBody.put("huntingAssociations", huntingAssociations);

        return mapBody;
    }
}
