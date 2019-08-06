package com.haoyou.spring.cloud.alibaba.register;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.mapper.CurrencyMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.ServerMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserDataMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.login.UserCatch.UserDateSynchronization;
import com.haoyou.spring.cloud.alibaba.pojo.bean.DailyCheckIn;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.ScoreRankUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/15 10:58
 */
@Service
public class Register {
    private final static Logger logger = LoggerFactory.getLogger(Register.class);

    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CurrencyMapper currencyMapper;
    @Autowired
    private UserDataMapper userDataMapper;

    @Autowired
    private ServerMapper serverMapper;

    @Autowired
    private ScoreRankUtil scoreRankUtil;

    @Autowired
    private UserDateSynchronization userDateSynchronization;
    @Autowired
    private UserUtil userUtil;

    /**
     * 初始化编号
     */
    @PostConstruct
    protected void init() {
        List<User> users = userMapper.selectAll();
        redisObjectUtil.save(RedisKey.USER_COUNT, Integer.valueOf(users.size()), -1);
    }

    /**
     * 注册
     *
     * @param user
     * @return
     */
    public User register(User user) {

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


        user.setCreatDate(new Date());
        user.setLastUpdateDate(new Date());


        this.idNumAndServer(user);


        Currency currency = new Currency();
        currency.setUserUid(user.getUid());
        currency.setCoin(0);
        currency.setVitality(100);
        currency.setDiamond(0);
        currency.setPropMax(20);
        currency.setPetMax(5);
        currency.setRank(1);
        currency.setPetExp(0L);
        user.setCurrency(currency);

        UserData userData = new UserData();
        userData.setUserUid(user.getUid());
        userData.setExp(0);
        userData.setAvatar("defult");
        userData.setLevel(999);
        userData.setName(user.getUsername());
        userData.setUpLevExp(260l);
        user.setUserData(userData);
        userUtil.setDailyCheckIn(user);

        userMapper.insertSelective(user);
        userDataMapper.insertSelective(userData);
        currencyMapper.insertSelective(currency);
        logger.info(String.format("registerUser: %s", user.getUsername()));
        user.setState(ResponseMsg.MSG_SUCCESS);


        //当前服排名
        scoreRankUtil.add(RedisKeyUtil.getKey(RedisKey.RANKING, user.getServerId().toString()), user);


        return user;
    }


    /**
     * 玩家编号 以及 分服
     *
     * @param user
     */
    public void idNumAndServer(User user) {
        //编号
        Integer num = redisObjectUtil.get(RedisKey.USER_COUNT, Integer.class) + 1;
        redisObjectUtil.save(RedisKey.USER_COUNT, Integer.valueOf(num), -1);


        String n = num.toString();
        String IdNum = "HY00000000";

        String substring = IdNum.substring(0, 10 - n.length());

        user.setIdNum(substring + n);


        int serverNum = num / 100 + 1;

        Server server = new Server();
        server.setServerNum(serverNum);
        server = serverMapper.selectOne(server);

        if (server == null) {
            server = new Server();
            server.setServerNum(serverNum);
            server.setCreatDate(new Date());
            server.setServerName("server" + serverNum);
            int insert = serverMapper.insert(server);
        }

        user.setServerId(server.getId());

    }




}
