package com.haoyou.spring.cloud.alibaba.serialization;


import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.Serializer;
import com.alipay.remoting.serialization.SerializerManager;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.ZIP;
import com.haoyou.spring.cloud.alibaba.util.KryoUtil;
import org.springframework.stereotype.Service;

/**
 * SOFABolt序列化器扩展
 * 自己扩展的序列化方法，Kryo序列化
 * TODO 可以在此处实现加密
 */
@Service
public class KryoSerializer implements Serializer {
    //本序列化器的编号
    public static final byte KryoSerializerCode = 3;

    private static final String KEY = "0CoJUm6Qyw8W8jud";

    private AES aes;

    public KryoSerializer() {
        this.aes = SecureUtil.aes(KEY.getBytes());
        SerializerManager.addSerializer(this.KryoSerializerCode, this);
    }

    /**
     * 信息传递最外层序列化
     *
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
     *
     * @param data
     * @param classOfT
     * @param <T>
     * @return
     * @throws CodecException
     */
    @Override
    public <T> T deserialize(byte[] data, String classOfT) throws CodecException {
        return zipDeserialize(data);
    }


    /**
     * 序列化
     *
     * @param t
     * @return
     */
    public <T> byte[]  noZipSerialize(T t) throws CodecException {
        if (t == null) {
            return null;
        }
        try {

            byte[] bytes = KryoUtil.writeToByteArray(t);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 反序列化
     *
     * @param data
     * @param <T>
     * @return
     */
    public <T> T noZipDeserialize(byte[] data) throws CodecException {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            return (T) KryoUtil.readFromByteArray(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 信息传递最外层序列化
     *
     * @param t
     * @return
     * @throws CodecException
     */
    public <T> byte[] zipSerialize(T t) throws CodecException {
        if (t == null) {
            return null;
        }

        byte[] bytes = noZipSerialize(t);
        bytes = ZIP.gZip(bytes);
//            Console.log(bytes.length);
//            Console.log(Arrays.toString(bytes));
        return bytes;

    }

    /**
     * 信息传递最外层反序列化
     *
     * @param data
     * @param <T>
     * @return
     * @throws CodecException
     */
    public <T> T zipDeserialize(byte[] data) throws CodecException {
        if (data == null || data.length == 0) {
            return null;
        }

        //gzip解压后使用
        data = ZIP.unGZip(data);
        return (T) noZipDeserialize(data);


    }


    /**
     * 加密
     *
     * @param b
     * @return
     */
    private byte[] encrypt(byte[] b) {
        return aes.encrypt(b);
    }

    /**
     * 解密
     *
     * @param b
     * @return
     */
    private byte[] decrypt(byte[] b) {
        return aes.decrypt(b);
    }
}
