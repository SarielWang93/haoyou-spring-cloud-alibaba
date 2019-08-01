package com.haoyou.spring.cloud.alibaba.sofabolt.scheduled;

import com.haoyou.spring.cloud.alibaba.service.cultivate.CultivateService;
import com.haoyou.spring.cloud.alibaba.service.fighting.FightingService;
import com.haoyou.spring.cloud.alibaba.service.login.LoginService;
import com.haoyou.spring.cloud.alibaba.service.match.MatchService;
import com.haoyou.spring.cloud.alibaba.sofabolt.connection.Connections;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/31 10:48
 * 所有定时操作在此类统一配置
 */
@Service
public class ScheduledService {
    private final static Logger logger = LoggerFactory.getLogger(ScheduledService.class);
    /**
     * 各系统服务
     */
    @Reference(version = "${match.service.version}")
    private MatchService matchService;
    @Reference(version = "${login.service.version}")
    private LoginService loginService;
    @Reference(version = "${fighting.service.version}")
    private FightingService fightingService;
    @Reference(version = "${cultivate.service.version}")
    private CultivateService cultivateService;

    @Autowired
    private Connections connections;

    /**
     * 每隔5分钟清除已断开的链接
     */
    @Scheduled(cron = "${sofabolt.connections.cleardelay: 0 */5 * * * ?}")
    public void inspectConnections() {
        logger.info("清理链接！");
        connections.inspect();
    }

    /**
     * 每隔一小时，检查结算
     */
    @Scheduled(cron = "0 0 */1 * * ?")
    public void inspectSettlement() {
        logger.info("结算系统！");
        cultivateService.doSettlement();
    }

    /**
     * 每隔30分钟,将缓存同步到数据库
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void loginSynchronization() {
        logger.info("向数据库同步用户信息！");
        loginService.synchronization();
    }

    /**
     * 每隔两秒匹配
     */
    @Scheduled(cron = "${matchpool.delay: 0/2 * * * * ?}")
    public void doMatch(){
        matchService.doMatch();
    }
}
