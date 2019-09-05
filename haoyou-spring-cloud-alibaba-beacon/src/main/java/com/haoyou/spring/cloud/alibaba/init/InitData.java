package com.haoyou.spring.cloud.alibaba.init;


import cn.hutool.core.lang.Console;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Protocol;
import com.haoyou.spring.cloud.alibaba.commons.mapper.ProtocolMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.email.CheckEmail;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
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

    public static MqttClient client;

    @Autowired
    private CheckEmail checkEmail;
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private ProtocolMapper protocolMapper;

    @Override
    public void run(ApplicationArguments args) {

        //test();

        mqttClient();

        catchBeacons();

        checkEmail.getEmails(CheckEmail.user2, CheckEmail.password2, CheckEmail.OUTLOOK_HOST);
    }

    private void mqttClient() {

        String endpoint = "tcp://zq3e4fc.mqtt.iot.gz.baidubce.com:1883";    //输入创建endpoint返回的SSL地址
        String username = "zq3e4fc/ernest"; //输入创建thing返回的username
        String topic = "bd_upload"; //订阅的消息主题
        String password = "2on6TswfZ40oDDQc";
        String clientId = "a6e296254fca48d09be09f9073272d";

        try {


            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());
//            System.out.println("initial client");
            MemoryPersistence persistence = new MemoryPersistence();

            //java-client为标识设备的ID，用户可自己定义，在同一个实例下，每个实体设备需要有一个唯一的ID
            client = new MqttClient(endpoint, clientId, persistence);
//            System.out.println("Connecting to broker: "+endpoint);

            client.connect(options);
//            System.out.println("Connected");
//            System.out.println("subscribing topic");


            client.subscribe(topic, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    byte[] payload = message.getPayload();
                    Console.log(new String(payload));
                }
            });
//            Thread.sleep(100000000);

//            MqttMessage message = new MqttMessage();
//            message.setPayload("15".getBytes());
//            System.out.println("publishing msg to broker");
//            client.publish(topic, message);


//            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private void test() {


        //$AGPS,20190823051206,3904.12574,N,11701.49359,E,*
        List<String> msgs = new ArrayList<>();
//        msgs.add("$AGPS,9,190823051206,3904.12574,N,11701.49359,E,*");
//        $AGPS,20190823064323,3904.15007,N,11701.52242,E,*
//        msgs.add("$AGPS,9,190823064323,3904.15007,N,11701.52242,E,*");

         String msg = "$AGPS,9,190823131357,3904.14945,N,11701.49821,E,*";

         Protocol protocol = new Protocol();
         protocol.analysis("AGPS",msg);
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
