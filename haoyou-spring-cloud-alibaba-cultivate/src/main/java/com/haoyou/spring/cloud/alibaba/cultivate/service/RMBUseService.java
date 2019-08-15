package com.haoyou.spring.cloud.alibaba.cultivate.service;

import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.cultivate.currency.use.handle.CurrencyUseHandle;
import com.haoyou.spring.cloud.alibaba.cultivate.rmb.use.handle.RMBUseHandle;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.CyrrencyUseMsg;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.RMBUseMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/22 10:04
 *
 * 财产使用
 *
 */
@Service
public class RMBUseService {

    private static HashMap<Integer, RMBUseHandle> RMBUseHandleMap = new HashMap<>();

    public static void register(RMBUseHandle rmbUseHandle){
        RMBUseHandleMap.put(rmbUseHandle.getHandleType(),rmbUseHandle);
    }

    @Autowired
    private NumericalService numericalService;



    public MapBody rmbUse(RMBUseMsg rmbUseMsg) {

        //累计充值
        numericalService.numericalAdd(rmbUseMsg.getUser(),"accumulated_recharge",rmbUseMsg.getRmb());
        //当日充值
        numericalService.numericalAdd(rmbUseMsg.getUser(),"daily_recharge",rmbUseMsg.getRmb());
        //充值总量
        numericalService.numericalAdd(rmbUseMsg.getUser(),"all_recharge",rmbUseMsg.getRmb());

        return RMBUseHandleMap.get(rmbUseMsg.getType()).rmbUse(rmbUseMsg);
    }

    //TODO 创建订单


}
