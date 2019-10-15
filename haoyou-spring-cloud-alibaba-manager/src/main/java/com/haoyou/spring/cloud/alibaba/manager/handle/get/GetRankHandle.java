package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Server;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.pojo.bean.RankUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

        Map<String, Object> msgMap = this.getMsgMap(req);
        String rankName = (String) msgMap.get("rankName");

        if (RedisKey.LADDER_RANKING.equals(rankName)) {
            return getLadderRank(user);
        }

        Server server = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.SERVER, user.getServerId().toString()), Server.class);
        String rankKey = RedisKeyUtil.getKey(RedisKey.RANKING, server.getServerNum().toString());
        MapBody serverRank = getRank(user, rankKey);
        return serverRank;
    }
    //获取天梯排行
    public MapBody getLadderRank(User user) {
        DateTime date = DateUtil.date();
        DateTime dateTime = DateUtil.offsetMonth(date, -1);
        String yyMM = dateTime.toString("yyMM");
        String rankKey = RedisKeyUtil.getKey(RedisKey.LADDER_RANKING, yyMM);
        MapBody serverRank = getRank(user, rankKey);
        serverRank.put("rankName", RedisKey.LADDER_RANKING);
        return serverRank;
    }


            //获得排名的前100名
            public MapBody getRank(User user, String rankKey) {


                MapBody mapBody = MapBody.beSuccess();

                Long aLong = scoreRankUtil.zCard(rankKey);
                Long start = 0l;
                if (aLong > 100) {
                    start = aLong - 100;
        }
        TreeMap<Long, String> treeMap = scoreRankUtil.list(rankKey, start, aLong);
        Long myRanking = -1L;
        Long myIntegral = -1L;
        List<RankUser> rankUsers = new ArrayList<>();
        long rank = 1;
        for (long integral : treeMap.keySet()) {
            String userUid = treeMap.get(integral);

            if (user.getUid().equals(userUid)) {
                myRanking = rank;
                myIntegral = integral;
            }

            User userByUid = userUtil.getUserByUid(userUid);
            RankUser rankUser = new RankUser().init(userByUid, integral, rank++);
            rankUsers.add(rankUser);
        }

        String key1 = RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid());
        String key = RedisKeyUtil.getKey(key1, rankKey);
        Award award = redisObjectUtil.get(key, Award.class);
        if (award != null) {
            mapBody.put("award", award);
        }

        mapBody.put("players", rankUsers);
        mapBody.put("ranking", myRanking);
        mapBody.put("integral", myIntegral);

        return mapBody;
    }
}
