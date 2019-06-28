package com.haoyou.spring.cloud.alibaba.service.cultivate;

import com.haoyou.spring.cloud.alibaba.commons.domain.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;

/**
 * @Author: wanghui
 * @Date: 2019/5/13 11:36
 * @Version 1.0
 * 养成系统服务
 */
public interface CultivateService {

    /**
     * 技能配置系统
     */
    boolean skillConfig(MyRequest req);

    /**
     * 宠物生成（临时）
     */
    boolean petGeneration(MyRequest req);

    /**
     * 宠物抽卡
     * @param req
     * @return
     */
    boolean petPumping (MyRequest req);

    /**
     * 宠物升级
     */
    MapBody petUpLev(MyRequest req);

    /**
     * 奖励获取
     */
    boolean rewards(User user,int type);

}
