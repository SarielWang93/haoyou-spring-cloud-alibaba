package com.haoyou.spring.cloud.alibaba.login.UserCatch;

import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.mapper.*;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 登录，登出时，用户信息缓存，以及删除
 */
@Component
public class UserDateSynchronization {
    private final static Logger logger = LoggerFactory.getLogger(UserDateSynchronization.class);

    @Autowired
    private PetMapper petMapper;
    @Autowired
    private PetSkillMapper petSkillMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserDataMapper userDataMapper;

    @Autowired
    private UserNumericalMapper userNumericalMapper;
    @Autowired
    private CurrencyMapper currencyMapper;
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private UserUtil userUtil;


    /**
     * 每隔30分钟,将缓存同步到数据库
     */
//    @scheduled(cron = "0 */30 * * * ?")
    public void synchronization() {
        logger.info(String.format("synchronization begin ......"));
        HashMap<String, User> users = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.USER), User.class);
        users.putAll(redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.OUTLINE_USER), User.class));
        for (Map.Entry<String, User> entry : users.entrySet()) {
            User user = entry.getValue();
            User user1 = userMapper.selectByPrimaryKey(user.getId());
            //只同步修改过的user
            if (user1.getLastUpdateDate().getTime() < user.getLastUpdateDate().getTime()) {
                redisObjectUtil.refreshTime(entry.getKey());
                userUtil.saveSqlUserAndPets(user);
            }
        }
    }

    /**
     * 缓存用户信息到redis
     *
     * @param user
     * @return
     */
    public boolean cache(User user) {
        //TODO 读取缓存用户所有信息
        logger.info(String.format("cacheUser: %s", user.getUsername()));

        String key = RedisKeyUtil.getKey(RedisKey.USER, user.getUid());

        if (!user.isOnLine()) {

            userUtil.cacheUser(user);

            //从数据库获取的pets
            userUtil.cachePet(user);
            user.setOnLine(true);
        }
        if (redisObjectUtil.save(key, user)) {
            //缓存宠物信息
            logger.info(String.format("%s 登录成功！！", user.getUsername()));
            return true;
        }

        return false;
    }



    /**
     * 登出内存操作
     *
     * @param user
     * @return
     */
    public boolean removeCache(User user) {
        userUtil.saveSqlUserAndPets(user);
        //TODO 清除用户所有缓存信息
        String key = RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, user.getUid());
        String key1 = RedisKeyUtil.getKey(RedisKey.USER, user.getUid());
        user.setOnLine(false);
        redisObjectUtil.save(key, user);
        return redisObjectUtil.delete(key1);
    }








}
