package com.haoyou.spring.cloud.alibaba.commons.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 通过该类即可在普通工具类里获取spring管理的bean
 *
 */
@Component
public class SpringTool implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context)
            throws BeansException {
        SpringTool.applicationContext = context;
    }
    public static <T>T getBean(String classOfT){
        return (T)applicationContext.getBean(classOfT);
    }

}