package com.haoyou.spring.cloud.alibaba.login.service.impl;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.login.UserCatch.UserDateSynchronization;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.service.redis.ScoreRankService;

import com.haoyou.spring.cloud.alibaba.service.login.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 登录有关服务实现类
 */
@Service(version = "${login.service.version}")
public class LoginServiceImpl implements LoginService {
    private final static Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserDateSynchronization serializerRotation;
    @Reference(version = "${score-rank.service.version}")
    private ScoreRankService scoreRankService;

    /**
     * 登录
     * @param req
     * @return
     */
    @Override
    public User login(MyRequest req){

        User user = req.getUser();

        logger.info(String.format("login: %s",user.getUid()));

        //获取用户信息
        User user1=select(user);

        if(user1==null)
        //如果没有用户则注册
        {
            //user.setPassword(Md5Utils.getMD5("123456","UTF-8"));
            //注册并获取用户信息（初始化用户信息）
            user=this.register(req);
            if(user==null){
                user.setState(ResponseMsg.MSG_LOGIN_WRONG);
                return user;
            }
            //加入排行榜
            scoreRankService.add(RedisKey.SCORE_RANK,user.getUid(),user.getRank().longValue());
        }else{
            user=user1;
        }

        //缓存登录用户的信息
        if(!serializerRotation.cache(user)){
            user.setState(ResponseMsg.MSG_LOGIN_WRONG);
        }
        user.setState(ResponseMsg.MSG_SUCCESS);
        return user;
    }

    /**
     * 登出
     * @param req
     * @return
     */
    @Override
    public User logout(MyRequest req) {
        User user = req.getUser();

        if(user==null){
            user.setState(ResponseMsg.MSG_LOGINOUT_WRONG);
            return user;
        }


        //清除缓存
        if(serializerRotation.removeCache(user)){
            logger.info(String.format("%s 登出成功！！",user.getName()));
            user.setState(ResponseMsg.MSG_SUCCESS);
            return user;
        }


        user.setState(ResponseMsg.MSG_LOGINOUT_WRONG);
        return user;
    }

    /**
     * 注册并查询
     * @param req
     * @return
     */
    @Override
    public User register(MyRequest req) {
        User user = req.getUser();
        //TODO 注册的时候可以存储平台信息
        user.setState(1);
        user.setCoin(0);
        user.setRank(1);
        user.setPropMax(20);
        userMapper.insert(user);
        user = userMapper.selectOne(user);
        logger.info(String.format("registerUser: %s",user.getName()));

        return user;
    }

    /**
     * 用户登录名数据库查询
     * @param user
     * @return
     */
    private User select(User user){
        return userMapper.selectOne(user);
    }



}
