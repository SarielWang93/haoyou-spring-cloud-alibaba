package com.haoyou.spring.cloud.alibaba.cultivate.service;

import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Friends;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.currency.use.handle.CurrencyUseHandle;
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

    private static final int chatRecordSavedays = 30;


    @Autowired
    private RedisObjectUtil redisObjectUtil;

    @Autowired
    private SendMsgUtil sendMsgUtil;

    @Autowired
    private UserUtil userUtil;
    @Autowired
    private RewardService rewardService;



    public BaseMessage friendsDo(MyRequest req) {


        User user = req.getUser();

        FriendsDoMsg friendsDoMsg = sendMsgUtil.deserialize(req.getMsg(), FriendsDoMsg.class);

        friendsDoMsg.setUser(user);

        /**
         * 1：好友申请，2：同意好友申请，3：一键拒绝，4：赠送礼物，5：领取礼物，6：发送信息
         */
        switch (friendsDoMsg.getType()) {
            case 1:
                applicationDo(friendsDoMsg);
                break;
            case 2:
                agreeDo(friendsDoMsg);
                break;
            case 3:
                refuseDo(friendsDoMsg);
                break;
            case 4:
                sendGift(friendsDoMsg);
                break;
            case 5:
                receiveGift(friendsDoMsg);
                break;
            case 6:
                sendMsg(friendsDoMsg);
                break;
        }


        return null;
    }

    /**
     * 好友申请
     *
     * @param friendsDoMsg
     * @return
     */
    public BaseMessage applicationDo(FriendsDoMsg friendsDoMsg) {

        User user = friendsDoMsg.getUser();

        String userUid = friendsDoMsg.getUserUid();

        String friendApplicationKey = RedisKeyUtil.getKey(RedisKey.USER_FRIENDS_APPLICATION, userUid, user.getUid());

        if (redisObjectUtil.save(friendApplicationKey, user.getUid())) {

            return BaseMessage.beSuccess();
        }

        return BaseMessage.beErr();
    }

    /**
     * 同意好友申请
     *
     * @param friendsDoMsg
     * @return
     */
    public BaseMessage agreeDo(FriendsDoMsg friendsDoMsg) {
        User user = friendsDoMsg.getUser();

        String userUid = friendsDoMsg.getUserUid();

        String friendApplicationKey = RedisKeyUtil.getKey(RedisKey.USER_FRIENDS_APPLICATION, user.getUid(), userUid);

        String s = redisObjectUtil.get(friendApplicationKey, String.class);
        if (s != null) {
            redisObjectUtil.delete(friendApplicationKey);

            Friends friend = new Friends();
            friend.setUserUid1(user.getUid());
            friend.setUserUid2(userUid);
            friend.setCreatTime(new Date());
            friend.setIntimacy(0);


            userUtil.saveFriend(friend);

            userUtil.saveUser(user);
            return BaseMessage.beSuccess();
        }
        return BaseMessage.beErr();
    }

    /**
     * 一键拒绝
     * @param friendsDoMsg
     * @return
     */
    public BaseMessage refuseDo(FriendsDoMsg friendsDoMsg) {
        User user = friendsDoMsg.getUser();
        if(friendsDoMsg.isOneButton()){
            redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.USER_FRIENDS_APPLICATION, user.getUid()));

        }else{
            redisObjectUtil.delete(RedisKeyUtil.getKey(RedisKey.USER_FRIENDS_APPLICATION, user.getUid(),friendsDoMsg.getUserUid()));
        }
        return BaseMessage.beSuccess();
    }

    /**
     * 发送奖励
     * @param friendsDoMsg
     * @return
     */
    public BaseMessage sendGift(FriendsDoMsg friendsDoMsg) {

       // friends_gift

        User user = friendsDoMsg.getUser();
        if(friendsDoMsg.isOneButton()){
            //给所有好友发送礼物
            String friendlkKey = RedisKeyUtil.getlkKey(RedisKey.USER_FRIENDS,user.getUid());
            HashMap<String, Integer> stringIntegerHashMap = redisObjectUtil.getlkMap(friendlkKey, Integer.class);

            for(Integer i : stringIntegerHashMap.values()){
                String friendKey = RedisKeyUtil.getKey(RedisKey.FRIENDS, i.toString());
                Friends friends = redisObjectUtil.get(friendKey, Friends.class);
                String userUid = friends.getUserUid1();
                if(userUid.equals(user.getUid())){
                    userUid = friends.getUserUid2();
                }
                sendGift(user,userUid);
            }
        }else{
            String userUid = friendsDoMsg.getUserUid();
            sendGift(user,userUid);
        }

        return BaseMessage.beSuccess();
    }
    private void sendGift(User user,String userUid){
        String type = RedisKeyUtil.getKey("friendsGift", user.getUid());
        Award friendsGift = rewardService.getUpAward(userUid, type);
        if(friendsGift == null){
            rewardService.refreshUpAward(userUid,rewardService.getAward("friends_gift"),type);
        }
    }

    /**
     * 领取礼物
     * @param friendsDoMsg
     * @return
     */
    public BaseMessage receiveGift(FriendsDoMsg friendsDoMsg) {

        User user = friendsDoMsg.getUser();

        if(friendsDoMsg.isOneButton()){
            String type = RedisKeyUtil.getlkKey("friendsGift");
            HashMap<String, Award> upAwards = rewardService.getUpAwards(user.getUid(), type);
            for(String key:upAwards.keySet()){
                rewardService.receiveAward(user,key);
            }
        }else{
            String userUid = friendsDoMsg.getUserUid();
            String type = RedisKeyUtil.getKey("friendsGift", userUid);
            rewardService.receiveAward(user,type);
        }
        return BaseMessage.beSuccess();
    }

    /**
     *
     * @param friendsDoMsg
     * @return
     */
    public BaseMessage sendMsg(FriendsDoMsg friendsDoMsg) {

        User user = friendsDoMsg.getUser();

        String userUid = friendsDoMsg.getUserUid();

        String sendMsg = friendsDoMsg.getSendMsg();

        //屏蔽词汇替换
        sendMsg = userUtil.replaceAllShieldVocas(sendMsg);

        //记录聊天记录
        ChatRecord chatRecord = userUtil.addChatRecord(user, userUid, sendMsg);

        //发送聊天信息
        sendMsgUtil.sendMsgOneNoReturn(userUid, SendType.SEND_CHAT,chatRecord);

        return BaseMessage.beSuccess();
    }
}
