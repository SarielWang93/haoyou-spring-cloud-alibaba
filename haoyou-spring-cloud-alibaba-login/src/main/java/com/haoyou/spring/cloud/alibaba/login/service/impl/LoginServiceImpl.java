package com.haoyou.spring.cloud.alibaba.login.service.impl;


import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fescar.spring.annotation.GlobalTransactional;
import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.entity.UserData;
import com.haoyou.spring.cloud.alibaba.commons.mapper.CurrencyMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserDataMapper;
import org.apache.dubbo.config.annotation.Service;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
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
    private UserDataMapper userDataMapper;

    /**
     * 登录
     *
     * @param req
     * @return
     */
    @Override
    @GlobalTransactional
    public User login(MyRequest req) {


        User userIn = req.getUser();
        User user = new User();
        user.setUid(userIn.getUid());
        user.setUsername(userIn.getUsername());


        logger.info(String.format("login: %s", userIn));


        //根据用户名或者uid获取用户信息
        user = select(user);

        if (user == null) {
            userIn.setState(ResponseMsg.MSG_LOGIN_USERNAME_WRONG);
            return userIn;
        }
        if (userIn != null) {
            if (StrUtil.isEmpty(userIn.getUid()) && !user.getPassword().equals(userIn.getPassword())) {
                userIn.setState(ResponseMsg.MSG_LOGIN_PASSWORD_WRONG);
                return userIn;
            }
        }
        user.setLastLoginDate(new Date());
        user.setLastLoginUrl(req.getUrl());
        //缓存登录用户的信息
        if (!userDateSynchronization.cache(user)) {
            userIn.setState(ResponseMsg.MSG_LOGIN_WRONG);
            return userIn;
        }
        user.setState(ResponseMsg.MSG_SUCCESS);


        if (findFighting(user)) {
            user.setState(ResponseMsg.MSG_LOGINOUT_FIGHTING);
        }


        return user.notTooLong();
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
    @GlobalTransactional
    public User logout(MyRequest req) {
        User user = req.getUser();

        if (user == null) {
            user.setState(ResponseMsg.MSG_LOGINOUT_WRONG);
            return user;
        }
        user.setLastLoginOutDate(new Date());
        //清除缓存
        if (userDateSynchronization.removeCache(user)) {
            logger.info(String.format("%s 登出成功！！", user.getUsername()));
            user.setState(ResponseMsg.MSG_SUCCESS);
            return user.notTooLong();
        }


        user.setState(ResponseMsg.MSG_LOGINOUT_WRONG);
        return user.notTooLong();
    }

    /**
     * 注册并查询
     *
     * @param req
     * @return
     */
    @Override
    @GlobalTransactional
    public User register(MyRequest req) {
        User user = req.getUser();

        if (StrUtil.isEmpty(user.getUsername()) || StrUtil.isEmpty(user.getPassword())) {
            user.setState(ResponseMsg.MSG_ERR);
            return user.notTooLong();
        }
        //根据用户名查询是否已存在
        User userx = new User();
        userx.setUsername(user.getUsername());
        userx = userMapper.selectOne(userx);
        if (userx != null && StrUtil.isNotEmpty(userx.getUid())) {
            user.setState(ResponseMsg.MSG_REGISTER_USERNAME_EXIST);
            return user.notTooLong();
        }


        //TODO 注册的时候可以存储平台信息
        if (StrUtil.isEmpty(user.getUid())) {
            user.setUid(IdUtil.simpleUUID());
        }

        user.setState(1);

        user.setRank(1);

        user.setCreatDate(new Date());
        user.setLastUpdateDate(new Date());
        userMapper.insertSelective(user);

        Currency currency = new Currency();
        currency.setUserUid(user.getUid());
        currency.setCoin(0);
        currency.setVitality(100);
        currency.setDiamond(0);
        currency.setPropMax(20);
        currency.setPetMax(5);

        UserData userData = new UserData();
        userData.setUserUid(user.getUid());
        userData.setExp(0);
        userData.setAvatar("defult");
        userData.setLevel(1);
        userData.setName(user.getUsername());
        userData.setUpLevExp(260l);

        userDataMapper.insertSelective(userData);
        currencyMapper.insertSelective(currency);
        logger.info(String.format("registerUser: %s", user.getUsername()));
        user.setState(ResponseMsg.MSG_SUCCESS);
        return user.notTooLong();
    }

    /**
     * 用户登录名数据库查询
     *
     * @param user
     * @return
     */
    private User select(User user) {
        User user1 = userMapper.selectOne(user);
        if (user1 != null) {
            //outline_user key中加载
            String key1 = RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, user1.getUid());
            User user2 = redisObjectUtil.get(key1, User.class);
            User userrt = null;
            if (user2 != null && user1.getLastUpdateDate().getTime() < user2.getLastUpdateDate().getTime()) {
                user2.setOnLine(true);
                userrt = user2;
            } else {
                //user  key中加载
                String key2 = RedisKeyUtil.getKey(RedisKey.USER, user1.getUid());
                User user3 = redisObjectUtil.get(key2, User.class);
                if (user3 != null && user1.getLastUpdateDate().getTime() < user3.getLastUpdateDate().getTime()){
                    user3.setOnLine(true);
                    userrt = user3;
                }else{
                    //数据库中加载玩家

                    //加载货币信息
                    Currency currency = new Currency();
                    currency.setUserUid(user1.getUid());
                    currency = currencyMapper.selectOne(currency);
                    user1.setCurrency(currency);
                    //加载玩家信息
                    UserData userData = new UserData();
                    userData.setUserUid(user1.getUid());
                    userData = userDataMapper.selectOne(userData);
                    user1.setUserData(userData);
                    //加载方式
                    user1.setOnLine(false);
                    userrt = user1;
                }

            }
            redisObjectUtil.delete(key1);
            return userrt;
        }
        return null;
    }


}
