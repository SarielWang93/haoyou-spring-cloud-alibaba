package com.haoyou.spring.cloud.alibaba.service.fighting;

import com.haoyou.spring.cloud.alibaba.commons.domain.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;

import java.util.List;
import java.util.Map;

/**
 * 调用manager服务
 */

public interface FightingService {


    boolean start(List<User> users, Map<String,Boolean> allIsAi, int rewardType,int fightingType);


    MapBody receiveFightingMsg(MyRequest req);

}
