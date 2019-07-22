package com.haoyou.spring.cloud.alibaba.cultivate.service;

import com.haoyou.spring.cloud.alibaba.cultivate.currency.use.handle.CurrencyUseHandle;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.CyrrencyUseMsg;
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
public class CurrencyUseService {

    private static HashMap<Integer, CurrencyUseHandle> CurrencyUseHandleMap = new HashMap<>();

    public static void register(CurrencyUseHandle currencyUseHandle){
        CurrencyUseHandleMap.put(currencyUseHandle.getHandleType(),currencyUseHandle);
    }

    public int currencyUse(CyrrencyUseMsg cyrrencyUseMsg) {
        return CurrencyUseHandleMap.get(cyrrencyUseMsg.getType()).currencyUse(cyrrencyUseMsg);
    }


}
