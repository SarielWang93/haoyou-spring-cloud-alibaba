package com.haoyou.spring.cloud.alibaba.init;


import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Protocol;
import com.haoyou.spring.cloud.alibaba.commons.mapper.ProtocolMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.email.CheckEmail;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@Component
@Order(value = 1)
public class InitData implements ApplicationRunner {

    @Autowired
    private CheckEmail checkEmail;
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private ProtocolMapper protocolMapper;

    @Override
    public void run(ApplicationArguments args) {

        //test();

        catchBeacons();

        checkEmail.getEmails(CheckEmail.user2, CheckEmail.password2, CheckEmail.OUTLOOK_HOST);
    }

    private void test() {


        //$AGPS,20190823051206,3904.12574,N,11701.49359,E,*
        List<String> msgs = new ArrayList<>();
//        msgs.add("$AGPS,9,190823051206,3904.12574,N,11701.49359,E,*");
//        $AGPS,20190823064323,3904.15007,N,11701.52242,E,*
//        msgs.add("$AGPS,9,190823064323,3904.15007,N,11701.52242,E,*");

         String msg = "$AGPS,9,190823131357,3904.14945,N,11701.49821,E,*";

         Protocol protocol = new Protocol();
         protocol.setDeviceIdNum("AGPS");
         protocol.setText(msg);
         protocol.analysis(msg);
         protocolMapper.insertSelective(protocol);

    }

    /**
     * 从数据库缓存到redis
     */
    private void catchBeacons() {

        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.BEACON));

        List<Protocol> protocols = protocolMapper.selectAll();

        for (Protocol protocol : protocols) {
            redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.BEACON, protocol.getDeviceIdNum(), protocol.getId().toString()), protocol, CheckEmail.SECONDS);
        }

    }


}
