package com.haoyou.spring.cloud.alibaba.service;

import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallback;
import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallbackRegistry;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcResult;
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

    @Override
    public Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex) {
        // Just wrap and throw the exception.
        logger.info(String.format("dubbo熔断机制：$s",invocation.getMethodName()));
        return new RpcResult(ex);
    }
}
