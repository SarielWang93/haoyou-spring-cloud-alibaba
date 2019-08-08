package com.haoyou.spring.cloud.alibaba.util;

import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import org.apache.dubbo.config.annotation.Reference;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
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

    private JsonSerializer serializer;

    @PostConstruct
    private void init() {

    }

    /**
     * sofabolt服务绕过调用自己
     *
     * @param sendMsgService
     */
    public void setSendMsgService(SendMsgService sendMsgService){
        this.sendMsgService = sendMsgService;
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
        logger.info(String.format("sendMsgOne: %s %s %s", userUid, type , baseMessage));
        byte[] serialize = this.serialize(baseMessage);

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
        logger.info(String.format("sendMsgOneNoReturn: %s %s %s", userUid, type , baseMessage));

        byte[] serialize = this.serialize(baseMessage);
        if(!userUid.startsWith("ai-")) {
            sendMsgService.sendMsgOneNoReturn(getreqstr(userUid, serialize, type));
        }
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
        logger.info(String.format("sendMsgAll: %s %s", type , baseMessage));
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
        logger.info(String.format("sendMsgList: %s %s %s", userUids, type , baseMessage));

        byte[] serialize = this.serialize(baseMessage);
        for (String userUid : userUids) {
            if(!userUid.startsWith("ai-")){
                sendMsgService.sendMsgOneNoReturn(getreqstr(userUid, serialize, type));
            }
        }
        return true;
    }


    /**
     * 判断用户是否在线
     * @param userUid
     * @return
     */
    public boolean connectionIsAlive(String userUid) {
        return sendMsgService.connectionIsAlive(userUid);
    }

    /**
     * 强制下线
     * @param users
     * @return
     */
    public void sendDownUserList(Collection<User> users) {
        for(User user : users){
            sendDown(user.getUid());
        }
    }
    public void sendDownList(Collection<String> userUids) {
        for(String userUid : userUids){
            sendDown(userUid);
        }
    }
    public void sendDown(String userUid) {
        sendMsgService.sendDown(userUid);
    }





    /**
     * 通信信息序列化
     *
     * @param baseMessage
     * @param isZip 是否压缩
     * @return
     */
    public byte[] serialize(Object baseMessage,boolean isZip) {


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
            if(isZip){
                return this.getSerializer().zipSerialize(eve);
            }else{
                return this.getSerializer().noZipSerialize(eve);
            }
        } catch (CodecException e) {
            e.printStackTrace();
            return null;
        }
    }
    public byte[] serialize(Object baseMessage){
        return this.serialize(baseMessage,false);
    }
    /**
     * 通信信息反序列化
     *
     * @param bt
     * @param aclass
     * @param unZip 是否解压缩
     * @param <T>
     * @return
     */
    public <T> T deserialize(byte[] bt, Class<T> aclass,boolean unZip) {

        try {
            if(unZip){
                return this.getSerializer().zipDeserialize(bt, aclass.getName());
            }else{
                return this.getSerializer().noZipDeserialize(bt, aclass.getName());
            }
        } catch (CodecException e) {
            e.printStackTrace();
            return null;
        }
    }
    public <T> T deserialize(byte[] bt, Class<T> aclass){
        return this.deserialize(bt,aclass,false);
    }


    /**
     * 获取序列化器
     * @return
     */
    private JsonSerializer getSerializer(){
        if(this.serializer==null){
            this.serializer = (JsonSerializer)SerializerManager.getSerializer(JsonSerializer.JsonSerializerCode);
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
