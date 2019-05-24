package com.haoyou.spring.cloud.alibaba.login.UserCatch;

import com.alibaba.dubbo.config.annotation.Reference;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.PetSkill;
import com.haoyou.spring.cloud.alibaba.commons.entity.PetType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetSkillMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.service.fighting.FightingService;
import com.haoyou.spring.cloud.alibaba.action.RedisObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录，登出时，用户信息缓存，以及删除
 */
@Component
public class UserDateSynchronization {
    private final static Logger logger = LoggerFactory.getLogger(UserDateSynchronization.class);
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PetMapper petMapper;
    @Autowired
    private PetSkillMapper petSkillMapper;
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Reference(version = "${fighting.service.version}")
    private FightingService fightingService;

    /**
     * 缓存用户信息到redis
     * @param user
     * @return
     */
    public boolean cache(User user){
        //TODO 读取缓存用户所有信息
        logger.info(String.format("cacheUser: %s",user.getName()));

        String key = RedisKeyUtil.getKey(RedisKey.USER, user.getUid());
        if(redisObjectUtil.save(key, user)){
            //缓存宠物信息
            cachePet(user);

            logger.info(String.format("%s 登录成功！！",user.getName()));
            return true;
        }

        return false;
    }

    /**
     * 缓存玩家宠物
     * @param user
     */
    public void cachePet(User user){
        Pet p = new Pet();
        p.setUserUid(user.getUid());
        List<Pet> pets = petMapper.select(p);
        String useruidkey = RedisKeyUtil.getKey(RedisKey.PETS, user.getUid());
        for(Pet pet:pets){
            //数据库查询出所有技能
            List<String> otherSkill = new ArrayList<>();
            PetSkill ps=new PetSkill();
            ps.setPetUid(pet.getUid());
            List<PetSkill> otherSkills = petSkillMapper.select(ps);
            for(PetSkill petSkill:otherSkills){
                otherSkill.add(petSkill.getSkillUid());
            }
            pet.setOtherSkill(otherSkill);

            //获取petType

            String petTypeKey = RedisKeyUtil.getKey(RedisKey.PET_Type, pet.getTypeUid());
            PetType petType = redisObjectUtil.get(petTypeKey, PetType.class);
            pet.setPetType(petType);

            String key = RedisKeyUtil.getKey(useruidkey, pet.getUid());
            redisObjectUtil.save(key,pet);

            //初始化宠物，面板属性（战斗属性）
            fightingService.newFightingPet(pet);

        }

    }


    /**
     * 清除玩家缓存
     * @param user
     */
    public void deletePet(User user){
        String useruidkey = RedisKeyUtil.getKey(RedisKey.PETS, user.getUid());
        HashMap<String, Pet> pets = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(useruidkey),Pet.class);
        for(Map.Entry<String, Pet> entry:pets.entrySet()){

            //删除宠物战斗对象缓存
            fightingService.deleteFightingPet(entry.getValue());
            //删除宠物缓存信息
            redisObjectUtil.delete(entry.getKey());

        }

    }


    public boolean removeCache(User user){
        //TODO 清除用户所有缓存信息
        this.deletePet(user);
        String key = RedisKeyUtil.getKey(RedisKey.USER, user.getUid());
        return redisObjectUtil.delete(key);
    }

}
