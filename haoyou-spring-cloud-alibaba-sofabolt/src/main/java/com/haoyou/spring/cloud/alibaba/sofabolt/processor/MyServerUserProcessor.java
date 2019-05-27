package com.haoyou.spring.cloud.alibaba.sofabolt.processor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.Connection;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.sofabolt.connection.Connections;
import com.haoyou.spring.cloud.alibaba.service.manager.ManagerService;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


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
    public Object handleRequest(BizContext bizCtx, MyRequest req) throws Exception {

        if (req != null) {
            //临时操作
            req.setMsgJson(new String(req.getMsg(), "UTF-8"));
            //loge记录接收到的信息
            logger.info(String.format("接受信息：%s", req));

            //判断uid
            String useruid = req.getUseruid();
            if (StrUtil.isNotEmpty(useruid)) {
                //刷新链接
                Connection connectionthis = bizCtx.getConnection();
                Connection connectionuid = connections.get(useruid);
                if (connectionuid == null || !connectionuid.getChannel().isActive()) {
                    connections.put(useruid, connectionthis);
                }
                //不同地点登录处理
                else if (!connectionuid.getRemoteAddress().equals(connectionthis.getRemoteAddress())) {
                    BaseMessage close = new BaseMessage();
                    close.setState(ResponseMsg.MSG_ERR);
                    sendMsgUtil.sendMsgOneNoReturn(useruid, SendType.MANDATORY_OFFLINE, close);
                    connections.put(useruid, connectionthis);
                }
            }

            //调用处理器
            BaseMessage baseMessage = managerService.handle(req);
            //回复信息
            req.setMsg(sendMsgUtil.serialize(baseMessage));

            if (req.getId() == 1 && ResponseMsg.MSG_SUCCESS == (baseMessage.getState())) {
                //如果登出成功，删除链接
                connections.remove(useruid);
            }

            //临时操作
            req.setMsgJson(new String(req.getMsg(), "UTF-8"));
            logger.info(String.format("返回信息：%s", req));
        }
        return req;
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
