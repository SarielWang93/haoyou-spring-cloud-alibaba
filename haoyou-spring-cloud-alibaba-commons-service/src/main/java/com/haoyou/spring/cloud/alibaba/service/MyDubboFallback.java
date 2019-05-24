package com.haoyou.spring.cloud.alibaba.service;

import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallback;
import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallbackRegistry;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import org.springframework.stereotype.Component;

/**
 * dubbo+sentinel熔断
 */
@Component
public class MyDubboFallback implements DubboFallback {

    public MyDubboFallback() {
        DubboFallbackRegistry.setConsumerFallback(this);
    }

    @Override
    public Result handle(Invoker<?> invoker, Invocation invocation, BlockException e) {
        e.printStackTrace();
        return null;
    }
}
