package com.haoyou.spring.cloud.alibaba;


import cn.hutool.core.lang.Console;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Protocol;
import com.haoyou.spring.cloud.alibaba.commons.mapper.ProtocolMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;

import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import sun.misc.BASE64Decoder;
import sun.security.provider.X509Factory;


import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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


    public static void main( String[] args )
    {



    }


}
