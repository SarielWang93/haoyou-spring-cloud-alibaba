package com.haoyou.spring.cloud.alibaba.login.UserCatch;

import com.haoyou.spring.cloud.alibaba.commons.mapper.CurrencyMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.PetSkill;
import com.haoyou.spring.cloud.alibaba.commons.entity.PetType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetSkillMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

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
    private CurrencyMapper currencyMapper;
    @Autowired
    private RedisObjectUtil redisObjectUtil;

    /**
     * 每隔30分钟,将缓存同步到数据库
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void synchronization() {
        logger.info(String.format("synchronization"));
        HashMap<String, User> users = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.USER), User.class);
        for (User user : users.values()) {
            this.saveSqlUserAndPets(user);
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

        if (redisObjectUtil.save(key, user)) {

            if (!user.isOnLine()) {
                this.cachePet(user);
                user.setOnLine(true);
            }else{
                //获取宠物个数
                Example e = new Example(Pet.class);
                e.setCountProperty("id");
                e.createCriteria().andEqualTo("userUid",user.getUid());
                int i = petMapper.selectCountByExample(e);
                String userUidKey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, user.getUid());
                String allKey = RedisKeyUtil.getlkKey(userUidKey);
                HashMap<String, Pet> allPet = redisObjectUtil.getlkMap(allKey,Pet.class);
                if(i!=allPet.size()){
                    this.cachePet(user);
                }
            }
            //缓存宠物信息
            logger.info(String.format("%s 登录成功！！", user.getUsername()));
            return true;
        }

        return false;
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
            pet.setTypeId(petType.getId());

            String key = RedisKeyUtil.getKey(userUidKey, pet.getUid());

            //初始化宠物，面板属性（战斗属性）
            new FightingPet(pet, redisObjectUtil).save(key);

        }

    }

    /**
     * 登出内存操作
     *
     * @param user
     * @return
     */
    public boolean removeCache(User user) {
        this.saveSqlUserAndPets(user);
        //TODO 清除用户所有缓存信息
        String key = RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, user.getUid());
        String key1 = RedisKeyUtil.getKey(RedisKey.USER, user.getUid());
        user.setOnLine(false);
        redisObjectUtil.save(key, user);
        return redisObjectUtil.delete(key1);
    }

    /**
     * redis向数据库同步玩家以及宠物信息
     * @param user
     */
    public void saveSqlUserAndPets(User user) {
        this.saveSqlUser(user);
        this.saveSqlPet(user);
    }
    /**
     * 向数据库同步玩家信息
     *
     * @param user
     */
    public void saveSqlUser(User user) {
        user.setLastLoginOutDate(new Date());
        user.setLastUpdateDate(new Date());
        userMapper.updateByPrimaryKeySelective(user);
        currencyMapper.updateByPrimaryKeySelective(user.getCurrency());
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
            petMapper.updateByPrimaryKeySelective(entry.getValue().getPet());
            PetSkill ps = new PetSkill(entry.getValue().getUid(), null);
            List<PetSkill> otherSkills = petSkillMapper.select(ps);

            //修改与增加
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
