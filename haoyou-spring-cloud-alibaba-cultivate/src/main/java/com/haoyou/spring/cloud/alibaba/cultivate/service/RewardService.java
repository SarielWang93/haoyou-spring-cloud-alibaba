package com.haoyou.spring.cloud.alibaba.cultivate.service;


import cn.hutool.core.util.IdUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.reward.handle.RewardHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
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
        Award award = this.getAward(type);

        if(award == null){
            award = rewardHandleMap.get(type).handle();
        }

        if(award == null){
            return false;
        }

        if(!award.isUsed()){
            this.doAward(user,award);
        }

        return true;
    }

    public Award getAward(String type){
         return redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, type),Award.class);
    }



    /**
     * 发送奖励信息给玩家
     * @param user
     * @param award
     */
    public boolean send(User user, Award award){
        if(sendMsgUtil.connectionIsAlive(user.getUid())){

            award.setPropsList(award.propList());
            award.setProps(null);
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
    public boolean doAward(User user, Award award){
        boolean mail=true;
        //增加货币
        user.getCurrency().setCoin(user.getCurrency().getCoin() + award.getCoin());

        user.getCurrency().setDiamond(user.getCurrency().getDiamond() + award.getDiamond());

        user.getCurrency().setPetExp(user.getCurrency().getPetExp() + award.getPetExp());

        user.getUserData().setExp(user.getUserData().getExp() + award.getExp());

        //TODO 玩家升级


        //增加道具
        if(!user.addProps(award.propList())){
            award.setCoin(0);
            award.setDiamond(0);
            award.setPetExp(0);
            award.setExp(0);
            //TODO 发送邮件给玩家

            String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid(), RedisKey.EMIL);
            redisObjectUtil.save(key,award);

        }

        this.send(user,award);

        return true;
    }


    /**
     * 把奖励存入内存，等待领取
     * @param userUid
     * @param award
     * @return
     */
    public boolean upAward(String userUid, Award award ,String type){
        if (award != null) {
            String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, userUid, type);
            return redisObjectUtil.save(key, award);
        }
        return false;
    }

    /**
     * 领取内存中的奖励
     * @param user
     * @param type
     * @return
     */
    public MapBody receiveAward (User user,String type) {
        MapBody mapBody = new MapBody();
        String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid(), type);
        Award award = redisObjectUtil.get(key, Award.class);
        if(award != null && !award.isUsed()){
            if (this.doAward(user,award)) {
                award.setUsed(true);
                redisObjectUtil.save(key,award);
                mapBody.setState(ResponseMsg.MSG_SUCCESS);
                return mapBody;
            }else{
                mapBody.put("errMsg", "奖励获取错误！");
            }
        }else{
            mapBody.put("errMsg", "奖励未找到，或者已经领取！");
        }

        mapBody.setState(ResponseMsg.MSG_ERR);
        return mapBody;
    }
}
