package com.haoyou.spring.cloud.alibaba.cultivate.service;

import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Friends;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.entity.UserNumerical;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.currency.use.handle.CurrencyUseHandle;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.pojo.bean.ChatRecord;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.CyrrencyUseMsg;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.FriendsDoMsg;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/22 10:04
 * <p>
 * 好友系统
 */
@Service
public class FriendsService {


    @Autowired
    private RedisObjectUtil redisObjectUtil;

    @Autowired
    private SendMsgUtil sendMsgUtil;

    @Autowired
    private UserUtil userUtil;
    @Autowired
    private RewardService rewardService;

    @Autowired
    private NumericalService numericalService;


    public BaseMessage friendsDo(MyRequest req) {


        User user = req.getUser();

        FriendsDoMsg friendsDoMsg = sendMsgUtil.deserialize(req.getMsg(), FriendsDoMsg.class);

        friendsDoMsg.setUser(user);

        MapBody mapBody = new MapBody();

        /**
         * 1：好友申请，2：同意好友申请，3：一键拒绝，4：赠送礼物，5：领取礼物，6：发送信息，7：删除好友，
         * 8：设置助战宠物
         */
        switch (friendsDoMsg.getType()) {
            case 1:
                mapBody = applicationDo(friendsDoMsg);
                break;
            case 2:
                mapBody = agreeDo(friendsDoMsg);
                break;
            case 3:
                mapBody = refuseDo(friendsDoMsg);
                break;
            case 4:
                mapBody = sendGift(friendsDoMsg);
                break;
            case 5:
                mapBody = receiveGift(friendsDoMsg);
                break;
            case 6:
                mapBody = sendMsg(friendsDoMsg);
                break;
            case 7:
                mapBody = deleteFriend(friendsDoMsg);
                break;
            case 8:
                mapBody = setHelpPet(friendsDoMsg);
                break;
        }

        mapBody.put("type",friendsDoMsg.getType());

        return mapBody;
    }

    /**
     * 设置助战宠物
     * @param friendsDoMsg
     * @return
     */
    private MapBody setHelpPet(FriendsDoMsg friendsDoMsg) {

        User user = friendsDoMsg.getUser();
        String helpPetUid = friendsDoMsg.getHelpPetUid();
        FightingPet byUserAndPetUid = FightingPet.getByUserAndPetUid(user, helpPetUid, redisObjectUtil);
        if(byUserAndPetUid == null){
            return MapBody.beErr();
        }
        user.getUserData().setHelpPetUid(helpPetUid);
        userUtil.saveUser(user);

        return MapBody.beSuccess();
    }

    /**
     * 好友申请
     *
     * @param friendsDoMsg
     * @return
     */
    public MapBody applicationDo(FriendsDoMsg friendsDoMsg) {

        User user = friendsDoMsg.getUser();

        String userUid = friendsDoMsg.getUserUid();

        String idNum = friendsDoMsg.getIdNum();

        if (StrUtil.isEmpty(userUid)) {
            if (StrUtil.isEmpty(idNum)) {
                return MapBody.beErr();
            } else {
                User userByIdNum = userUtil.getUserByIdNum(idNum);
                if (userByIdNum == null) {
                    return MapBody.beErr();
                } else {
                    userUid = userByIdNum.getUid();
                }
            }
        }

        if (friendsIsFull(user, userUid)) {
            return MapBody.beErr();
        }

        String friendApplicationKey = RedisKeyUtil.getKey(RedisKey.USER_FRIENDS_APPLICATION, userUid, user.getUid());

        if (redisObjectUtil.save(friendApplicationKey, user.getUid())) {

            sendMsgUtil.sendMsgOneNoReturn(userUid, SendType.FRIEND_APPLICATION, MapBody.beSuccess());

            return MapBody.beSuccess();
        }

        return MapBody.beErr();
    }

    /**
     * 同意好友申请
     *
     * @param friendsDoMsg
     * @return
     */
    public MapBody agreeDo(FriendsDoMsg friendsDoMsg) {
        User user = friendsDoMsg.getUser();

        String userUid = friendsDoMsg.getUserUid();

        String friendApplicationKey = RedisKeyUtil.getKey(RedisKey.USER_FRIENDS_APPLICATION, user.getUid(), userUid);

        String s = redisObjectUtil.get(friendApplicationKey, String.class);
        if (StrUtil.isNotEmpty(s)) {
            redisObjectUtil.delete(friendApplicationKey);

            if (friendsIsFull(user, userUid)) {
                return MapBody.beErr();
            }

            Friends friend = new Friends();
            friend.setUserUid1(user.getUid());
            friend.setUserUid2(userUid);
            friend.setCreatTime(new Date());
            friend.setIntimacy(0);

            userUtil.saveFriend(friend);

            UserNumerical userNumerical = new UserNumerical();
            userNumerical.setValue(friend.getIntimacy().longValue());
            String numericalName = String.format("%s_%s", RedisKey.FRIENDS, friend.getId());
            userNumerical.setNumericalName(numericalName);
            userNumerical.setUserUid(user.getUid());
            user.getUserNumericalMap().put(numericalName, userNumerical);

            User userByUid = userUtil.getUserByUid(userUid);
            userNumerical.setUserUid(userByUid.getUid());
            userByUid.getUserNumericalMap().put(numericalName, userNumerical);
            userUtil.saveUser(userByUid);
            userUtil.saveUser(user);
            return MapBody.beSuccess();
        }
        return MapBody.beErr();
    }

