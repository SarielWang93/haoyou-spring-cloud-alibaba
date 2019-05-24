package com.haoyou.spring.cloud.alibaba.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;


import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.LongAdder;

/**
 * 解决同时拥有多个Scheduler标签（定时操作）线程阻塞问题
 */
@Configuration
public class SchedulingConfiguration implements SchedulingConfigurer{
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        // 配置一个线程工程
        taskRegistrar.setScheduler(new ScheduledThreadPoolExecutor(10,
                new ThreadFactory() {
                    LongAdder num = new LongAdder();
                    @Override
                    public Thread newThread(Runnable r) {
                        num.add(1l);
                        return new Thread(r,String .format("schedule-%s",num.longValue()));
                    }
                }));
    }

}
