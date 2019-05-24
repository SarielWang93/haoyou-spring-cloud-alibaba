package com.haoyou.spring.cloud.alibaba.action;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.Serializer;
import com.alipay.remoting.serialization.SerializerManager;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.MapBody;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.service.sofabolt.SendMsgService;
import com.haoyou.spring.cloud.alibaba.serialization.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Collection;

/**
 * 发送信息工具类（序列化并发送信息，并且封装信息系列化）
 */
@Service
public class SendMsgUtil implements Serializable {
    private final static Logger logger = LoggerFactory.getLogger(SendMsgUtil.class);
    private static final long serialVersionUID = -72133336896544571L;
    @Reference(version = "${send-msg.service.version: 1.0.0}")
    private SendMsgService sendMsgService;

    private Serializer serializer;

    @PostConstruct
    private void init() {

    }

    /**
     * 发送信息T，并返回信息M
     *
     * @param userUid
     * @param type
     * @param baseMessage
     * @return
     */
    public MyRequest sendMsgOne(String userUid, Integer type, BaseMessage baseMessage) {
        byte[] serialize = serialize(baseMessage);

        MyRequest myRequest = sendMsgService.sendMsgOne(getreqstr(userUid, serialize, type));

        return myRequest;

    }

    /**
     * 发送信息T，无返回信息
     *
     * @param userUid
     * @param type
     * @param baseMessage
     * @return
     */
    public boolean sendMsgOneNoReturn(String userUid, Integer type, BaseMessage baseMessage) {
        logger.info(String.format("sendMsgList: %s %s", userUid, baseMessage));

        byte[] serialize = this.serialize(baseMessage);

        sendMsgService.sendMsgOneNoReturn(getreqstr(userUid, serialize, type));

        return true;
    }

    /**
     * 全员广播 信息T
     *
     * @param type
     * @param baseMessage
     * @return
     */
    public boolean sendMsgAll(Integer type, BaseMessage baseMessage) {

        byte[] serialize = this.serialize(baseMessage);

        return sendMsgService.sendMsgAll(getreqstr(null, serialize, type));
    }


    /**
     * 针对群体广播 信息T
     *
     * @param userUids
     * @param type
     * @param baseMessage
     * @param <T>
     * @return
     */
    public <T> boolean sendMsgList(Collection<String> userUids, Integer type, BaseMessage baseMessage) {
        logger.info(String.format("sendMsgList: %s %s", userUids, baseMessage));

        byte[] serialize = this.serialize(baseMessage);
        for (String userUid : userUids) {
            sendMsgService.sendMsgOneNoReturn(getreqstr(userUid, serialize, type));
        }
        return true;
    }


    /**
     * 通信信息序列化
     *
     * @param baseMessage
     * @return
     */
    public byte[] serialize(BaseMessage baseMessage) {


        Object eve = null;
        eve = baseMessage;

        /**
         * 处理MapBody类型信息
         */
        if (baseMessage instanceof MapBody) {
            MapBody mb = (MapBody) baseMessage;
            mb.getMsg().put("state", mb.getState());
            eve = mb.getMsg();
        }


        try {
            byte[] serialize = this.getSerializer().serialize(eve);

            return serialize;
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
    public <T> T deserialize(byte[] bt, Class<T> aclass) {

        try {
            return this.getSerializer().deserialize(bt, aclass.getName());
        } catch (CodecException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Serializer getSerializer(){
        if(this.serializer==null){
            this.serializer = SerializerManager.getSerializer(JsonSerializer.JsonSerializerCode);
        }
        return this.serializer;
    }


    /**
     * 封装发送信息json
     *
     * @param msg
     * @return
     */
    private MyRequest getreqstr(String userUid, byte[] msg, Integer type) {

        MyRequest req = new MyRequest();
        req.setUseruid(userUid);
        req.setId(type);
        req.setMsg(msg);
        return req;

    }

}
