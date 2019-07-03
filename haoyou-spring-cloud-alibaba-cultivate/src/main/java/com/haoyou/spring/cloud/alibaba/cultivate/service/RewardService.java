package com.haoyou.spring.cloud.alibaba.cultivate.service;


import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.cultivate.reward.handle.RewardHandle;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @Author: wanghui
 * @Date: 2019/5/23 15.52
 * @Version 1.0
 */
@Service
public class RewardService {

    private static HashMap<Integer, RewardHandle> rewardHandleMap = new HashMap<>();

    public static void register(RewardHandle rewardHandle){
        rewardHandleMap.put(rewardHandle.getHandleType(),rewardHandle);
    }


    public boolean rewards(User user, int type) {
        return rewardHandleMap.get(type).reward(user);
    }
}
