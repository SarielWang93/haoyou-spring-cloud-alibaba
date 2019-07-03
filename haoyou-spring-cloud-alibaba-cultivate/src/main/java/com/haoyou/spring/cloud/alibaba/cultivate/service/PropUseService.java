package com.haoyou.spring.cloud.alibaba.cultivate.service;


import com.haoyou.spring.cloud.alibaba.cultivate.msg.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle.PeopUseHandle;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @Author: wanghui
 * @Date: 2019/5/23 15.52
 * @Version 1.0
 */
@Service
public class PropUseService {

    private static HashMap<String, PeopUseHandle> propUseHandleMap = new HashMap<>();

    public static void register(PeopUseHandle peopUseHandle){
        propUseHandleMap.put(peopUseHandle.getHandleType(),peopUseHandle);
    }


    public boolean propUse(PropUseMsg propUseMsg) {
        return propUseHandleMap.get(propUseMsg.getProp().getName()).useProp(propUseMsg);
    }
}
