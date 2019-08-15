package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.pojo.bean.ChatRecord;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 获取成就系统
 */
@Service
public class GetFriendsHandle extends ManagerHandle {

    private static final Logger logger = LoggerFactory.getLogger(GetFriendsHandle.class);
    private static final long serialVersionUID = -9187389077840416711L;

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_FRIENDS;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        User user = req.getUser();

        Map<String, Object> msgMap = this.getMsgMap(req);

        Integer type = (Integer) msgMap.get("type");

        MapBody mapBody = null;

        switch (type) {
            //获取好友列表
            case 1:
                mapBody = getFriends(user);
                break;
            //获取聊天记录
            case 2:
                mapBody = getChatRecords(user, msgMap);
                break;
            //好友添加列表
            case 3:
                mapBody = getAddFriends(user);
                break;
            //好友申请列表
            case 4:
                mapBody = getAddFriendsApplication(user);
                break;
        }
        mapBody.put("type",type);

        return mapBody;
    }


    /**
     * 获取好友列表
     *
     * @param user
     * @return
     */
    private MapBody getFriends(User user) {
        MapBody mapBody = new MapBody<>();

        List<Friends> friends = userUtil.getFriends(user.getUid());

        List<Map> friendsMsg = new ArrayList<>();

        for (Friends friend : friends) {
            Map<String, Object> friendMsg = new HashMap<>();

            String userUid = friend.getUserUid1();
            if (userUid.equals(user.getUid())) {
                userUid = friend.getUserUid2();
            }

            User friendUser = userUtil.getUserByUid(userUid);

            UserData userData = friendUser.getUserData();

            friendMsg.put("name", userData.getName());
            friendMsg.put("userUid", friendUser.getUid());
            friendMsg.put("avatar", userData.getAvatar());
            friendMsg.put("level", userData.getLevel());
            friendMsg.put("intimacy", friend.getIntimacy());

            String inCatch = userUtil.isInCatch(userUid);

            if (inCatch.startsWith(RedisKey.USER)) {
                friendMsg.put("isOnLine", true);
            }

            List<ChatRecord> chatRecords = userUtil.getChatRecord(friend);
            for (ChatRecord chatRecord : chatRecords) {
                if(chatRecord.isNotRead() && user.getUid().equals(chatRecord.getUserUid())){
                    friendMsg.put("hasNotRead", true);
                    break;
                }
            }


            String type1 = RedisKeyUtil.getKey(RedisKey.FRIENDS_GIFT, friendUser.getUid());
            String type2 = RedisKeyUtil.getKey(RedisKey.FRIENDS_GIFT, user.getUid());

            Award upAward1 = this.getUpAward(user.getUid(), type1);
            Award upAward2 = this.getUpAward(friendUser.getUid(), type2);

            if (upAward1 != null) {
                friendMsg.put("canReceiveGift", true);
            }
            if (upAward2 == null) {
                friendMsg.put("canSendGift", true);
            }
            friendsMsg.add(friendMsg);
        }

        mapBody.put("friends", friendsMsg);
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }

    /**
     * 获取聊天记录
     *
     * @param user
     * @param msgMap
     * @return
     */
    private MapBody getChatRecords(User user, Map<String, Object> msgMap) {
        MapBody mapBody = new MapBody<>();
        String userUid = (String) msgMap.get("userUid");

        List<ChatRecord> chatRecords = userUtil.getChatRecord(user, userUid);

        mapBody.put("chatRecords", chatRecords);

        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }

    /**
     * 好友添加列表
     *
     * @param user
     * @return
     */
    private MapBody getAddFriends(User user) {
        MapBody mapBody = new MapBody<>();

        List<User> users = userUtil.allUser();

        List<Friends> friends = userUtil.getFriends(user.getUid());

        //好友uid
        List<String> friendsUid = new ArrayList<>();
        for (Friends friend : friends) {
            String friendUid = friend.getUserUid1();
            if (friendUid.equals(user.getUid())) {
                friendUid = friend.getUserUid2();
            }
            friendsUid.add(friendUid);
        }

        //过滤掉好友以及还有数量达到上限的玩家,已经申请过的
        List<User> filter = new ArrayList<>();
        for (User user1 : users) {
            if (userUtil.friendsIsFull(user1.getUid())) {
                filter.add(user1);
            }else if (friendsUid.contains(user1.getUid())) {
                filter.add(user1);
            }else{
                String friendApplicationKey = RedisKeyUtil.getKey(RedisKey.USER_FRIENDS_APPLICATION, user1.getUid(), user.getUid());
                String s = redisObjectUtil.get(friendApplicationKey, String.class);
                if(StrUtil.isNotEmpty(s)){
                    filter.add(user1);
                }
            }

        }
        users.removeAll(filter);

        //获取返回的5个
        List<User> rt5 = new ArrayList<>();
        int max = 5;
        if (users.size() < 5) {
            max = users.size();
        }
        for (; ; ) {
            int i = RandomUtil.randomInt(users.size());
            User user1 = users.remove(i);
            rt5.add(user1);
            if (rt5.size() >= max) {
                break;
            }
        }
        //返回信息
        List<Map> usersMsg = new ArrayList<>();

        for (User user1 : rt5) {
            Map<String, Object> userMsg = new HashMap<>();
            UserData userData = user1.getUserData();
            userMsg.put("name", userData.getName());
            userMsg.put("userUid", user1.getUid());
            userMsg.put("avatar", userData.getAvatar());
            userMsg.put("level", userData.getLevel());
            usersMsg.add(userMsg);
        }

        mapBody.put("users", usersMsg);

        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }

    /**
     * 好友申请列表
     *
     * @param user
     * @return
     */
    private MapBody getAddFriendsApplication(User user) {
        MapBody mapBody = new MapBody<>();

        HashMap<String, String> stringStringHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.USER_FRIENDS_APPLICATION, user.getUid()), String.class);

        //返回信息
        List<Map> usersMsg = new ArrayList<>();

        for (String userUid : stringStringHashMap.values()) {
            User user1 = userUtil.getUserByUid(userUid);
            Map<String, Object> userMsg = new HashMap<>();
            UserData userData = user1.getUserData();
            userMsg.put("name", userData.getName());
            userMsg.put("userUid", user1.getUid());
            userMsg.put("avatar", userData.getAvatar());
            userMsg.put("level", userData.getLevel());
            usersMsg.add(userMsg);
        }


        mapBody.put("users", usersMsg);
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }
}
