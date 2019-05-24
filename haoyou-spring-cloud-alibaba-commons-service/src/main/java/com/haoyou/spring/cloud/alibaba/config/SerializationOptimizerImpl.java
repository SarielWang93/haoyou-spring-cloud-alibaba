package com.haoyou.spring.cloud.alibaba.config;

import com.alibaba.dubbo.common.serialize.support.SerializationOptimizer;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * kryo快速序列化，序列化类注册机，暂时不用
 */
public class SerializationOptimizerImpl implements SerializationOptimizer {
    public Collection<Class> getSerializableClasses() {
        List<Class> classes = new LinkedList<Class>();
        classes.add(User.class);
        classes.add(Pet.class);
        classes.add(StackTraceElement.class);
        return classes;
    }
}
