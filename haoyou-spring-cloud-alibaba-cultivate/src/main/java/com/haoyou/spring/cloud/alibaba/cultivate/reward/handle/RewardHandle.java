package com.haoyou.spring.cloud.alibaba.cultivate.reward.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.cultivate.service.RewardService;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Data
public abstract class RewardHandle {

    @Autowired
    protected RedisObjectUtil redisObjectUtil;
    @Autowired
    protected SendMsgUtil sendMsgUtil;


    @Autowired
    protected UserMapper userMapper;


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
        RewardService.register(this);
    }

    /**
     * 重写方法，配置标识
     */
    protected abstract void setHandleType();


    /**
     * 处理方法
     * @param user
     * @return
     */
    public abstract boolean handle(User user);
}
