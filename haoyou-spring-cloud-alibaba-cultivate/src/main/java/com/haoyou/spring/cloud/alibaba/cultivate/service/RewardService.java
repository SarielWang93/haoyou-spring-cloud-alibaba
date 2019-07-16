package com.haoyou.spring.cloud.alibaba.cultivate.service;


import cn.hutool.core.util.IdUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.reward.handle.RewardHandle;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @Author: wanghui
 * @Date: 2019/5/23 15.52
 * @Version 1.0
 */
@Service
public class RewardService {

    private static HashMap<String, RewardHandle> rewardHandleMap = new HashMap<>();

    public static void register(RewardHandle rewardHandle){
        rewardHandleMap.put(rewardHandle.getHandleType(),rewardHandle);
    }

    @Autowired
    protected RedisObjectUtil redisObjectUtil;
    @Autowired
    protected SendMsgUtil sendMsgUtil;



    public boolean rewards(User user, String type) {

        Award award =redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, type),Award.class);
        if(award == null){
            award = rewardHandleMap.get(type).handle();
        }

        if(award == null){
            return false;
        }

        for(Prop prop:award.propList()){
            prop.setPropInstenceUid(IdUtil.simpleUUID());
        }

        this.doAward(user,award);

        return true;
    }



    /**
     * 发送奖励信息给玩家
     * @param user
     * @param award
     */
    public boolean send(User user, Award award){
        if(sendMsgUtil.connectionIsAlive(user.getUid())){
            return sendMsgUtil.sendMsgOneNoReturn(user.getUid(), SendType.AWARD, award);
        }else{
            return false;
        }
    }

    /**
     * 奖励处理
     * @param user
     * @param award
     */
    public void doAward(User user, Award award){
        boolean mail=true;
        //增加货币
        user.getCurrency().setCoin(user.getCurrency().getCoin() + award.getCoin());
        user.getCurrency().setDiamond(user.getCurrency().getDiamond() + award.getDiamond());
        user.getCurrency().setPetExp(user.getCurrency().getPetExp() + award.getExp());
        //增加道具
        if(!user.addProps(award.propList())){
            //TODO 发送邮件给玩家
        }

        this.send(user,award);
    }
}
