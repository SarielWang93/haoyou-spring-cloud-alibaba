package com.haoyou.spring.cloud.alibaba.manager.handle;

import org.apache.dubbo.config.annotation.Reference;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.manager.service.impl.ManagerServiceImpl;
import com.haoyou.spring.cloud.alibaba.service.cultivate.CultivateService;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.service.fighting.FightingService;
import com.haoyou.spring.cloud.alibaba.service.login.LoginService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.service.match.MatchService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;

/**
 * 消息处理器基类
 */
@Service
@Data
public abstract class ManagerHandle implements Serializable {
    private static final long serialVersionUID = 4685902869191400040L;


    /**
     * 各系统服务
     */
    @Reference(version = "${match.service.version}")
    protected MatchService matchService;
    @Reference(version = "${login.service.version}")
    protected LoginService loginService;
    @Reference(version = "${fighting.service.version}")
    protected FightingService fightingService;
    @Reference(version = "${cultivate.service.version}")
    protected CultivateService cultivateService;


    /**
     * 工具
     */
    @Autowired
    protected RedisObjectUtil redisObjectUtil;
    @Autowired
    protected SendMsgUtil sendMsgUtil;

    /**
     * 处理标识
     */
    protected Integer handleType;

    /**
     * 设置类型并注册到处理中心
     */
    @PostConstruct
    protected void init(){
        setHandleType();
        ManagerServiceImpl.putManagerHanderMap(this);
    }

    /**
     * 重写方法，配置标识
     */
    protected abstract void setHandleType();

    /**
     * 处理方法
     * @param req
     * @return
     */
    public abstract BaseMessage handle(MyRequest req);

}
