package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import cn.hutool.core.date.DateTime;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.ServerMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserNumericalMapper;
import com.haoyou.spring.cloud.alibaba.cultivate.service.PropUseService;
import com.haoyou.spring.cloud.alibaba.cultivate.service.RewardService;
import com.haoyou.spring.cloud.alibaba.cultivate.service.SkillConfigService;
import com.haoyou.spring.cloud.alibaba.cultivate.service.SettlementService;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.ScoreRankUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 道具使用类
 */
@Service
@Data
public abstract class SettleHandle {

    @Autowired
    protected RedisObjectUtil redisObjectUtil;
    @Autowired
    protected SendMsgUtil sendMsgUtil;
    @Autowired
    protected PetMapper petMapper;
    @Autowired
    protected UserMapper userMapper;
    @Autowired
    protected ServerMapper serverMapper;
    @Autowired
    protected UserNumericalMapper userNumericalMapper;
    @Autowired
    protected ScoreRankUtil scoreRankUtil;
    @Autowired
    protected SkillConfigService skillConfigService;
    @Autowired
    protected RewardService rewardService;
    @Autowired
    protected PropUseService propUseService;
    @Autowired
    protected UserUtil userUtil;

    protected DateTime date;
    protected List<User> users =null;

    /**
     * 设置类型并注册到处理中心
     */
    @PostConstruct
    protected void init(){
        SettlementService.register(this);
    }


    /**
     * 时间校验
     * @return
     */
    public abstract boolean chackDate();

    /**
     *
     * 道具效果
     * @return
     */
    public abstract void handle();

    public void doHandle(DateTime date){
        this.users = userUtil.allUser();
        this.date = date;
        //定时结算
        if (this.chackDate()) {
            this.handle();
        }
    }



}
