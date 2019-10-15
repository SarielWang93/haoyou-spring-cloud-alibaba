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
import java.util.Map;

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
        Map<String, Object> msgMap = this.getMsgMap(req);

        String type = (String) msgMap.get("type");
        if ("awards".equals(type)) {
            return getLadderRankingAwards();
        }

        User user = req.getUser();
        MapBody mapBody = MapBody.beSuccess();

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
        Award award = userUtil.getAward("pvp");

        //排名
        mapBody.put("aLong", aLong);
        //天梯阶数
        mapBody.put("ladderLevel", ladderLevel);
        //当前星数
        mapBody.put("ladderLevelStar", ladderLevelStar);
        //当天天梯胜利次数
        mapBody.put("dailyLadderWin", dailyLadderWin);
        //当天天梯奖励次数上限
        mapBody.put("ladderAwardMax", ladderAwardMax);
        //奖励
        mapBody.put("award", award);


        return mapBody;
    }

    //获取赛季奖励
    public BaseMessage getLadderRankingAwards() {
        MapBody mapBody = MapBody.beSuccess();

        for (int i = 1; i < 6; i++) {
            mapBody.put(String.format("ladder_ranking%s", i), userUtil.getAward(String.format("ladder_ranking%s", i)));
        }
        mapBody.put("ladder_ranking6_10", userUtil.getAward("ladder_ranking6-10"));
        mapBody.put("ladder_ranking11_20", userUtil.getAward("ladder_ranking11-20"));
        mapBody.put("ladder_ranking21_50", userUtil.getAward("ladder_ranking21-50"));
        mapBody.put("ladder_ranking51_100", userUtil.getAward("ladder_ranking51-100"));
        mapBody.put("ladder_ranking101_200", userUtil.getAward("ladder_ranking101-200"));
        mapBody.put("ladder_ranking201_500", userUtil.getAward("ladder_ranking201-500"));
        mapBody.put("ladder_ranking501_1000", userUtil.getAward("ladder_ranking501-1000"));
        mapBody.put("ladder_ranking1001", userUtil.getAward("ladder_ranking1001"));

        for(int i = 0; i < 15; i++){
            mapBody.put(String.format("ladder%s", i), userUtil.getAward(String.format("ladder%s", i)));
        }

        mapBody.put("type", "awards");

        return mapBody;
    }

}
