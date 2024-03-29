package com.haoyou.spring.cloud.alibaba.cultivate.service;


import cn.hutool.core.lang.Console;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle.PeopUseHandle;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @Author: wanghui
 * @Date: 2019/5/23 15.52
 * @Version 1.0
 * 道具使用
 */
@Service
public class PropUseService {

    private static HashMap<String, PeopUseHandle> propUseHandleMap = new HashMap<>();

    public static void register(PeopUseHandle peopUseHandle){
        propUseHandleMap.put(peopUseHandle.getHandleType(),peopUseHandle);
    }

    public MapBody propUse(PropUseMsg propUseMsg) {
        return propUseHandleMap.get(propUseMsg.getProp().getName()).useProp(propUseMsg);
    }
}
