package com.haoyou.spring.cloud.alibaba.manager.handle;

import com.alibaba.dubbo.config.annotation.Reference;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.manager.service.impl.ManagerServiceImpl;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.service.fighting.FightingService;
import com.haoyou.spring.cloud.alibaba.service.login.LoginService;
import com.haoyou.spring.cloud.alibaba.action.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.action.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.service.match.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;

/**
 * 消息处理器基类
 */
@Service
public abstract class ManagerHandle implements Serializable {


    public final static int LOGIN = 0;//登录
    public final static int LOGINOUT = 1;//登出
    public final static int BEAT = 2;//心跳


    public final static int MATCH_IN = 31;//开始匹配
    public final static int MATCH_OUT = 32;//取消匹配
    public final static int MATCH_ACCEPT = 33;//接受匹配
    public final static int MATCH_REFUSE = 34;//拒绝匹配


    public final static int RANK_LIST = 11;//获取排行榜
    public final static int RANK_NUM = 12;//获取我的排名


    public final static int FIGHTING_MSG = 20;//战斗信息

    @Reference(version = "${match.service.version}")
    protected MatchService matchService;
    @Reference(version = "${login.service.version}")
    protected LoginService loginService;
    @Reference(version = "${fighting.service.version}")
    protected FightingService fightingService;

    @Autowired
    protected ManagerServiceImpl managerService;
    @Autowired
    protected RedisObjectUtil redisObjectUtil;
    @Autowired
    protected SendMsgUtil sendMsgUtil;

    protected Integer handleType;

    @PostConstruct
    protected void init(){
        setHandleType();
        managerService.putManagerHanderMap(this.handleType,this);
    }

    abstract void setHandleType();

    public abstract BaseMessage handle(MyRequest req);

}
