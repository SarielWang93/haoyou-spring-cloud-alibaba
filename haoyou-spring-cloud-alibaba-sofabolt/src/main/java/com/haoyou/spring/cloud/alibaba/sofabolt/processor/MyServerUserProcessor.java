package com.haoyou.spring.cloud.alibaba.sofabolt.processor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.Connection;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.sofabolt.connection.Connections;
import com.haoyou.spring.cloud.alibaba.service.manager.ManagerService;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.sofabolt.server.MyServer;
import com.haoyou.spring.cloud.alibaba.sofabolt.service.impl.SendMsgServiceImpl;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;


/**
 * 自定义的业务逻辑用户处理器
 * 注意：
 * 对于所有的请求数据的类型，都必须有 UserProcessor 可以处理（感兴趣），
 * 否则将抛出 RpcServerException 异常，类似于 "RpcServerException：No user processor found for request: java.lang.String"
 */
@Component
public class MyServerUserProcessor extends SyncUserProcessor<MyRequest> {
    private final static Logger logger = LoggerFactory.getLogger(MyServerUserProcessor.class);

    @Reference(version = "${manager.service.version}")
    ManagerService managerService;

    @Autowired
    private Connections connections;
    @Autowired
    private SendMsgUtil sendMsgUtil;


    @Override
    public Object handleRequest(BizContext bizCtx, MyRequest req) {

        if (req != null) {
            //临时操作
            try {
                req.setMsgJson(new String(req.getMsg(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //loge记录接收到的信息
            logger.info(String.format("接受信息：%s", req));

            //判断uid
            String useruid = req.getUseruid();
            if (StrUtil.isNotEmpty(useruid)) {
                //刷新链接
                Connection connectionthis = bizCtx.getConnection();
                Connection connectionuid = connections.get(useruid);
                //前一链接已断开
                if (connectionuid == null || !connectionuid.getChannel().isActive()) {
                    setDeviceuid(connectionthis, req.getDeviceuid());
                    connections.put(useruid, connectionthis);
                }
                //前一链接与新链接不同
                else if (!connectionuid.getRemoteIP().equals(connectionthis.getRemoteIP()) || connectionuid.getRemotePort() != connectionthis.getRemotePort()) {
                    //如果不是同一设备则把前一设备踢下线
                    if (!req.getDeviceuid().equals(connectionuid.getAttribute("deviceuid"))) {
                        sendDown(useruid, connectionuid);
                    }
                    setDeviceuid(connectionthis, req.getDeviceuid());
                    connections.put(useruid, connectionthis);
                }
                //如果没有设备编号则添加设备编号
                else {
                    if (connectionuid.getAttribute("deviceuid") == null) {
                        setDeviceuid(connectionuid, req.getDeviceuid());
                    }
                }
            }

            //调用处理器
            BaseMessage baseMessage = managerService.handle(req);
            //回复信息
            req.setMsg(sendMsgUtil.serialize(baseMessage));

            if (req.getId() == 1 && ResponseMsg.MSG_SUCCESS == (baseMessage.getState())) {
                //如果登出成功，删除链接
                connections.remove(useruid);
            } else if (req.getId() == 0 && ResponseMsg.MSG_SUCCESS == (baseMessage.getState())) {
                useruid = ((User) baseMessage).getUid();
                //不同地点登录处理
                Connection connectionthis = bizCtx.getConnection();
                Connection connectionuid = connections.get(useruid);
                if (connectionuid != null && connectionuid.getChannel().isActive()) {
                    if (!connectionuid.getRemoteAddress().getAddress().getHostAddress().equals(connectionthis.getRemoteAddress().getAddress().getHostAddress())) {
                        sendDown(useruid, connectionuid);
                    }
                }
                setDeviceuid(connectionthis, req.getDeviceuid());
                connections.put(useruid, connectionthis);
            }

            //临时操作
            try {
                req.setMsgJson(new String(req.getMsg(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            logger.info(String.format("返回信息：%s %s", req.getMsg().length, req));
        }
        return req;
    }

    /**
     * 存储设备标识
     *
     * @param connection
     * @param deviceuid
     */
    private void setDeviceuid(Connection connection, String deviceuid) {
        if (deviceuid != null) {
            connection.setAttribute("deviceuid", deviceuid);
        }
    }

    /**
     * 发送强制下线
     *
     * @param useruid
     * @param connectionuid
     */
    private void sendDown(String useruid, Connection connectionuid) {
        BaseMessage close = new BaseMessage();
        close.setState(ResponseMsg.MSG_ERR);
        //发送强制下线
        MyRequest reqx = new MyRequest();
        reqx.setUseruid(useruid);
        reqx.setId(SendType.MANDATORY_OFFLINE);
        reqx.setMsg(sendMsgUtil.serialize(close));
        try {
            MyServer.server.oneway(connectionuid, reqx);
        } catch (RemotingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 指定感兴趣的请求数据类型，该 UserProcessor 只对感兴趣的请求类型的数据进行处理；
     * 假设 除了需要处理 MyRequest 类型的数据，还要处理 java.lang.String 类型，有两种方式：
     * 1、再提供一个 UserProcessor 实现类，其 interest() 返回 java.lang.String.class.getName()
     * 2、使用 MultiInterestUserProcessor 实现类，可以为一个 UserProcessor 指定 List<String> multiInterest()
     */
    @Override
    public String interest() {
        return MyRequest.class.getName();
    }

}
