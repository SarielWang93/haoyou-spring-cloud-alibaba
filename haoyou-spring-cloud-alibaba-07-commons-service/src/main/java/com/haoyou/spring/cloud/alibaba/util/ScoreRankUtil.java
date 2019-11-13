package com.haoyou.spring.cloud.alibaba.util;


import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.serialization.JsonSerializer;
import com.haoyou.spring.cloud.alibaba.pojo.bean.RankUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;


/**
 * redis存储对象
 */
@Service
public class ScoreRankUtil {

    private final static Logger logger = LoggerFactory.getLogger(ScoreRankUtil.class);
    @Autowired
    private ScoreRankService scoreRankService;

    @PostConstruct
    private void init() {
    }

    /**
     * 批量新增
     */
    public boolean batchAdd(String scoreRank, Map<String, Long> msgs) {
        return scoreRankService.batchAdd(scoreRank, msgs);
    }
    public boolean batchAdd(String scoreRank, List<User> users) {
        Map<String, Long> msgs = new HashMap<>();
        for (User user : users) {
            msgs.put(user.getUid(), user.getCurrency().getRank().longValue());
        }
        return this.batchAdd(scoreRank, msgs);
    }

    /**
     * 单个新增
     */
    public boolean add(String scoreRank, User user) {
        return scoreRankService.add(scoreRank, user.getUid() , user.getCurrency().getRank().longValue());
    }
    public boolean add(String scoreRank, User user, long rank) {
        return scoreRankService.add(scoreRank, user.getUid() , rank);
    }
    /**
     * 获取排行列表
     */
    public TreeMap<Long,String> list(String scoreRank) {
        return  list(scoreRank,0L,zCard(scoreRank));
    }

    public TreeMap<Long,String> list(String scoreRank, Long start, Long end) {
        TreeMap<Long,String> treeMap = scoreRankService.list(scoreRank, start, end);
        return treeMap;
    }


    /**
     * 获取单个的排行
     */
    public Long find(String scoreRank, User user){
        return scoreRankService.find(scoreRank,user.getUid());
    }
    /**
     * 获取单个的积分
     */
    public Long findIntegral(String scoreRank, User user){
        return scoreRankService.findIntegral(scoreRank,user.getUid());
    }

    /**
     * 使用加法操作分数
     */

    public Long incrementScore(String scoreRank, User user ,long rank){
        return scoreRankService.incrementScore(scoreRank,user.getUid(),rank);
    }


    /**
     * 获取整个集合的基数(数量大小)
     */

    public Long zCard(String scoreRank){
        return scoreRankService.zCard(scoreRank);
    }

    /**
     * 删除排名
     */

    public Long removeRank(String scoreRank,long start,long end){
        return scoreRankService.removeRank(scoreRank,start,end);
    }

    /**
     * 删除排名
     */

    public Long removeRankAll(String scoreRank){
        return scoreRankService.removeRank(scoreRank,0L,zCard(scoreRank));
    }



}
