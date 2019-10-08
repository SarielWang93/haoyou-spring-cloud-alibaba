package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.collection.CollUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Email;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.pojo.bean.DailyCheckIn;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 每日签到
 */
@Service
public class GetDailyCheckInHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetDailyCheckInHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_DAILY_IN;
    }

    @Override
    public BaseMessage handle(MyRequest req) {

        MapBody mapBody = new MapBody<>();

        User user = req.getUser();

        DailyCheckIn dailyCheckIn = userUtil.getDailyCheckIn(user);

        List<Award> awards = dailyCheckIn.getAwards();


        for (Award award : awards) {
            if (!award.isUsed()) {
                String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid(), RedisKey.DAILY_CHECK_IN, award.getType());
                Award award1 = redisObjectUtil.get(key, Award.class);
                if (award1 != null && award1.isUsed()) {
                    mapBody.put("todayIsUsed", true);
                    award.setUsed(true);
                } else {
                    mapBody.put("type", RedisKeyUtil.getKey(RedisKey.DAILY_CHECK_IN, award.getType()));
                    mapBody.put("todayIsUsed", false);
                }
                break;
            }
        }
        mapBody.put("awards", awards);
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }

}
