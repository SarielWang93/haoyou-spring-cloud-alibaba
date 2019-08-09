package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import cn.hutool.core.date.DateTime;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.ServerMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserNumericalMapper;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.cultivate.service.*;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.ScoreRankUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
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
    @Autowired
    protected EmailService emailService;




    protected DateTime date;
    protected List<User> users ;
    protected long runingDays;

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

    public void doWithUsers(){
        this.users = userUtil.allUser();
        this.handle();
        String settleHandle = this.getClass().getSimpleName().replaceAll("SettleHandle", "");

        //向在线玩家发送结算信息
        HashMap<String, User> userLogin = userUtil.getUserLogin();
        for(User user:userLogin.values()){
            MapBody<String,Object> mapBody = new MapBody<>();
            mapBody.put("settleHandle",settleHandle);
            sendMsgUtil.sendMsgOneNoReturn(user.getUid(), SendType.SETTLE,mapBody);
        }
    }

    public void doHandle(){
        //定时结算
        if (this.chackDate()) {
            try {
                this.doWithUsers();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 是否到刷新的天数
     * @param refresh
     * @return
     */
    protected boolean isRefresh(Integer refresh){
        int dayOfWeek = this.date.dayOfWeek();
        int dayOfMonth = this.date.dayOfMonth();
        //执行日清零，周清零，月清零
        if(refresh<100){
            if (refresh.equals(1) || (dayOfWeek == 2 && refresh.equals(7)) || (dayOfMonth == 1 && refresh.equals(30))) {
                return true;
            }
        }else{
            refresh-=100;
            if(this.runingDays%refresh==0){
                return true;
            }
        }
        return false;
    }



}
