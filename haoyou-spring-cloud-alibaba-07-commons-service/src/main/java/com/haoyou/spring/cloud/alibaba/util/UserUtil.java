package com.haoyou.spring.cloud.alibaba.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.USER),User.class);
    }
    public HashMap<String, User> getUserOutLine() {
        return redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.OUTLINE_USER),User.class);
    }
    public HashMap<String, User> getUserAllHive() {
        HashMap<String, User> stringUserHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.USER), User.class);
        stringUserHashMap.putAll(redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.OUTLINE_USER),User.class));
        return stringUserHashMap;
    }



    /**
     * 从数据库获取全部user
     * @return
     */
    public List<User> allUser() {

        List<User> users = userMapper.selectAll();

        for(User user:users){
            User user1 = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, user.getUid()), User.class);

            if(user1 == null){
                user1 = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, user.getUid()), User.class);
            }
            if(user1 == null){
                this.cacheUser(user);
            }else{
                user = user1;
            }
        }
        return users;
    }

    /**
     * 加载用户
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
        Map<String,UserNumerical> userNumericalMap = new HashMap<>();
        for(UserNumerical userNumerical: userNumericals){
            userNumericalMap.put(userNumerical.getNumericalName(),userNumerical);
        }
        if (stringNumericalHashMap.size() != userNumericals.size()) {
            for (Numerical numerical : stringNumericalHashMap.values()){
                if(!userNumericalMap.containsKey(numerical.getName())){
                    UserNumerical userNumerical = new UserNumerical();
                    userNumerical.setUserUid(user.getUid());
                    userNumerical.setNumericalName(numerical.getName());
                    userNumerical.setValue(0l);
                    userNumericalMapper.insertSelective(userNumerical);
                    userNumericalMap.put(numerical.getName(),userNumerical);
                }
            }
        }

        //每日签到
        if(user.getUserData().getDailyCheckIn()== null){
            this.setDailyCheckIn(user);
        }
        user.setUserNumericalMap(userNumericalMap);
    }





    /**
     * 添加道具
     * @param user
     * @param prop
     */
    public void addProp(User user,Prop prop) {
        List<Prop> list = new ArrayList<>();
        list.add(prop);
        addProps(user,list);
    }

    public void addProps(User user, List<Prop> propList){
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

            if(!propsOver.isEmpty()){
                propsEmail(user,propsOver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 超出的道具发送邮箱
     * @param user
     * @param propsOver
     */
    public void propsEmail(User user, List<Prop> propsOver){
            //TODO 超出的道具发送邮箱
    }


    /**
     * 获取用户奖励
     * @param user
     * @return
     */
    public DailyCheckIn getDailyCheckIn(User user) {
        return redisObjectUtil.deserialize(user.getUserData().getDailyCheckIn(),DailyCheckIn.class);
    }
    /**
     * 获取一套签到奖励
     * @param user
     * @return
     */
    public void setDailyCheckIn(User user,DailyCheckIn dailyCheckIn) {
        user.getUserData().setDailyCheckIn(redisObjectUtil.serialize(dailyCheckIn));
    }
    public void setDailyCheckIn(User user) {
        //随机一个版本
        int t = RandomUtil.randomInt(2) + 1;

        List<Award> awards = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            Award award = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, String.format("%s_%s_%s",RedisKey.DAILY_CHECK_IN,t,i+1)), Award.class);
            awards.add(award);
        }

        DailyCheckIn dailyCheckIn = new DailyCheckIn();

        dailyCheckIn.setAwards(awards);

        this.setDailyCheckIn(user,dailyCheckIn);
    }









}
