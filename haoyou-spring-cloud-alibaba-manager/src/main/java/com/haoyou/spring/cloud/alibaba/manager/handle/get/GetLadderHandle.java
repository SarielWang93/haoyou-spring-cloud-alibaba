package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.date.DateUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.pojo.bean.RankUser;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 获取获取天梯页面信息
 */
@Service
public class GetLadderHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetLadderHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_LADDER;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        MapBody mapBody = MapBody.beSuccess();

        User user = req.getUser();
        String yyMM = DateUtil.date().toString("yyMM");
        String rankKey = RedisKeyUtil.getKey(RedisKey.LADDER_RANKING, yyMM);
        //排名
        Long aLong = scoreRankUtil.find(rankKey, user);
        //天梯阶数
        Long ladderLevel = user.getUserNumericalMap().get("ladder_level").getValue();
        //当前星数
        Long ladderLevelStar = user.getUserNumericalMap().get("ladder_level_star").getValue();
        //当天天梯胜利次数
        Long dailyLadderWin = user.getUserNumericalMap().get("daily_ladder_win").getValue();
        //当天天梯奖励次数上限
        Long ladderAwardMax = 10L;
        //奖励
        Award award = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, "pvp"), Award.class);

        //排名
        mapBody.put("aLong",aLong);
        //天梯阶数
        mapBody.put("ladderLevel",ladderLevel);
        //当前星数
        mapBody.put("ladderLevelStar",ladderLevelStar);
        //当天天梯胜利次数
        mapBody.put("dailyLadderWin",dailyLadderWin);
        //当天天梯奖励次数上限
        mapBody.put("ladderAwardMax",ladderAwardMax);
        //奖励
        mapBody.put("award",award);


        return mapBody;
    }
}
