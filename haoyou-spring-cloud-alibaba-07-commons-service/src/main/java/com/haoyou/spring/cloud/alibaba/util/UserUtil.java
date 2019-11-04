package com.haoyou.spring.cloud.alibaba.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Console;
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
import com.haoyou.spring.cloud.alibaba.pojo.bean.Badge;
import com.haoyou.spring.cloud.alibaba.pojo.bean.ChatRecord;
import com.haoyou.spring.cloud.alibaba.pojo.bean.DailyCheckIn;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
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


    public static final int friendsMaxCount = 30;

    public static final int vitalityMaxCount = 200;

    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private SendMsgUtil sendMsgUtil;


    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserDataMapper userDataMapper;

    @Autowired
    private UserNumericalMapper userNumericalMapper;
    @Autowired
    private CurrencyMapper currencyMapper;

    @Autowired
    private FriendsMapper friendsMapper;

    @Autowired
    private PetMapper petMapper;
    @Autowired
    private PetSkillMapper petSkillMapper;
    @Autowired
    private LandMapper landMapper;

    public Award getAward(String type) {
        Award award = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, type), Award.class);
        return award;
    }


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
                this.cacheUserAndPet(user);
            }
        } else {
            user = redisObjectUtil.get(key, User.class);
        }
        return user;
    }

    /**
     * 根据idNum获取用户信息
     *
     * @param idNum
     * @return
     */
    public User getUserByIdNum(String idNum) {
        User user = new User();
        user.setIdNum(idNum);
        user = userMapper.selectOne(user);
        user = this.getUserByUid(user.getUid());
        return user;
    }

    /**
     * 根据name获取用户信息
     *
     * @param name
     * @return
     */
    public User getUserByName(String name) {
        List<User> users = this.allUser();

        for (User user : users) {

            if (user.getUserData().getName().equals(name)) {
                return user;
            }

        }
        return null;
    }

    /**
     * 根据userName获取用户信息
     *
     * @param userName
     * @return
     */
    public User getUserByUserName(String userName) {
        User user = new User();
        user.setUsername(userName);
        user = userMapper.selectOne(user);
        user = this.getUserByUid(user.getUid());
        return user;
    }

    /**
     * 根据device获取游客信息
     *
     * @param deviceUid
     * @return
     */
    public User getUserByDeviceUid(String deviceUid) {
        if(StrUtil.isNotEmpty(deviceUid)){
            HashMap<String, User> userAllCatch = this.getUserAllCatch();
            for (User user : userAllCatch.values()) {
                if (deviceUid.equals(user.getLastLoginDevice()) && (StrUtil.isEmpty(user.getUsername()) || StrUtil.isEmpty(user.getPassword())) ) {
                    return user;
                }
            }

            User users = new User();
            users.setLastLoginDevice(deviceUid);
            List<User> select = userMapper.select(users);
            for(User user : select){
                if((StrUtil.isEmpty(user.getUsername()) || StrUtil.isEmpty(user.getPassword()))){
                    return user;
                }
            }


        }
        return null;
    }

    /**
     * 获取玩家宠物
     *
     * @param userUid
     * @return
     */
    public List<Pet> getUserPets(String userUid) {
        String userUidKey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, userUid);
        String allKey = RedisKeyUtil.getlkKey(userUidKey);
        HashMap<String, Pet> stringPetHashMap = redisObjectUtil.getlkMap(allKey, Pet.class);

        TreeMap<Integer, Pet> petTreeMap = new TreeMap<>();
        for (Pet pet : stringPetHashMap.values()) {
            petTreeMap.put(pet.getLevel(), pet);
        }
        List<Pet> petList = CollUtil.newArrayList(petTreeMap.values());

        return petList;
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

    public void saveUser(User user, String redisKey) {
        redisObjectUtil.save(RedisKeyUtil.getKey(redisKey, user.getUid()), user);
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

    public void deleteInCatch(String UserUid) {
        redisObjectUtil.delete(isInCatch(UserUid));
    }
    public User refreshCatch(String UserUid) {

        String inCatch = isInCatch(UserUid);
        if(StrUtil.isEmpty(inCatch)){
            inCatch = RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, UserUid);
        }else{
            redisObjectUtil.delete(inCatch);
        }
        User userByUid = this.getUserByUid(UserUid);
        redisObjectUtil.save(inCatch,userByUid);

        return userByUid;
    }


    /**
     * 加载所有用户到redis
     */
    public void cacheUserToRedisByUid(String userUid) {
        String inCatch = isInCatch(userUid);
        if (StrUtil.isEmpty(inCatch)) {
            User user = getUserByUid(userUid);
            if (user != null) {
                redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, user.getUid()), user);
            }
            cachePet(user);
        }
    }

    public void cacheAllUserToRedis() {
        List<User> users = this.allUser();
        for (User user : users) {
            String inCatch = isInCatch(user.getUid());
            if (StrUtil.isEmpty(inCatch)) {
                redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, user.getUid()), user);
            }
            cachePet(user);
        }
    }

    /**
     * 加载用户
     *
     * @param user
     */
    public void cacheUser(User user) {
        //加载货币信息
        Currency currency = new Currency();
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
        user.setUserNumericalMap(userNumericalMap);
        //加载好友系统

        Friends friend1select = new Friends();
        friend1select.setUserUid1(user.getUid());
        Friends friend2select = new Friends();
        friend2select.setUserUid2(user.getUid());

        List<Friends> select = friendsMapper.select(friend1select);
        select.addAll(friendsMapper.select(friend2select));

        for (Friends friend : select) {

            saveFriend(friend);

        }

        //加载土地信息
        Land landSelect = new Land();
        landSelect.setUserUid(user.getUid());
        List<Land> lands = landMapper.select(landSelect);
        if (lands.size() == 0) {
            addLand(user.getUid());
        }
        for (Land land : lands) {
            String landKey = RedisKeyUtil.getKey(RedisKey.LAND, land.getUserUid(), land.getUid());
            redisObjectUtil.save(landKey, land, -1);
        }


        //每日签到
        if (user.getUserData().getDailyCheckIn() == null) {
            this.setDailyCheckIn(user);
            userDataMapper.updateByPrimaryKeySelective(user.getUserData());
        }

    }

    /**
     * @param user
     */
    public void cacheUserAndPet(User user) {
        this.cacheUser(user);
        this.cachePet(user);
    }

    /**
     * 向数据库同步玩家信息
     *
     * @param user
     */
    public void saveSqlUser(User user) {
        userMapper.updateByPrimaryKey(user);

        currencyMapper.updateByPrimaryKey(user.getCurrency());

        userDataMapper.updateByPrimaryKey(user.getUserData());

        //修改或者新增
        if(user.getUserNumericalMap()!=null){
            for (UserNumerical userNumerical : user.getUserNumericalMap().values()) {
                if (userNumerical.getId() == null) {
                    userNumericalMapper.insertSelective(userNumerical);
                } else {
                    userNumericalMapper.updateByPrimaryKey(userNumerical);
                }
            }
        }

        //土地同步

        String landlkKey = RedisKeyUtil.getlkKey(RedisKey.LAND, user.getUid());

        HashMap<String, Land> stringLandHashMap = redisObjectUtil.getlkMap(landlkKey, Land.class);

        for (Map.Entry<String, Land> entry : stringLandHashMap.entrySet()) {
            Land land=entry.getValue();
            if (land.getId() == null) {
                redisObjectUtil.delete(entry.getKey());
                landMapper.insertSelective(land);
                redisObjectUtil.save(entry.getKey(),land);
            } else {
                landMapper.updateByPrimaryKey(land);
            }
        }


        //同步好友信息到数据库
        saveSqlFriends(user);
    }

    /**
     * 同步好友信息到数据库
     *
     * @param user
     */
    public void saveSqlFriends(User user) {

        String friendlkKey = RedisKeyUtil.getlkKey(RedisKey.USER_FRIENDS, user.getUid());

        HashMap<String, Integer> stringIntegerHashMap = redisObjectUtil.getlkMap(friendlkKey, Integer.class);

        for (Integer i : stringIntegerHashMap.values()) {
            String friendKey = RedisKeyUtil.getKey(RedisKey.FRIENDS, i.toString());

            Friends friends = redisObjectUtil.get(friendKey, Friends.class);

            friendsMapper.updateByPrimaryKeySelective(friends);


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
        if (propList == null || user == null) {
            return;
        }
        try {
            List<Prop> propsThis = user.propList();
            List<Prop> propsOver = new ArrayList<>();

            for (Prop prop : propList) {

                long count = 1;
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
        int tcount = 2;

        //随机一个版本
        int t = RandomUtil.randomInt(tcount) + 1;

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
        HashMap<String, User> userLogin = this.getUserLogin();
        sendMsgUtil.sendDownUserList(userLogin.values());
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.USER));
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.OUTLINE_USER));
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.FIGHT_PETS));
    }


    public void refreshAllUserCatch() {
        saveSqlUserAndPetsAll();
        deleteAllUserCatch();
        cacheAllUserToRedis();
    }


    /**
     * 屏蔽词汇替换
     *
     * @param msg
     * @return
     */
    public String replaceAllShieldVocas(String msg) {

        String shieldVocaKey = RedisKeyUtil.getKey(RedisKey.SHIELD_VOCA);
        List<String> list = redisObjectUtil.get(shieldVocaKey, List.class);
        for (String shieldVoca : list) {
            if (StrUtil.containsAny(msg, shieldVoca)) {
                StringBuilder builder = StrUtil.builder();
                for (int i = 0; i < shieldVoca.length(); i++) {
                    builder.append("*");
                }
                msg = StrUtil.replace(msg, shieldVoca, builder.toString());
            }
        }
        return msg;
    }

    /**
     * 是否拥有屏蔽词
     *
     * @param msg
     * @return
     */
    public boolean hasShieldVocas(String msg) {
        String shieldVocaKey = RedisKeyUtil.getKey(RedisKey.SHIELD_VOCA);
        List<String> shieldVocas = redisObjectUtil.get(shieldVocaKey, List.class);
        for (String shieldVoca : shieldVocas) {
            if (msg.contains(shieldVoca)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取聊天记录
     *
     * @param user
     * @param userUid
     * @return
     */
    public List<ChatRecord> getChatRecord(User user, String userUid) {
        Friends friend = getFriend(user, userUid);
        List<ChatRecord> chatRecords = getChatRecord(friend);
        for (ChatRecord chatRecord : chatRecords) {
            if (chatRecord.isNotRead() && user.getUid().equals(chatRecord.getUserUid())) {
                chatRecord.setNotRead(false);
            }
        }
        friend.setChatRecord(redisObjectUtil.serialize(chatRecords));
        saveFriend(friend);
        return chatRecords;
    }

    public List<ChatRecord> getChatRecord(Friends friend) {
        List<ChatRecord> deserialize = null;
        if (friend.getChatRecord() == null) {
            deserialize = new ArrayList<>();
        } else {
            deserialize = redisObjectUtil.deserialize(friend.getChatRecord(), List.class);
        }
        return deserialize;
    }

    /**
     * 添加聊天记录
     *
     * @param user
     * @param userUid
     * @param sendMsg
     */
    public ChatRecord addChatRecord(User user, String userUid, String sendMsg) {
        return addChatRecord(user, userUid, sendMsg, false);
    }

    public ChatRecord addChatRecord(User user, String userUid, String sendMsg, boolean notSend) {
        ChatRecord chatRecord = new ChatRecord(user.getUid(), new Date(), sendMsg);
        Friends friend = getFriend(user, userUid);
        List<ChatRecord> chatRecords = getChatRecord(friend);


        chatRecord.setNotRead(notSend);
        chatRecords.add(chatRecord);

        if (chatRecords.size() > 50) {
            chatRecords.remove(50);
        }


        friend.setChatRecord(redisObjectUtil.serialize(chatRecords));
        saveFriend(friend);

        return chatRecord;
    }

    /**
     * 保存好友对象
     *
     * @param friend
     */
    public void saveFriend(Friends friend) {
        if (friend.getId() == null) {
            friendsMapper.insertSelective(friend);
        }

        String friendKey = RedisKeyUtil.getKey(RedisKey.FRIENDS, friend.getId().toString());
        redisObjectUtil.save(friendKey, friend, -1);

        String friend1Key = RedisKeyUtil.getKey(RedisKey.USER_FRIENDS, friend.getUserUid1(), friend.getUserUid2());
        String friend2Key = RedisKeyUtil.getKey(RedisKey.USER_FRIENDS, friend.getUserUid2(), friend.getUserUid1());

        redisObjectUtil.save(friend1Key, friend.getId(), -1);
        redisObjectUtil.save(friend2Key, friend.getId(), -1);
    }

    /**
     * 删除好友
     *
     * @param friend
     */
    public void deleteFriend(Friends friend) {
        String friend1Key = RedisKeyUtil.getKey(RedisKey.USER_FRIENDS, friend.getUserUid1(), friend.getUserUid2());
        String friend2Key = RedisKeyUtil.getKey(RedisKey.USER_FRIENDS, friend.getUserUid2(), friend.getUserUid1());

        redisObjectUtil.delete(friend1Key);
        redisObjectUtil.delete(friend2Key);

        String friendKey = RedisKeyUtil.getKey(RedisKey.FRIENDS, friend.getId().toString());
        redisObjectUtil.delete(friendKey);

        friendsMapper.delete(friend);
    }

    /**
     * 获取好友信息
     *
     * @param user
     * @param userUid
     * @return
     */
    public Friends getFriend(User user, String userUid) {
        Integer friendId = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER_FRIENDS, user.getUid(), userUid), Integer.class);

        String friendKey = RedisKeyUtil.getKey(RedisKey.FRIENDS, friendId.toString());

        return redisObjectUtil.get(friendKey, Friends.class);
    }

    /**
     * 获取所有好友
     *
     * @param userUid
     * @return
     */
    public List<Friends> getFriends(String userUid) {
        List<Friends> friends = new ArrayList<>();
        HashMap<String, Integer> stringIntegerHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.USER_FRIENDS, userUid), Integer.class);
        for (Integer i : stringIntegerHashMap.values()) {
            String friendKey = RedisKeyUtil.getKey(RedisKey.FRIENDS, i.toString());
            friends.add(redisObjectUtil.get(friendKey, Friends.class));
        }
        return friends;
    }
    public List<String> getFriendsUid(String userUid) {
        List<Friends> friends = getFriends(userUid);

        List<String> friendsUid = new ArrayList<>();
        for(Friends friend:friends){

            String friendUid = friend.getUserUid1();
            if(userUid.equals(friendUid)){
                friendUid = friend.getUserUid2();
            }
            friendsUid.add(friendUid);

        }
        return friendsUid;
    }
    /**
     * 好友是否到达上限
     *
     * @param userUid
     * @return
     */
    public boolean friendsIsFull(String userUid) {
        List<Friends> friends = getFriends(userUid);

        if (friends.size() >= friendsMaxCount) {
            return true;
        }
        return false;
    }

    /**
     * 获取运行天数
     *
     * @param date
     */
    public long getRuningDays(DateTime date) {

        HashMap<String, Server> stringServerHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.SERVER), Server.class);
        TreeMap<Date, Server> serverTreeMap = new TreeMap<>();
        for (Server server : stringServerHashMap.values()) {
            serverTreeMap.put(server.getCreatDate(), server);
        }
        Map.Entry<Date, Server> firstEntry = serverTreeMap.firstEntry();

        return DateUtil.betweenDay(firstEntry.getKey(), date, true);
    }

    /**
     * 获取徽章
     *
     * @param userUid
     * @return
     */
    public List<Badge> getBadges(String userUid) {
        User userByUid = getUserByUid(userUid);
        return getBadges(userByUid);
    }

    public List<Badge> getBadges(User user) {

        List<Badge> badges = new ArrayList<>();
        if(user.getUserData().getBadges()!=null){
            badges = redisObjectUtil.deserialize(user.getUserData().getBadges(), List.class);
        }


        return badges;
    }

    public boolean addBadges(String userUid, LevelDesign levelDesign, int difficult) {
        User userByUid = getUserByUid(userUid);
        List<Badge> badges = getBadges(userByUid);
        Badge badge = new Badge(levelDesign.getChapterName(), levelDesign.getIdNum(), difficult);
        if (!badges.contains(badge)) {
            badges.add(badge);
        } else {
            return false;
        }
        userByUid.getUserData().setBadges(redisObjectUtil.serialize(badges));
        saveUser(userByUid);
        return true;
    }

    /**
     * 增加土地
     * @param userUid
     */
    public Land addLand(String userUid){
        Land land = new Land();
        land.setUserUid(userUid);
        land.setUid(IdUtil.simpleUUID());
        land.setLevel(1);
        land.setReductionTime(0);
        land.setIncreaseOutput(0);
        land.setCropCount(0);
        land.setBeingStolen(0);
        String landKey = RedisKeyUtil.getKey(RedisKey.LAND, userUid, land.getUid());
        if(redisObjectUtil.save(landKey, land, -1)){
            return land;
        }
        return null;
    }

    /**
     * 获取用户所有土地
     * @param userUid
     * @return
     */
    public List<Land> getLands(String userUid){
        String landlkKey = RedisKeyUtil.getlkKey(RedisKey.LAND, userUid);
        HashMap<String, Land> stringLandHashMap = redisObjectUtil.getlkMap(landlkKey, Land.class);
        return CollUtil.newArrayList(stringLandHashMap.values());
    }
    /**
     * 获得土地对象
     * @param userUid
     * @param landUid
     * @return
     */
    public Land getLand(String userUid,String landUid){
        String landKey = RedisKeyUtil.getKey(RedisKey.LAND, userUid, landUid);
        Land land = redisObjectUtil.get(landKey, Land.class);
        return land;
    }

    /**
     * 保存土地
     * @param land
     */
    public void saveLand(Land land){
        String landKey = RedisKeyUtil.getKey(RedisKey.LAND, land.getUserUid(), land.getUid());
        redisObjectUtil.save(landKey, land, -1);
    }



    /**
     * 获取参数map
     * @param req
     * @return
     */
    public Map<String, Object> getMsgMap(MyRequest req){
        try {
            return MapperUtils.json2map(new String(req.getMsg()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取道具
     * @param userUid
     * @param propInstenceUid
     * @return
     */
    public Prop getPropByInstenceUid(String userUid,String propInstenceUid){
        User userByUid = getUserByUid(userUid);
        return getPropByInstenceUid(userByUid,propInstenceUid);
    }
    public Prop getPropByInstenceUid(User user,String propInstenceUid){
        if(StrUtil.isEmpty(propInstenceUid)){
            return null;
        }
        List<Prop> props = user.propList();
        for (Prop prop : props) {
            if (propInstenceUid.equals(prop.getPropInstenceUid())) {
                return prop;
            }
        }
        return null;
    }

}
