package com.haoyou.spring.cloud.alibaba.util;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.Serializer;
import com.alipay.remoting.serialization.SerializerManager;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.redis.RedisObjKV;
import com.haoyou.spring.cloud.alibaba.redis.service.RedisObjectService;
import com.haoyou.spring.cloud.alibaba.commons.util.ZIP;
import com.haoyou.spring.cloud.alibaba.serialization.KryoSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * redis存储对象
 */
@Service
public class RedisObjectUtil {

    private final static Logger logger = LoggerFactory.getLogger(RedisObjectUtil.class);
    @Autowired
    private RedisObjectService redisObjectService;

    private Serializer serializer;

    @PostConstruct
    private void init() {
    }

    /**
     * redis对象存储
     *
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    public <T> boolean save(String key, T value) {

        //logger.info(String.format("save  %s",key));
        return redisObjectService.save(new RedisObjKV(key, this.serialize(value)));
    }

    /**
     * redis对象存储，设定超时时间
     *
     * @param key
     * @param value
     * @param timeout
     * @param <T>
     * @return
     */
    public <T> boolean save(String key, T value, long timeout) {
        //logger.info(String.format("save  %s:%s",key,timeout));
        return redisObjectService.save(new RedisObjKV(key, this.serialize(value)), timeout);

    }

    /**
     * 获取对象
     *
     * @param key
     * @param aclass
     * @param <T>
     * @return
     */
    public <T> T get(String key, Class<T> aclass) {
        //logger.info(String.format("get %s",key));
        RedisObjKV redisObjKV = redisObjectService.get(key);
        if (redisObjKV.getVal() != null) {
            return this.deserialize(redisObjKV.getVal(), aclass);
        }

        return null;
    }

    /**
     * 模糊查询对象
     *
     * @param key
     * @param aclass
     * @param <T>
     * @return
     */
    public <T> HashMap<String, T> getlkMap(String key, Class<T> aclass) {
//        if(!key.contains(RedisKey.MATCH_PLAYER_POOL)){
//            logger.info(String.format("getlkMap %s",key));
//        }

        HashMap<String, T> rm = new HashMap<>();
        List<RedisObjKV> lkList = redisObjectService.getlkMap(key);

        for (RedisObjKV redisObjKV : lkList) {
            rm.put(redisObjKV.getKey(), this.deserialize(redisObjKV.getVal(), aclass));
        }

        return rm;
    }

    /**
     * 删除对象
     *
     * @param key
     * @return
     */
    public boolean delete(String key) {
        //logger.info(String.format("delete %s",key));
        return redisObjectService.delete(key);
    }

    /**
     * 删除所有对象
     *
     * @param key
     * @return
     */
    public boolean deleteAll(String key) {
        //logger.info(String.format("deleteAll %s",key));
        List<RedisObjKV> lkList = redisObjectService.getlkMap(key);

        for (RedisObjKV redisObjKV : lkList) {
            redisObjectService.delete(redisObjKV.getKey());
        }


        return true;
    }

    /**
     * 刷新过期时间
     *
     * @param key
     * @return
     */
    public boolean refreshTime(String key) {
        return redisObjectService.refreshTime(key);
    }


    /**
     * 备份
     *
     */
    public void backup() {

        List<String> keys = new ArrayList<>();
        keys.add(RedisKey.USER_AWARD);

        List<RedisObjKV> lkList = new ArrayList<>();

        for(String key:keys){
            lkList.addAll(redisObjectService.getlkMap(RedisKeyUtil.getlkKey(key)));
        }


        List<String> lines = new ArrayList<>();
        for (RedisObjKV redisObjKV : lkList) {
            try {
                lines.add(MapperUtils.obj2json(redisObjKV));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DateTime date = DateUtil.date();
        String HHmmss = date.toString("HHmmss");

        String yyyyMMdd = date.toString("yyyyMMdd");

        String usrHome = System.getProperty("user.home");
        String url = String.format("%s/logs/XXL/backup/%s/%s.backup", usrHome, yyyyMMdd, HHmmss);

        //Console.log(url);

        File file = FileUtil.file(url);

        File touch = FileUtil.touch(file);

        FileWriter fileWriter = FileWriter.create(touch);

        fileWriter.appendLines(lines);

    }

    /**
     * 加载备份
     *
     */
    public void inputBackup(String fileUrl) {

        File file;

        if(StrUtil.isEmpty(fileUrl)){

            DateTime date = DateUtil.date();

            String yyyyMMdd = date.toString("yyyyMMdd");

            String usrHome = System.getProperty("user.home");
            String url = String.format("%s/logs/XXL/backup/%s/", usrHome, yyyyMMdd);

            File[] ls = FileUtil.ls(url);

            if(ls.length == 0){
                date = DateUtil.offsetDay(date,-1);
                yyyyMMdd = date.toString("yyyyMMdd");
                url = String.format("%s/logs/XXL/backup/%s/", usrHome, yyyyMMdd);
                ls = FileUtil.ls(url);
            }
            file = ls[ls.length-1];
        }else{
            file = FileUtil.file(fileUrl);
        }

        FileReader fileReader = FileReader.create(file);

        List<String> strings = fileReader.readLines();

        for (String json : strings) {
            try {
                RedisObjKV redisObjKV = MapperUtils.json2pojo(json, RedisObjKV.class);

                redisObjectService.save(redisObjKV);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }


    /**
     * 通信信息序列化
     *
     * @param t
     * @return
     */
    public <T> byte[] serialize(T t) {

        try {
            return ZIP.gZip(this.getSerializer().serialize(t));
        } catch (Exception e) {
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
    public <T> T deserialize(byte[] bt, Class<T> aclass) {

        try {
            return this.getSerializer().deserialize(ZIP.unGZip(bt), aclass.getName());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return
     */
    private Serializer getSerializer() {
        if (this.serializer == null) {
//            this.serializer = SerializerManager.getSerializer(JsonSerializer.JsonSerializerCode);
//            this.serializer = SerializerManager.getSerializer(KryoSerializer.KryoSerializerCode);
            this.serializer = SerializerManager.getSerializer(SerializerManager.Hessian2);
        }
        return this.serializer;
    }


}
