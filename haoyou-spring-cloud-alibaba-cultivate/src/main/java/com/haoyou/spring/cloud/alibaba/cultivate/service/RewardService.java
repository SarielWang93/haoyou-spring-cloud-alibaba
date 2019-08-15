package com.haoyou.spring.cloud.alibaba.cultivate.service;


import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.reward.handle.RewardHandle;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    @Autowired
    protected UserUtil userUtil;



    public boolean rewards(User user, String type) {
        Award award = this.getAward(type);

        if(award == null){
            return false;
        }

        if(!award.isUsed()){
            return this.doAward(user,award);
        }

        return true;
    }

    public Award getAward(String type){
        Award award =  redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, type),Award.class);


        if(award == null){
            if(rewardHandleMap.get(type) != null){
                award = rewardHandleMap.get(type).handle();
            }
        }
        return award;
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
    public boolean doAward(User user, Award award){

        if(award == null){
            return false;
        }

        boolean mail=true;
        //增加货币
        user.getCurrency().setCoin(user.getCurrency().getCoin() + award.getCoin());

        user.getCurrency().setDiamond(user.getCurrency().getDiamond() + award.getDiamond());

        user.getCurrency().setPetExp(user.getCurrency().getPetExp() + award.getPetExp());

        user.getUserData().setExp(user.getUserData().getExp() + award.getExp());

        //TODO 玩家升级


        if(award.getPropsList()!=null){
            //增加道具
            userUtil.addProps(user,award.getPropsList());
        }

        this.send(user,award);

        return true;
    }


    /**
     * 把奖励存入内存，等待领取.已经存在则加入失败
     * @param userUid
     * @param award
     * @return
     */
    public boolean upAward(String userUid, Award award ,String type){
        if (award != null) {
            award.setUpAwardDate(new Date());
            String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, userUid, type);
            Award award1 = redisObjectUtil.get(key, Award.class);
            if(award1 == null){
                return redisObjectUtil.save(key, award,-1);
            }else {
                return false;
            }
        }
        return false;
    }

    /**
     * 把奖励存入内存，等待领取.已经存在则刷新
     * @param userUid
     * @param award
     * @param type
     * @return
     */
    public boolean refreshUpAward(String userUid, Award award ,String type){
        if (award != null) {
            award.setUpAwardDate(new Date());
            String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, userUid, type);
            return redisObjectUtil.save(key, award,-1);
        }
        return false;
    }

    /**
     * 删除内存中的奖励
     * @param userUid
     * @param type
     * @return
     */
    public boolean deleteUpAward(String userUid ,String type){
        String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, userUid, type);
        return redisObjectUtil.delete(key);
    }
    public boolean deleteUpAwards(String userUid ,String type){
        String key = RedisKeyUtil.getlkKey(RedisKey.USER_AWARD, userUid, type);
        return redisObjectUtil.deleteAll(key);
    }


    /**
     * 获取已发放的奖励
     * @param userUid
     * @param type
     * @return
     */
    public Award getUpAward(String userUid,String type){
        String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, userUid, type);
        return redisObjectUtil.get(key, Award.class);
    }
    public HashMap<String, Award> getUpAwards(String userUid, String type){
        String key = RedisKeyUtil.getlkKey(RedisKey.USER_AWARD, userUid, type);
        return redisObjectUtil.getlkMap(key, Award.class);
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
        if(type!=null && type.startsWith(RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid()))){
            key = type;
        }

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
