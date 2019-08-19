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

        msgs.forEach((user, rank) -> {
            DefaultTypedTuple<String> tuple = new DefaultTypedTuple<String>(user, rank.doubleValue());
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
    public TreeMap<Long,String> list(String scoreRank,Long start, Long end) {
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(scoreRank, start, end);
        TreeMap<Long,String> treeMap = new TreeMap<>();
        for(ZSetOperations.TypedTuple<String> typedTuple:typedTuples){
            Double score = typedTuple.getScore();
            String value = typedTuple.getValue();
            long key = score.longValue();
            for(long l = key;treeMap.get(l)!=null;l++){
                key = l;
            }
            treeMap.put(key,value);
        }


        return treeMap;
    }

    @Override
    public Long find(String scoreRank,String userUid) {
        Long rankNum = null;
        try {
            rankNum = redisTemplate.opsForZSet().reverseRank(scoreRank, userUid);
        }catch (Exception e){
            e.printStackTrace();
        }
        return rankNum;
    }

    @Override
    public Long findIntegral(String scoreRank,String userUid) {
        Long rankNum = null;
        try {
            rankNum = redisTemplate.opsForZSet().score(scoreRank, userUid).longValue();
        }catch (Exception e){
            e.printStackTrace();
        }
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


    @Override
    public Long removeRank(String scoreRank,long start,long end) {
        long aLong = redisTemplate.opsForZSet().removeRange(scoreRank,start,end);
        return aLong;
    }
}
