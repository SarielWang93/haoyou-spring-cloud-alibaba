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
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
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

    }

    /**
     * 注册
     *
     * @param req
     * @return
     */
    public User register(MyRequest req) {

        User user = req.getUser();
        user.setState(null);
        //设备编号
        String deviceUid = req.getDeviceuid();
        String[] deviceUidSplit = deviceUid.split("-");
        //判断用户名密码以及屏蔽词
        if (StrUtil.isNotEmpty(user.getUsername()) && StrUtil.isNotEmpty(user.getPassword())) {
            if (userUtil.hasShieldVocas(user.getUsername())) {
                user.setState(ResponseMsg.MSG_ERR);
                return user.notTooLong();
            }
            //根据用户名查询是否已存在
            User userx = new User();
            userx.setUsername(user.getUsername());
            userx = userMapper.selectOne(userx);
            if (userx != null && StrUtil.isNotEmpty(userx.getUid())) {
                user.setState(ResponseMsg.MSG_REGISTER_USERNAME_EXIST);
                return user;
            }
            //游客转正操作
            if(StrUtil.isNotEmpty(user.getUid())){

                User userByDeviceUid = userUtil.getUserByDeviceUid(deviceUid);

                userByDeviceUid.setUsername(user.getUsername());
                userByDeviceUid.setPassword(user.getPassword());

                userUtil.saveUser(userByDeviceUid);
                userUtil.saveSqlUser(userByDeviceUid);
                userByDeviceUid.setState(ResponseMsg.ALREADY_USER);
                return userByDeviceUid;

            }
        } else if (user.getUid() == null) {
            user.setState(ResponseMsg.MSG_ERR);
            return user.notTooLong();
        } else if (deviceUidSplit.length > 1) {
            //游客注册
            if (user.getUid().equals(deviceUidSplit[1])) {
                User userByDeviceUid = userUtil.getUserByDeviceUid(deviceUid);
                if (userByDeviceUid != null) {
                    userByDeviceUid.setState(ResponseMsg.ALREADY);
                    return userByDeviceUid;
                }
                user.setState(ResponseMsg.ALREADY_REGISTERED);
                user.setUid(null);
                user.setLastLoginDevice(deviceUid);

            }
        }


        //TODO 注册的时候可以存储平台信息
        if (StrUtil.isEmpty(user.getUid())) {
            user.setUid(IdUtil.simpleUUID());
        }

//        user.setState(1);


        user.setCreatDate(new Date());
        user.setLastUpdateDate(new Date());


        this.idNumAndServer(user);


        Currency currency = new Currency();
        currency.setUserUid(user.getUid());
        currency.setCoin(0L);
        currency.setVitality(100);
        currency.setDiamond(0L);
        currency.setPropMax(20);
        currency.setPetMax(5);
        currency.setRank(1);
        currency.setPetExp(0L);
        user.setCurrency(currency);

        UserData userData = new UserData();
        userData.setUserUid(user.getUid());
        userData.setExp(0L);
        userData.setAvatar("defult");
        userData.setLevel(999);
        userData.setName(StrUtil.isNotEmpty(user.getUsername()) ? user.getUsername() : String.format("游客-%s", user.getIdNum()));
        userData.setUpLevExp(260l);
        user.setUserData(userData);
        userUtil.setDailyCheckIn(user);

        userMapper.insertSelective(user);
        userDataMapper.insertSelective(userData);
        currencyMapper.insertSelective(currency);
        logger.info(String.format("registerUser: %s", userData.getName()));
        if (user.getState() == null) {
            user.setState(ResponseMsg.MSG_SUCCESS);
        }


        //当前服排名
        scoreRankUtil.add(RedisKeyUtil.getKey(RedisKey.RANKING, user.getServerId().toString()), user);
        scoreRankUtil.add(RedisKey.RANKING, user);

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

            String levelUpExpKey = RedisKeyUtil.getKey(RedisKey.SERVER, Integer.toString(insert));
            redisObjectUtil.save(levelUpExpKey, server, -1);
        }

        user.setServerId(server.getId());

    }


}
