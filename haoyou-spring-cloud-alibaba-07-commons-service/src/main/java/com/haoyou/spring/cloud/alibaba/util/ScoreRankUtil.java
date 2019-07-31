package com.haoyou.spring.cloud.alibaba.util;


import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.entity.UserData;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.serialization.JsonSerializer;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.RankUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * redis存储对象
 */
@Service
public class ScoreRankUtil {

    private final static Logger logger = LoggerFactory.getLogger(ScoreRankUtil.class);
    @Autowired
    private ScoreRankService scoreRankService;
    @Autowired
    private JsonSerializer jsonSerializer;

    @PostConstruct
    private void init() {
    }

    /**
     * 批量新增
     */
    public boolean batchAdd(String scoreRank, List<User> users) {

        Map<String, Long> msgs = new HashMap<>();
        for (User user : users) {
            msgs.put(this.getRankMsg(user), user.getCurrency().getRank().longValue());
        }

        return scoreRankService.batchAdd(scoreRank, msgs);

    }

    /**
     * 单个新增
     */
    public boolean add(String scoreRank, User user) {
        return scoreRankService.add(scoreRank, user.getUid(), user.getCurrency().getRank().longValue());
    }

    /**
     * 获取排行列表
     */
    public List<RankUser> list(String scoreRank, Long start, Long end) {

        List<RankUser> rankUsers = new ArrayList<>();
        List<String> list = scoreRankService.list(scoreRank, start, end);

        for (String s : list) {
            rankUsers.add(this.getRankUser(s));
        }

        return rankUsers;
    }


    /**
     * 获取单个的排行
     */
    public Long find(String scoreRank, User user){
        return scoreRankService.find(scoreRank, this.getRankMsg(user));
    }


    /**
     * 使用加法操作分数
     */

    public Long incrementScore(String scoreRank, User user){
        return scoreRankService.incrementScore(scoreRank,this.getRankMsg(user),user.getCurrency().getRank().longValue());
    }


    /**
     * 获取整个集合的基数(数量大小)
     */

    public Long zCard(String scoreRank){
        return scoreRankService.zCard(scoreRank);
    }









    private RankUser getRankUser(String s){
        try {
            return MapperUtils.json2pojo(s,RankUser.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getRankMsg(User user) {

        RankUser rankUser = new RankUser();

        rankUser.setUserUid(user.getUid());
        rankUser.setName(user.getUserData().getName());
        rankUser.setAvatar(user.getUserData().getAvatar());
        rankUser.setIntegral(user.getCurrency().getRank());

        String plj = "";
        try {
            plj = MapperUtils.obj2json(rankUser);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return plj;
    }
}
