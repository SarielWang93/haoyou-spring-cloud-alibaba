package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Server;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.RankUser;
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
        Server server = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.SERVER, user.getServerId().toString()), Server.class);

        MapBody mapBody = new MapBody<>();
        mapBody.setState(ResponseMsg.MSG_SUCCESS);


        Long aLong = scoreRankUtil.zCard(RedisKeyUtil.getKey(RedisKey.RANKING, server.getServerNum().toString()));
        Long start = 0l;
        if (aLong > 100) {
            start = aLong - 100;
        }
        List<RankUser> list = scoreRankUtil.list(RedisKeyUtil.getKey(RedisKey.RANKING, server.getServerNum().toString()), start, aLong);
        RankUser[] players = new RankUser[list.size()];

        int myRanking = -1;

        for (int i = 0; i < list.size(); i++) {

            RankUser rankUser = list.get(i);


            players[i] = rankUser;
        }

        String key1 = RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid());
        String key = RedisKeyUtil.getKey(key1, RedisKey.RANKING);
        Award award = redisObjectUtil.get(key, Award.class);
        if (award != null) {
            award.setPropsList(award.propList());
            award.setProps(null);
            mapBody.put("award", award);
        }

        mapBody.put("players", players);
        mapBody.put("ranking", myRanking);
        mapBody.put("integral", user.getCurrency().getRank());

        return mapBody;
    }
}
