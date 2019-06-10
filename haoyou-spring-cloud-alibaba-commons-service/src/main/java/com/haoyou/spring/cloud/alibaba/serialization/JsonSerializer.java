package com.haoyou.spring.cloud.alibaba.serialization;


import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.Serializer;
import com.alipay.remoting.serialization.SerializerManager;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.ZIP;
import org.springframework.stereotype.Service;

/**
 * SOFABolt序列化器扩展
 * 自己扩展的序列化方法，json传送信息，所以直接字符串序列化，字符集用UTF-8
 * TODO 可以在此处实现加密
 */
@Service
public class JsonSerializer implements Serializer {
    //本序列化器的编号
    public static final byte JsonSerializerCode = 2;

    private static final String KEY="0CoJUm6Qyw8W8jud";

    private AES aes ;

    public JsonSerializer() {
        this.aes = SecureUtil.aes(KEY.getBytes());
        SerializerManager.addSerializer(this.JsonSerializerCode,this);
    }

    /**
     * 信息传递最外层序列化
     * @param obj
     * @return
     * @throws CodecException
     */
    @Override
    public byte[] serialize(Object obj) throws CodecException {
        return zipSerialize(obj);
    }

    /**
     * 信息传递最外层反序列化
     * @param data
     * @param classOfT
     * @param <T>
     * @return
     * @throws CodecException
     */
    @Override
    public <T> T deserialize(byte[] data, String classOfT) throws CodecException {
        return zipDeserialize(data,classOfT);
    }


    /**
     * 序列化
     * @param obj
     * @return
     */
    public byte[] noZipSerialize(Object obj) throws CodecException{
        if(obj==null){
            return null;
        }
        try {
            String msg = MapperUtils.obj2jsonIgnoreNull(obj);
            byte[] bytes = msg.getBytes("UTF-8");
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 反序列化
     * @param data
     * @param classOfT
     * @param <T>
     * @return
     */
    public <T> T noZipDeserialize(byte[] data, String classOfT) throws CodecException{
        if(data==null||classOfT==null||data.length==0){
            return null;
        }
        try {
            return (T) MapperUtils.json2pojo( new String(data, "UTF-8"),Class.forName(classOfT));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 信息传递最外层序列化
     * @param obj
     * @return
     * @throws CodecException
     */
    public byte[] zipSerialize(Object obj) throws CodecException {
        if(obj==null){
            return null;
        }
        try {
            String msg = MapperUtils.obj2jsonIgnoreNull(obj);
            byte[] bytes = msg.getBytes("UTF-8");
            bytes=ZIP.gZip(bytes);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 信息传递最外层反序列化
     * @param data
     * @param classOfT
     * @param <T>
     * @return
     * @throws CodecException
     */
    public <T> T zipDeserialize(byte[] data, String classOfT) throws CodecException {
        if(data==null||classOfT==null||data.length==0){
            return null;
        }
        try {
            //gzip解压后使用
            data=ZIP.unGZip(data);
            return (T) MapperUtils.json2pojo( new String(data, "UTF-8"),Class.forName(classOfT));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * 加密
     * @param b
     * @return
     */
    private byte[] encrypt(byte[] b){
        return aes.encrypt(b);
    }

    /**
     * 解密
     * @param b
     * @return
     */
    private byte[] decrypt(byte[] b){
        return aes.decrypt(b);
    }
}
