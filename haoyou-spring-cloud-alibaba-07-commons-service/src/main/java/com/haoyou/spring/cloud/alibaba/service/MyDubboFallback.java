package com.haoyou.spring.cloud.alibaba.service;

import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallback;
import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallbackRegistry;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * dubbo+sentinel熔断
 */
@Component
public class MyDubboFallback implements DubboFallback {
    private final static Logger logger = LoggerFactory.getLogger(MyDubboFallback.class);
    public MyDubboFallback() {
        DubboFallbackRegistry.setConsumerFallback(this);
        DubboFallbackRegistry.setProviderFallback(this);
    }

//    @Override
//    public Result handle(Invoker<?> invoker, Invocation invocation, BlockException e) {
//        logger.info(String.format("dubbo熔断机制：$s",e.getMessage()));
//        e.printStackTrace();
//        return null;
//    }

    @Override
    public org.apache.dubbo.rpc.Result handle(org.apache.dubbo.rpc.Invoker<?> invoker, org.apache.dubbo.rpc.Invocation invocation, BlockException e) {
        logger.info(String.format("dubbo熔断机制：$s",e.getMessage()));
        e.printStackTrace();
        return null;
    }
}
