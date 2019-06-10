package com.haoyou.spring.cloud.alibaba.manager.handle;

import com.alibaba.dubbo.config.annotation.Reference;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
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
     * 系统管理
     */
    public final static int LOGIN = 0;//登录
    public final static int LOGINOUT = 1;//登出
    public final static int BEAT = 2;//心跳
    public final static int REGISTER = 3;//注册
    public final static int VERSION_CONTROLLER = 6;//版本控制


    /**
     * 匹配系统
     */
    public final static int MATCH_IN = 31;//开始匹配
    public final static int MATCH_OUT = 32;//取消匹配
    public final static int MATCH_ACCEPT = 33;//接受匹配
    public final static int MATCH_REFUSE = 34;//拒绝匹配

    /**
     * 排名系统
     */
    public final static int RANK_LIST = 41;//获取排行榜
    public final static int RANK_NUM = 42;//获取我的排名
    /**
     * 战斗系统
     */
    public final static int FIGHTING_MSG = 20;//战斗信息
    public final static int FIGHTING_AI = 25;//战斗信息
    public final static int FIGHTING_AI2 = 26;//战斗信息
    /**
     * 养成
     */
    public final static int SKILL_CONFIG = 41;//技能配置
    /**
     * 获取信息
     */
    public final static int GET_PROPS = 51;//获取仓库信息
    public final static int GET_HALL = 52;//获取仓库信息

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
    protected ManagerServiceImpl managerService;
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
        managerService.putManagerHanderMap(this);
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