    /**
     * 一键拒绝
     *
     * @param friendsDoMsg
     * @return
     */
    public MapBody refuseDo(FriendsDoMsg friendsDoMsg) {
        User user = friendsDoMsg.getUser();
        if (friendsDoMsg.isOneButton()) {
            redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.USER_FRIENDS_APPLICATION, user.getUid()));

        } else {
            redisObjectUtil.delete(RedisKeyUtil.getKey(RedisKey.USER_FRIENDS_APPLICATION, user.getUid(), friendsDoMsg.getUserUid()));
        }
        return MapBody.beSuccess();
    }

    public boolean friendsIsFull(User user, String userUid) {
        if (userUtil.friendsIsFull(user.getUid()) || userUtil.friendsIsFull(userUid)) {
            return true;
        }
        return false;
    }


    /**
     * 发送奖励
     *
     * @param friendsDoMsg
     * @return
     */
    public MapBody sendGift(FriendsDoMsg friendsDoMsg) {

        // friends_gift

        User user = friendsDoMsg.getUser();
        if (friendsDoMsg.isOneButton()) {
            //给所有好友发送礼物
            String friendlkKey = RedisKeyUtil.getlkKey(RedisKey.USER_FRIENDS, user.getUid());
            HashMap<String, Integer> stringIntegerHashMap = redisObjectUtil.getlkMap(friendlkKey, Integer.class);

            for (Integer i : stringIntegerHashMap.values()) {
                String friendKey = RedisKeyUtil.getKey(RedisKey.FRIENDS, i.toString());
                Friends friends = redisObjectUtil.get(friendKey, Friends.class);
                String userUid = friends.getUserUid1();
                if (userUid.equals(user.getUid())) {
                    userUid = friends.getUserUid2();
                }
                sendGift(user, userUid);
            }
        } else {
            String userUid = friendsDoMsg.getUserUid();
            sendGift(user, userUid);
        }

        return MapBody.beSuccess();
    }

    private void sendGift(User user, String userUid) {
        String type = RedisKeyUtil.getKey(RedisKey.FRIENDS_GIFT, user.getUid());
        Award friendsGift = rewardService.getUpAward(userUid, type);
        if (friendsGift == null) {
            rewardService.refreshUpAward(userUid, rewardService.getAward("friends_gift"), type);
            addIntimacy(user,userUid,1L);
        }
    }

    /**
     * 领取礼物
     *
     * @param friendsDoMsg
     * @return
     */
    public MapBody receiveGift(FriendsDoMsg friendsDoMsg) {

        User user = friendsDoMsg.getUser();

        String type = RedisKeyUtil.getlkKey(RedisKey.FRIENDS_GIFT);
        HashMap<String, Award> upAwards = rewardService.getUpAwards(user.getUid(), type);
        //限制每日领取的奖励个数
        if (upAwards.size() >= userUtil.friendsMaxCount) {
            return MapBody.beErr();
        }
        if (friendsDoMsg.isOneButton()) {
            for (String key : upAwards.keySet()) {
                rewardService.receiveAward(user, key);
            }
        } else {
            String userUid = friendsDoMsg.getUserUid();
            type = RedisKeyUtil.getKey(RedisKey.FRIENDS_GIFT, userUid);
            return rewardService.receiveAward(user, type);
        }
        return MapBody.beSuccess();
    }

    /**
     * @param friendsDoMsg
     * @return
     */
    public MapBody sendMsg(FriendsDoMsg friendsDoMsg) {

        User user = friendsDoMsg.getUser();

        String userUid = friendsDoMsg.getUserUid();

        String sendMsg = friendsDoMsg.getSendMsg();

        if(sendMsg.length()>50){
            MapBody.beErr();
        }

        //屏蔽词汇替换
        sendMsg = userUtil.replaceAllShieldVocas(sendMsg);


        if(sendMsgUtil.connectionIsAlive(userUid)){
            //记录聊天记录
            ChatRecord chatRecord = userUtil.addChatRecord(user, userUid, sendMsg);
            //发送聊天信息
            sendMsgUtil.sendMsgOneNoReturn(userUid, SendType.SEND_CHAT, chatRecord);
        }else{
            //记录聊天记录
            userUtil.addChatRecord(user, userUid, sendMsg,true);
        }

        return MapBody.beSuccess();
    }

    /**
     * 删除好友
     * @param friendsDoMsg
     * @return
     */
    public MapBody deleteFriend(FriendsDoMsg friendsDoMsg) {
        User user = friendsDoMsg.getUser();

        String userUid = friendsDoMsg.getUserUid();

        Friends friend = userUtil.getFriend(user, userUid);
        userUtil.deleteFriend(friend);

        return MapBody.beSuccess();
    }



    /**
     * 增加亲密度
     * @param user
     * @param userUid
     */
    public void addIntimacy(User user, String userUid,long value) {
        //增加亲密度
        Friends friend = userUtil.getFriend(user, userUid);
        String numericalName = String.format("%s_%s", RedisKey.FRIENDS, friend.getId());
        numericalService.numericalAdd(user,numericalName,value);
    }
}
