package com.haoyou.spring.cloud.alibaba.cultivate.numerical;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/30 14:23
 * 增加好友亲密度
 */
@Service
public class FriendsIntimacyCheck extends NumericalCheck {
    @Override
    public void check(User user, String numericalName, long addValue) {
        if(numericalName.startsWith(RedisKey.FRIENDS)){
            String[] s = numericalName.split("_");

            Integer id = Integer.valueOf(s[s.length-1]);

            UserNumerical userNumerical = user.getUserNumericalMap().get(numericalName);

            //修改友好度值
            Friends friend = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.FRIENDS, id.toString()), Friends.class);
            Integer intimacy = friend.getIntimacy();
            userNumerical.setValue(intimacy.longValue());
            friend.setIntimacy(friend.getIntimacy()+Integer.valueOf(Long.toString(addValue)));

            userUtil.saveFriend(friend);
            //同步对面友好度
            String userUid = friend.getUserUid1();
            if(userUid.equals(user.getUid())){
                userUid = friend.getUserUid2();
            }
            User userByUid = userUtil.getUserByUid(userUid);
            userByUid.getUserNumericalMap().get(numericalName).setValue(friend.getIntimacy().longValue());
            userUtil.saveUser(userByUid);


            //TODO 奖励发放




        }
    }
}
