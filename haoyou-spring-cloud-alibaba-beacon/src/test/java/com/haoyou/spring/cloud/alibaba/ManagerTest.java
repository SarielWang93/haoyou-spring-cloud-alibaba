package com.haoyou.spring.cloud.alibaba;


import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Protocol;
import com.haoyou.spring.cloud.alibaba.commons.mapper.ProtocolMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;

import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ManagerTest {

    @Autowired
    ScoreRankService scoreRankService;
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private ProtocolMapper protocolMapper;



    @Test
    public void contextLoads() throws Exception {

    }


    public static void main(String[] args) {

    }
}
