package com.haoyou.spring.cloud.alibaba.serialization;


import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.Serializer;
import com.alipay.remoting.serialization.SerializerManager;
import com.haoyou.spring.cloud.alibaba.commons.util.ByteArrayUtil;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

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

    @Override
    public byte[] serialize(Object obj) throws CodecException {

        try {
            String msg = MapperUtils.obj2jsonIgnoreNull(obj);
            byte[] bytes = msg.getBytes("UTF-8");
//            加密
//            bytes = encrypt(bytes);
            return bytes;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T deserialize(byte[] data, String classOfT) throws CodecException {

        try {
//            解密
//            data = decrypt(data);
//            Console.log(new String(data, "UTF-8"));

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
