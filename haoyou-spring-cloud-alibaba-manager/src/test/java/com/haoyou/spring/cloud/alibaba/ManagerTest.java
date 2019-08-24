package com.haoyou.spring.cloud.alibaba;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.Serializer;
import com.alipay.remoting.serialization.SerializerManager;
import com.haoyou.spring.cloud.alibaba.commons.entity.Fund;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.HiFightingRoomMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.HttpUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.ZIP;
import com.haoyou.spring.cloud.alibaba.redis.service.RedisService;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.serialization.JsonSerializer;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ManagerTest {
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    RedisService redisService;
    @Autowired
    JsonSerializer jsonSerializer;
    @Autowired
    UserMapper userMapper;
    @Autowired
    HiFightingRoomMapper hiFightingRoomMapper;
    @Autowired
    ScoreRankService scoreRankService;


    @Test
    public void contextLoads() throws Exception {

    }
    @Test
    public void contextLoads2() throws Exception {
        HashMap<String, String> map = redisService.getlkMap("user:name:*");
        Console.log(map);
    }
    @Test
    public void contextLoads3() throws Exception {
        User user =new User();
        user.setUid("ec12ffde5b2447d6bbc758421ba9");

        user = userMapper.selectOne(user);

        //sendMsgService.sendMsgAll(SendType.MATCH_READY,user.toJson());
        Console.log(user);
    }



    public static void main(String[] args) throws InterruptedException, CodecException {

    }

    /**
     * 敏感词
     * @param str
     * @throws InterruptedException
     */
    public static void mgc(String str) throws InterruptedException {

        String host = "http://monitoring.market.alicloudapi.com";
        String path = "/neirongjiance";
        String method = "POST";
        String appcode = "cc7cfea9a84440119c153aad46cc8742";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("in", str);


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            //获取response的body
            String s = EntityUtils.toString(response.getEntity());
            Console.log(s);

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    /**
     * 身份证查询
     * @param card
     * @throws InterruptedException
     */
    public static void idCard(String card){

        String host = "https://jisuidcard.market.alicloudapi.com";
        String path = "/idcard/query";
        String method = "GET";
        String appcode = "cc7cfea9a84440119c153aad46cc8742";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("idcard", card);


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            //获取response的body
            String s = EntityUtils.toString(response.getEntity());
            Console.log(s);

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    /**
     * 聊天机器人
     * @param str
     */
    public static void iqa(String str) {

        String host = "https://jisuiqa.market.alicloudapi.com";
        String path = "/iqa/query";
        String method = "GET";
        String appcode = "cc7cfea9a84440119c153aad46cc8742";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("question", str);


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            //获取response的body
            String s = EntityUtils.toString(response.getEntity());
            Console.log(s);

        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
