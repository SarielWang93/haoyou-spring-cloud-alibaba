package com.haoyou.spring.cloud.alibaba.sofabolt.server;

import com.alipay.remoting.Configs;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.serialization.SerializerManager;
import com.haoyou.spring.cloud.alibaba.sofabolt.processor.MyServerUserProcessor;
import com.haoyou.spring.cloud.alibaba.serialization.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
@Order(value = 1)
public class MyServer  implements ApplicationRunner {
    private final static Logger logger = LoggerFactory.getLogger(MyServer.class);

    @Autowired
    private MyServerUserProcessor myServerUserProcessor;

    @Value("${sofabolt.server.threadpool.minsize: 10}")
    private String TP_MIN_SIZE;
    @Value("${sofabolt.server.threadpool.maxsize: 100}")
    private String TP_MAX_SIZE;
    @Value("${sofabolt.server.threadpool.queuesize: 120}")
    private String TP_QUEUE_SIZE;

    @Value("${sofabolt.server.port: 12200}")
    private int port;

    public static RpcServer server;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (this.start()) {
            logger.info("创建RpcServer实例成功");
            logger.info("启动端口："+port);
            logger.info("线程池最小线程数："+TP_MIN_SIZE);
            logger.info("线程池最大线程数："+TP_MAX_SIZE);
            logger.info("线程池队列大小："+TP_QUEUE_SIZE);
        } else {
            logger.error("创建RpcServer实例失败！！");
        }
    }



    /**
     * 创建 RpcServer 实例，并做一些初始化操作
     * @return
     */
    public boolean start() {
        //设置默认的线程池大小

        System.setProperty(Configs.TP_MIN_SIZE, TP_MIN_SIZE);
        System.setProperty(Configs.TP_MAX_SIZE, TP_MAX_SIZE);
        System.setProperty(Configs.TP_QUEUE_SIZE, TP_QUEUE_SIZE);

        /**
         * 注册自己的序列化器
         */
        SerializerManager.addSerializer(JsonSerializer.JsonSerializerCode, new JsonSerializer());
        /**
         * 启用注册的序列化器
         */
        System.setProperty(Configs.SERIALIZER, String.valueOf(JsonSerializer.JsonSerializerCode));
        /**
         * 创建 RpcServer 实例，指定监听 port
         */
        server = new RpcServer(port,true);
        /**
         * 注册业务逻辑处理器 UserProcessor
         */
        server.registerUserProcessor(myServerUserProcessor);
        /**
         * 启动服务端：先做 netty 配置初始化操作，再做 bind 操作
         * 配置 netty 参数两种方式：[SOFABolt 源码分析11 - Config 配置管理的设计](https://www.jianshu.com/p/76b0be893745)
         */
        boolean start = server.start();


        return start;
    }
}