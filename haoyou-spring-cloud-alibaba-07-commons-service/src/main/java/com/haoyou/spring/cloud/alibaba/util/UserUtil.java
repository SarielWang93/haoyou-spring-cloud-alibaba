package com.haoyou.spring.cloud.alibaba.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.mapper.CurrencyMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserDataMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserNumericalMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.commons.util.ZIP;
import com.haoyou.spring.cloud.alibaba.pojo.bean.DailyCheckIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/29 11:50
 * user封装工具类
 */
@Service
public class UserUtil {


    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserDataMapper userDataMapper;

    @Autowired
    private UserNumericalMapper userNumericalMapper;
    @Autowired
    private CurrencyMapper currencyMapper;


    public HashMap<String, User> getUserLogin() {
        return redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.USER), User.class);
    }

    public HashMap<String, User> getUserOutLine() {
        return redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.OUTLINE_USER), User.class);
    }

    public HashMap<String, User> getUserAllCatch() {
        HashMap<String, User> stringUserHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.USER), User.class);
        stringUserHashMap.putAll(redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.OUTLINE_USER), User.class));
        return stringUserHashMap;
    }
    /**
     * 从数据库获取全部user
     *
     * @return
     */
    public List<User> allUser() {

        List<User> users = userMapper.selectAll();

        List<User> remove = new ArrayList<>();
        List<User> add = new ArrayList<>();

        for (User user : users) {
            User user1 = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, user.getUid()), User.class);

            if (user1 == null) {
                user1 = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, user.getUid()), User.class);
            }
            if (user1 == null) {
                this.cacheUser(user);
            } else {
                remove.add(user);
                add.add(user1);
            }
        }
        users.removeAll(remove);
        users.addAll(add);

        return users;
    }

    /**
     * 根据uid获取用户信息
     * @param userUid
     * @return
     */
    public User getUserByUid(String userUid) {
        User user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, userUid), User.class);
        if(user == null){
            user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, userUid), User.class);
        }
        if(user == null){
            User s = new User();
            s.setUid(userUid);
            user = userMapper.selectOne(s);
            this.cacheUser(user);
        }
        return user;
    }

    public void saveUser(User user){

        user.setLastUpdateDate(new Date());
        String key = this.isInCatch(user);
        if(key != null){
            redisObjectUtil.save(key,user);
        }else{
            this.saveSqlUser(user);
        }
    }
    /**
     * 是否缓存
     * @param user
     * @return
     */
    public String isInCatch(User user){

        String key = RedisKeyUtil.getKey(RedisKey.USER, user.getUid());

        User userx = redisObjectUtil.get(key,User.class);
        if(userx == null){
            key = RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, user.getUid());
            userx = redisObjectUtil.get(key, User.class);
            if(userx == null){
                key = null;
            }
        }
        return key;
    }





    /**
     * 加载用户
     *
     * @param user
     */
    public void cacheUser(User user) {
        //加载货币信息
        com.haoyou.spring.cloud.alibaba.commons.entity.Currency currency = new Currency();
        currency.setUserUid(user.getUid());
        user.setCurrency(currencyMapper.selectOne(currency));
        //加载玩家信息
        UserData userData = new UserData();
        userData.setUserUid(user.getUid());
        user.setUserData(userDataMapper.selectOne(userData));
        //加载数值系统信息
        UserNumerical userNumericalselect = new UserNumerical();
        userNumericalselect.setUserUid(user.getUid());
        List<UserNumerical> userNumericals = userNumericalMapper.select(userNumericalselect);

        HashMap<String, Numerical> stringNumericalHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.NUMERICAL), Numerical.class);
        Map<String, UserNumerical> userNumericalMap = new HashMap<>();
        for (UserNumerical userNumerical : userNumericals) {
            userNumericalMap.put(userNumerical.getNumericalName(), userNumerical);
        }
        if (stringNumericalHashMap.size() != userNumericals.size()) {
            for (Numerical numerical : stringNumericalHashMap.values()) {
                if (!userNumericalMap.containsKey(numerical.getName())) {
                    UserNumerical userNumerical = new UserNumerical();
                    userNumerical.setUserUid(user.getUid());
                    userNumerical.setNumericalName(numerical.getName());
                    userNumerical.setValue(0l);
                    userNumericalMapper.insertSelective(userNumerical);
                    userNumericalMap.put(numerical.getName(), userNumerical);
                }
            }
        }

        //每日签到
        if (user.getUserData().getDailyCheckIn() == null) {
            this.setDailyCheckIn(user);
            userDataMapper.updateByPrimaryKeySelective(user.getUserData());
        }
        user.setUserNumericalMap(userNumericalMap);
    }

    /**
     * 向数据库同步玩家信息
     *
     * @param user
     */
    public void saveSqlUser(User user) {
        userMapper.updateByPrimaryKeySelective(user);

        currencyMapper.updateByPrimaryKeySelective(user.getCurrency());

        userDataMapper.updateByPrimaryKeySelective(user.getUserData());

        for(UserNumerical userNumerical : user.getUserNumericalMap().values()){
            userNumericalMapper.updateByPrimaryKeySelective(userNumerical);
        }
    }


    /**
     * 添加道具
     *
     * @param user
     * @param prop
     */
    public void addProp(User user, Prop prop) {
        List<Prop> list = new ArrayList<>();
        list.add(prop);
        addProps(user, list);
    }

    public void addProps(User user, List<Prop> propList) {
        try {
            List<Prop> propsThis = user.propList();
            List<Prop> propsOver = new ArrayList<>();

            for (Prop prop : propList) {

                int count = 1;
                if (prop.getCount() != 0) {
                    count = prop.getCount();
                }
                int i = 0;
                if ((i = propsThis.indexOf(prop)) != -1 && !"PetSkill".equals(prop.getName())) {
                    propsThis.get(i).setCount(propsThis.get(i).getCount() + count);
                } else {
                    prop.setPropInstenceUid(IdUtil.simpleUUID());
                    prop.setCount(count);
                    propsThis.add(prop);
                }
                user.getCurrency().setProps(ZIP.gZip(MapperUtils.obj2jsonIgnoreNull(propsThis).getBytes("UTF-8")));
            }

            if (!propsOver.isEmpty()) {
                propsEmail(user, propsOver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 超出的道具发送邮箱
     *
     * @param user
     * @param propsOver
     */
    public void propsEmail(User user, List<Prop> propsOver) {
        //TODO 超出的道具发送邮箱
    }


    /**
     * 获取用户签到列表
     *
     * @param user
     * @return
     */
    public DailyCheckIn getDailyCheckIn(User user) {
        return redisObjectUtil.deserialize(user.getUserData().getDailyCheckIn(), DailyCheckIn.class);
    }

    /**
     * 获取一套签到奖励
     *
     * @param user
     * @return
     */
    public void setDailyCheckIn(User user, DailyCheckIn dailyCheckIn) {
        user.getUserData().setDailyCheckIn(redisObjectUtil.serialize(dailyCheckIn));
    }

    public void setDailyCheckIn(User user) {
        //随机一个版本
        int t = RandomUtil.randomInt(2) + 1;

        List<Award> awards = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            Award award = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, String.format("%s_%s_%s", RedisKey.DAILY_CHECK_IN, t, i + 1)), Award.class);
            awards.add(award);
        }

        DailyCheckIn dailyCheckIn = new DailyCheckIn();

        dailyCheckIn.setAwards(awards);

        this.setDailyCheckIn(user, dailyCheckIn);
    }


    /**
     * 获取基金信息
     *
     * @param user
     * @return
     */
    public TreeMap<Date, Fund> getFunds(User user) {
        byte[] fundsBytes = user.getUserData().getFunds();

        TreeMap<Date, Fund> fundsTreeMap = null;

        if (fundsBytes != null) {
            fundsTreeMap = redisObjectUtil.deserialize(fundsBytes, TreeMap.class);
        } else {
            fundsTreeMap = new TreeMap();
        }
        return fundsTreeMap;

    }

    /**
     * 添加基金
     *
     * @param user
     * @param fund
     */
    public void addFund(User user, Fund fund) {
        TreeMap<Date, Fund> fundsTreeMap = this.getFunds(user);
        fundsTreeMap.put(new Date(), fund);
        user.getUserData().setFunds(redisObjectUtil.serialize(fundsTreeMap));
    }

    /**
     * 删除基金
     *
     * @param user
     */
    public void deleteFunds(User user) {
        TreeMap<Date, Fund> fundsTreeMap = this.getFunds(user);

        TreeMap<Date, Fund> newfundsTreeMap = new TreeMap<>();

        for (Map.Entry<Date, Fund> entry : fundsTreeMap.entrySet()) {

            Date key = entry.getKey();
            Fund fund = entry.getValue();

            Date overTime = fund.getOverTime();

            DateTime dateTime = DateUtil.offsetDay(key, fund.getDays()-1);

            Date date = dateTime.toJdkDate();

            if(overTime.getTime() > date.getTime()){
                date = overTime;
            }

            if(date.getTime() > new Date().getTime()){
                newfundsTreeMap.put(key,fund);
            }

        }

        user.getUserData().setFunds(redisObjectUtil.serialize(newfundsTreeMap));
    }


    /**
     * 获取邮件信息
     * @param user
     * @return
     */
    public TreeMap<Date,Email> getEmails(User user){
        byte[] emailsBytes = user.getUserData().getEmails();
        TreeMap<Date,Email> emailsTreeMap = null;

        if(emailsBytes == null){
            emailsTreeMap = new TreeMap<>();
        }else{
            emailsTreeMap = redisObjectUtil.deserialize(emailsBytes,TreeMap.class);
        }

        return emailsTreeMap;
    }

    /**
     * 添加邮件
     * @param user
     * @param email
     */
    public void addEmail(User user, Email email) {
        TreeMap<Date,Email> emailsTreeMap = this.getEmails(user);
        emailsTreeMap.put(email.getCreatDate(),email);
        user.getUserData().setEmails(redisObjectUtil.serialize(emailsTreeMap));
    }

    /**
     * 删除邮件
     * @param user
     * @param email
     */
    public void deleteEmail(User user, Email email) {
        TreeMap<Date,Email> emailsTreeMap = this.getEmails(user);
        emailsTreeMap.remove(email.getCreatDate());
        user.getUserData().setEmails(redisObjectUtil.serialize(emailsTreeMap));
    }








}
