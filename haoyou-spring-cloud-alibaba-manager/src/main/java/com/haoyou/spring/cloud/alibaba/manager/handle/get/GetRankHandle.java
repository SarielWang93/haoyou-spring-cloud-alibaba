package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取排行榜数据
 */
@Service
public class GetRankHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetRankHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_RANK;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        User user = req.getUser();

        MapBody mapBody = new MapBody<>();
        mapBody.setState(ResponseMsg.MSG_SUCCESS);


        Long aLong = scoreRankService.zCard(RedisKey.RANKING);
        Long start = 0l;
        if (aLong > 100) {
            start = aLong - 100;
        }
        List<String> list = scoreRankService.list(RedisKey.RANKING, start, aLong);

        Map[] players = new Map[list.size()];

        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> player = new HashMap<>();
            user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, list.get(i)), User.class);

            player.put("name", user.getUserData().getName());
            player.put("avatar", user.getUserData().getAvatar());
            player.put("rank", user.getCurrency().getRank());

            players[list.size()-1-i] = player;
        }


        mapBody.put("players", players);
        return mapBody;
    }
}
