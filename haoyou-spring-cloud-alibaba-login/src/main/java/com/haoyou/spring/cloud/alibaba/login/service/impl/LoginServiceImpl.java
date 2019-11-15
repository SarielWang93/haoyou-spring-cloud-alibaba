package com.haoyou.spring.cloud.alibaba.login.service.impl;


import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fescar.spring.annotation.GlobalTransactional;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.mapper.CurrencyMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserDataMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserNumericalMapper;
import com.haoyou.spring.cloud.alibaba.pojo.bean.DailyCheckIn;
import com.haoyou.spring.cloud.alibaba.register.Register;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.apache.dubbo.config.annotation.Service;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.login.UserCatch.UserDateSynchronization;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;


import com.haoyou.spring.cloud.alibaba.service.login.LoginService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * 登录有关服务实现类
 */
@Service(version = "${login.service.version}")
public class LoginServiceImpl implements LoginService {
    private final static Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CurrencyMapper currencyMapper;
    @Autowired
    private UserDateSynchronization userDateSynchronization;
    @Autowired
    private Register register;

    @Autowired
    private UserUtil userUtil;

    /**
     * 登录
     *
     * @param req
     * @return
     */
    @Override
    public User login(MyRequest req) {


        User userIn = req.getUser();

        if(userIn == null){
            userIn .setState(ResponseMsg.MSG_LOGIN_USERNAME_WRONG);
            return userIn.notTooLong();
        }

        logger.info(String.format("login: %s", userIn));


        //根据用户名或者uid获取用户信息
        User user = null;

        String username = userIn.getUsername();
        if(StrUtil.isNotEmpty(username)){
            user = userUtil.getUserByUserName(username);
        }else if(StrUtil.isNotEmpty(userIn.getUid())){

            //设备编号
            String deviceUid = req.getDeviceuid();
            if(StrUtil.isNotEmpty(deviceUid)){
                String[] deviceUidSplit = deviceUid.split("-");
                if(deviceUidSplit.length>1 && userIn.getUid().equals(deviceUidSplit[1])){
                    //游客登录
                    user = userUtil.getUserByDeviceUid(deviceUid);
                }
            }

            if(user == null){
                //缓存登录用户的信息
                user = userUtil.getUserByUid(userIn.getUid());
            }
        }


        if (user == null) {
            userIn.setState(ResponseMsg.MSG_LOGIN_USERNAME_WRONG);
            return userIn.notTooLong();
        }

        if (StrUtil.isEmpty(userIn.getUid()) && !user.getPassword().equals(userIn.getPassword())) {
            userIn.setState(ResponseMsg.MSG_LOGIN_PASSWORD_WRONG);
            return userIn.notTooLong();
        }


        user.setLastLoginDate(new Date());
        user.setLastLoginUrl(req.getUrl());
        user.setLastLoginDevice(req.getDeviceuid());

        String inCatch = userUtil.isInCatch(user.getUid());

        if(inCatch != null && inCatch.contains(RedisKey.OUTLINE_USER)){
            redisObjectUtil.delete(inCatch);
        }
        userUtil.saveUser(user,RedisKey.USER);


        user.setState(ResponseMsg.MSG_SUCCESS);


        this.loginUpAward(user);


        //检查是否在对战中
        if (findFighting(user)) {
            user.setState(ResponseMsg.MSG_LOGINOUT_FIGHTING);
        }


        return user;
    }

    /**
     * 各种奖励
     * @param user
     */
    private void loginUpAward(User user){
        //发放签到奖励
        DailyCheckIn dailyCheckIn = redisObjectUtil.deserialize(user.getUserData().getDailyCheckIn(), DailyCheckIn.class);

        for(Award award:dailyCheckIn.getAwards()){
            if(!award.isUsed()){
                String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid(),RedisKey.DAILY_CHECK_IN,award.getType());
                Award award1 = redisObjectUtil.get(key, Award.class);
                if(award1 == null){
                    redisObjectUtil.save(key,award,-1);
                }
                break;
            }
        }

    }



    /**
     * 获取玩家当前是否处于战斗中
     *
     * @param user
     */
    private boolean findFighting(User user) {

        String fightingRoomUid = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PLAYER_FIGHTING_ROOM, user.getUid()), String.class);
        String aiFightingRoomUid = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PLAYER_FIGHTING_ROOM, "ai-" + user.getUid()), String.class);
        if (StrUtil.isEmpty(aiFightingRoomUid)) {
            if (StrUtil.isNotEmpty(fightingRoomUid)) {
                return true;
            }
        } else {
            redisObjectUtil.delete(RedisKeyUtil.getKey(RedisKey.PLAYER_FIGHTING_ROOM, user.getUid()));
            redisObjectUtil.delete(RedisKeyUtil.getKey(RedisKey.PLAYER_FIGHTING_ROOM, "ai-" + user.getUid()));
            redisObjectUtil.delete(RedisKeyUtil.getKey(RedisKey.FIGHTING_ROOM, aiFightingRoomUid));
        }
        return false;
    }

    /**
     * 登出
     *
     * @param req
     * @return
     */
    @Override
    public User logout(MyRequest req) {
        User user = req.getUser();

        if (user == null) {
            user = new User();
            user.setState(ResponseMsg.MSG_LOGINOUT_WRONG);
            return user;
        }
        user.setLastLoginOutDate(new Date());
        //清除缓存

        userUtil.saveSqlUserAndPets(user);

        userUtil.deleteInCatch(user.getUid());

        userUtil.saveUser(user,RedisKey.OUTLINE_USER);


        logger.info(String.format("%s 登出成功！！", user.getUsername()));
        user.setState(ResponseMsg.MSG_SUCCESS);
        return user;

    }

    /**
     * 注册
     *
     * @param req
     * @return
     */
    @Override
    public User register(MyRequest req) {
        return register.register(req);
    }



    @Override
    public void synchronization() {
        ThreadUtil.excAsync(() -> {
            userDateSynchronization.synchronization();
        },false);
    }


}
