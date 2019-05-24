package com.haoyou.spring.cloud.alibaba.service.redis;


import java.util.List;
import java.util.Map;

/**
 * 基于redis排行榜系统
 */

public interface ScoreRankService {
    /**
     * 批量新增
     */

    boolean batchAdd(String scoreRank, Map<String, Long> msgs);

    /**
     * 单个新增
     */

    boolean add(String scoreRank, String userUid, Long rank);

    /**
     * 获取排行列表
     */

    List<String> list(String scoreRank, Long start, Long end);


    /**
     * 获取单个的排行
     */

    Long find(String scoreRank, String userUid);


    /**
     * 使用加法操作分数
     */

    Long incrementScore(String scoreRank, String userUid, Long rank);


    /**
     * 获取整个集合的基数(数量大小)
     */

    Long zCard(String scoreRank);
}
