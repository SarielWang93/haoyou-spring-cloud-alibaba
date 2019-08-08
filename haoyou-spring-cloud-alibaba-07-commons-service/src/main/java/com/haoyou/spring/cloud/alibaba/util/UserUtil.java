package com.haoyou.spring.cloud.alibaba.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.mapper.*;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.commons.util.ZIP;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
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

    @Autowired
    private PetMapper petMapper;
    @Autowired
    private PetSkillMapper petSkillMapper;


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
     *
     * @param userUid
     * @return
     */
    public User getUserByUid(String userUid) {
        String key = this.isInCatch(userUid);
        User user = null;
        if (StrUtil.isEmpty(key)) {
            User s = new User();
            s.setUid(userUid);
            user = userMapper.selectOne(s);
            if (user != null) {
                this.cacheUser(user);
            }
        } else {
            user = redisObjectUtil.get(key, User.class);
        }
        return user;
    }

    public void saveUser(User user) {

        user.setLastUpdateDate(new Date());
        String key = this.isInCatch(user.getUid());
        if (StrUtil.isNotEmpty(key)) {
            redisObjectUtil.save(key, user);
        } else {
            this.saveSqlUser(user);
        }
    }

    /**
     * 是否缓存
     *
     * @param UserUid
     * @return
     */
    public String isInCatch(String UserUid) {

        String key = RedisKeyUtil.getKey(RedisKey.USER, UserUid);

        User userx = redisObjectUtil.get(key, User.class);
        if (userx == null) {
            key = RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, UserUid);
            userx = redisObjectUtil.get(key, User.class);
            if (userx == null) {
                key = null;
            }
        }
        return key;
    }


    /**
     * 加载所有用户到redis
     */
    public void cacheUserToRedisByUid(String userUid) {
        String inCatch = isInCatch(userUid);
        if (StrUtil.isEmpty(inCatch)) {
            User user = getUserByUid(userUid);
            if(user!=null){
                redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, user.getUid()), user);
            }
        }
    }

    public void cacheAllUserToRedis() {
        List<User> users = this.allUser();
        for (User user : users) {
            String inCatch = isInCatch(user.getUid());
            if (StrUtil.isEmpty(inCatch)) {
                redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, user.getUid()), user);
            }
        }
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

        for (UserNumerical userNumerical : user.getUserNumericalMap().values()) {
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
            //购买时间
            Date key = entry.getKey();

            Fund fund = entry.getValue();

            //奖励已发放天数
            long l = DateUtil.betweenDay(key, new Date(), true);

            //如果还未发放完毕则保留
            if (l < fund.getDays()) {
                newfundsTreeMap.put(key, fund);
            }

        }

        user.getUserData().setFunds(redisObjectUtil.serialize(newfundsTreeMap));
    }


    /**
     * 获取邮件信息
     *
     * @param user
     * @return
     */
    public TreeMap<Date, Email> getEmails(User user) {
        byte[] emailsBytes = user.getUserData().getEmails();
        TreeMap<Date, Email> emailsTreeMap = null;

        if (emailsBytes == null) {
            emailsTreeMap = new TreeMap<>();
        } else {
            emailsTreeMap = redisObjectUtil.deserialize(emailsBytes, TreeMap.class);
        }

        return emailsTreeMap;
    }

    /**
     * 添加邮件
     *
     * @param user
     * @param email
     */
    public void addEmail(User user, Email email) {
        TreeMap<Date, Email> emailsTreeMap = this.getEmails(user);
        emailsTreeMap.put(email.getCreatDate(), email);
        user.getUserData().setEmails(redisObjectUtil.serialize(emailsTreeMap));
    }

    /**
     * 删除邮件
     *
     * @param user
     * @param email
     */
    public void deleteEmail(User user, Email email) {
        TreeMap<Date, Email> emailsTreeMap = this.getEmails(user);
        emailsTreeMap.remove(email.getCreatDate());
        user.getUserData().setEmails(redisObjectUtil.serialize(emailsTreeMap));
    }


    /**
     * 缓存玩家宠物
     *
     * @param user
     */
    public void cachePet(User user) {
        Pet p = new Pet();
        p.setUserUid(user.getUid());
        List<Pet> pets = petMapper.select(p);
        String userUidKey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, user.getUid());
        String allKey = RedisKeyUtil.getlkKey(userUidKey);
        redisObjectUtil.deleteAll(allKey);
        for (Pet pet : pets) {
            //数据库查询出所有技能
            PetSkill ps = new PetSkill(pet.getUid(), null);
            List<PetSkill> otherSkills = petSkillMapper.select(ps);
            pet.setOtherSkill(otherSkills);

            //获取petType

            String petTypeKey = RedisKeyUtil.getKey(RedisKey.PET_TYPE, pet.getTypeUid());
            PetType petType = redisObjectUtil.get(petTypeKey, PetType.class);
            pet.setTypeName(petType.getName());
            pet.setTypeDescription(petType.getDescription());
            pet.setTypeId(petType.getId());

            String key = RedisKeyUtil.getKey(userUidKey, pet.getUid());

            //初始化宠物，面板属性（战斗属性）
            new FightingPet(pet, redisObjectUtil).save(key);

        }

    }

    /**
     * 向数据库同步宠物信息
     *
     * @param user
     */
    public void saveSqlPet(User user) {
        String useruidkey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, user.getUid());
        HashMap<String, FightingPet> fightingPets = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(useruidkey), FightingPet.class);
        for (Map.Entry<String, FightingPet> entry : fightingPets.entrySet()) {
            //刷新宠物战斗对象缓存时间
            redisObjectUtil.refreshTime(entry.getKey());
            /**
             * 刷新数据库
             */
            Pet pet = entry.getValue().getPet();
            Pet pet1 = petMapper.selectByPrimaryKey(pet.getId());
            pet.setLastUpdateDate(null);

            if (pet1 != null) {
                petMapper.updateByPrimaryKeySelective(pet);
            } else {
                petMapper.insertSelective(pet);
                entry.getValue().setRedisObjectUtil(redisObjectUtil);
                entry.getValue().save();
            }


            PetSkill ps = new PetSkill(entry.getValue().getUid(), null);
            List<PetSkill> otherSkills = petSkillMapper.select(ps);

            //修改与增加
            if (entry.getValue().getPet().getOtherSkill() != null) {
                for (PetSkill petSkill : entry.getValue().getPet().getOtherSkill()) {
                    PetSkill petSkill1 = petSkillMapper.selectOne(petSkill);
                    if (petSkill1 == null) {
                        petSkillMapper.insertSelective(petSkill);
                    } else if (!petSkill1.equals(petSkill)) {
                        petSkillMapper.updateByPrimaryKeySelective(petSkill);
                    }
                }
                //删除
                otherSkills.removeAll(entry.getValue().getPet().getOtherSkill());
                for (PetSkill petSkill : otherSkills) {
                    petSkillMapper.delete(petSkill);
                }
            }


        }

    }

    /**
     * redis向数据库同步玩家以及宠物信息
     *
     * @param user
     */
    public void saveSqlUserAndPets(User user) {
        this.saveSqlUser(user);
        this.saveSqlPet(user);
    }

    public void saveSqlUserAndPetsAll() {
        HashMap<String, User> userAllCatch = this.getUserAllCatch();
        for (User user : userAllCatch.values()) {
            this.saveSqlUserAndPets(user);
        }
    }

    public void deleteAllUserCatch() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.USER));
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.OUTLINE_USER));
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.FIGHT_PETS));
    }


}
