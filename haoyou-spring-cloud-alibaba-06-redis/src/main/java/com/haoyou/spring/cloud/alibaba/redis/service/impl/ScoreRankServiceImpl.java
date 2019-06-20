package com.haoyou.spring.cloud.alibaba.redis.service.impl;

import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScoreRankServiceImpl implements ScoreRankService {
    @Autowired
    private StringRedisTemplate redisTemplate;


    @Override
    public boolean batchAdd(String scoreRank, Map<String,Long> msgs) {

        Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();

        msgs.forEach((userUid, rank) -> {
            DefaultTypedTuple<String> tuple = new DefaultTypedTuple<String>(userUid, rank.doubleValue());
            tuples.add(tuple);
        });
        Long num = redisTemplate.opsForZSet().add(scoreRank, tuples);

        return true;
    }

    @Override
    public boolean add(String scoreRank,String userUid,Long rank) {
        redisTemplate.opsForZSet().add(scoreRank,userUid, rank.doubleValue());
        return true;
    }

    @Override
    public List<String> list(String scoreRank,Long start, Long end) {
        Set<String> range = redisTemplate.opsForZSet().reverseRange(scoreRank, start, end);
        List<String> userUids=new ArrayList<>();
        userUids.addAll(range);
        return userUids;
    }

    @Override
    public Long find(String scoreRank,String userUid) {
        long rankNum = redisTemplate.opsForZSet().reverseRank(scoreRank, userUid);
        return rankNum;
    }

    @Override
    public Long incrementScore(String scoreRank,String userUid,Long rank) {
        return redisTemplate.opsForZSet().incrementScore(scoreRank, userUid, 1D+rank).longValue();
    }

    @Override
    public Long zCard(String scoreRank) {
        long aLong = redisTemplate.opsForZSet().zCard(scoreRank);
        return aLong;
    }
}
