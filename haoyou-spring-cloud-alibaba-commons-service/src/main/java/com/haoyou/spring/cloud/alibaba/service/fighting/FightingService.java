package com.haoyou.spring.cloud.alibaba.service.fighting;

import com.haoyou.spring.cloud.alibaba.commons.domain.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;

import java.util.List;

/**
 * 调用manager服务
 */

public interface FightingService {


    boolean start(List<User> users);


    MapBody receiveFightingMsg(MyRequest req);


    boolean newFightingPet(Pet pet);


    boolean deleteFightingPet(Pet pet);


    MapBody getFightingPet(Pet pet);

}
