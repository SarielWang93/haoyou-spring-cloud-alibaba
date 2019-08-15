package com.haoyou.spring.cloud.alibaba.cultivate.numerical;

import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.cultivate.service.NumericalService;
import com.haoyou.spring.cloud.alibaba.cultivate.service.PropUseService;
import com.haoyou.spring.cloud.alibaba.cultivate.service.RewardService;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/26 10:39
 * 数值系统抽象类
 */
@Service
@Data
public abstract class NumericalCheck {

    @Autowired
    protected RedisObjectUtil redisObjectUtil;
    @Autowired
    protected SendMsgUtil sendMsgUtil;
    @Autowired
    protected UserUtil userUtil;


    @Autowired
    protected RewardService rewardService;
    @Autowired
    protected NumericalService numericalService;

    /**
     * 设置类型并注册到处理中心
     */
    @PostConstruct
    protected void init(){
        NumericalService.register(this);
    }


    /**
     * 数值检测，发放奖励
     * @param user
     * @param numericalName
     * @return
     */
    public abstract void check(User user, String numericalName, long addValue);

}
