package com.haoyou.spring.cloud.alibaba.util;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.Serializer;
import com.alipay.remoting.serialization.SerializerManager;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.RedisObjKV;
import com.haoyou.spring.cloud.alibaba.service.redis.RedisObjectService;
import com.haoyou.spring.cloud.alibaba.serialization.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;

/**
 * redis存储对象
 */
@Service
public class RedisObjectUtil {
    private final static Logger logger = LoggerFactory.getLogger(RedisObjectUtil.class);
    @Reference(version = "${redis-object.service.version}")
    private RedisObjectService redisObjectService;

    private Serializer serializer;

    @PostConstruct
    private void init() {
    }
    /**
     * redis对象存储
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    public <T> boolean save( String key, T value){

        //logger.info(String.format("save  %s",key));
        return redisObjectService.save(new RedisObjKV(key, this.serialize(value)));
    }

    /**
     * redis对象存储，设定超时时间
     * @param key
     * @param value
     * @param timeout
     * @param <T>
     * @return
     */
    public <T> boolean save( String key, T value,long timeout){
        //logger.info(String.format("save  %s:%s",key,timeout));
        return redisObjectService.save(new RedisObjKV(key, this.serialize(value)),timeout);

    }

    /**
     * 获取对象
     * @param key
     * @param aclass
     * @param <T>
     * @return
     */
    public <T> T get(String key,Class<T> aclass){
        //logger.info(String.format("get %s",key));
        RedisObjKV redisObjKV = redisObjectService.get(key);

        return this.deserialize(redisObjKV.getVal(),aclass);

    }

    /**
     * 模糊查询对象
     * @param key
     * @param aclass
     * @param <T>
     * @return
     */
    public <T>HashMap<String, T> getlkMap(String key,Class<T> aclass){
//        if(!key.contains(RedisKey.MATCH_PLAYER_POOL)){
//            logger.info(String.format("getlkMap %s",key));
//        }

        HashMap<String, T> rm=new HashMap<>();
        List<RedisObjKV> lkList = redisObjectService.getlkMap(key);

        for(RedisObjKV redisObjKV:lkList){
            rm.put(redisObjKV.getKey(),this.deserialize(redisObjKV.getVal(), aclass));
        }

        return rm;
    }

    /**
     * 删除对象
     * @param key
     * @return
     */
    public boolean delete(String key){
        //logger.info(String.format("delete %s",key));
        return redisObjectService.delete(key);
    }



    /**
     * 通信信息序列化
     *
     * @param t
     * @return
     */
    private <T> byte[] serialize(T t) {

        try {
            return this.getSerializer().serialize(t);
        } catch (CodecException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通信信息反序列化
     *
     * @param bt
     * @param aclass
     * @param <T>
     * @return
     */
    private <T> T deserialize(byte[] bt, Class<T> aclass) {

        try {
            return this.getSerializer().deserialize(bt, aclass.getName());
        } catch (CodecException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @return
     */
    private Serializer getSerializer(){
        if(this.serializer==null){
            this.serializer = SerializerManager.getSerializer(JsonSerializer.JsonSerializerCode);
        }
        return this.serializer;
    }




}
