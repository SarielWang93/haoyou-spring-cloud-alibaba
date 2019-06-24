package com.haoyou.spring.cloud.alibaba.config;

import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.redis.RedisObjKV;
import org.apache.dubbo.common.serialize.support.SerializationOptimizer;

import java.util.*;

/**
 * kryo快速序列化，序列化类注册机
 */
public class SerializationOptimizerImpl implements SerializationOptimizer {
    public Collection<Class> getSerializableClasses() {
        List<Class> classes = new LinkedList<Class>();
        classes.add(MyRequest.class);
        classes.add(Pet.class);
        classes.add(User.class);
        classes.add(RedisObjKV.class);
        classes.add(Map.class);
        classes.add(List.class);
        classes.add(Set.class);
        classes.add(BaseMessage.class);
        classes.add(MapBody.class);
        classes.add(Collection.class);
        return classes;
    }
}
