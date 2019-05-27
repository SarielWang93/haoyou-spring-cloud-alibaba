package com.haoyou.spring.cloud.alibaba.login.UserCatch;

import com.alibaba.dubbo.config.annotation.Reference;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.PetSkill;
import com.haoyou.spring.cloud.alibaba.commons.entity.PetType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetSkillMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.service.fighting.FightingService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
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
        logger.info(String.format("cacheUser: %s",user.getUsername()));

        String key = RedisKeyUtil.getKey(RedisKey.USER, user.getUid());
        if(redisObjectUtil.save(key, user)){
            //缓存宠物信息
            cachePet(user);

            logger.info(String.format("%s 登录成功！！",user.getUsername()));
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
        String userUidKey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, user.getUid());
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

            String petTypeKey = RedisKeyUtil.getKey(RedisKey.PET_TYPE, pet.getTypeUid());
            PetType petType = redisObjectUtil.get(petTypeKey, PetType.class);
            pet.setTypeName(petType.getName());
            pet.setTypeId(petType.getId());

            String key = RedisKeyUtil.getKey(userUidKey, pet.getUid());

            //初始化宠物，面板属性（战斗属性）
            new FightingPet(pet,redisObjectUtil).save(key);

        }

    }


    /**
     * 清除玩家缓存
     * @param user
     */
    public void deletePet(User user){
        String useruidkey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, user.getUid());
        HashMap<String, FightingPet> fightingPets = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(useruidkey),FightingPet.class);
        for(Map.Entry<String, FightingPet> entry:fightingPets.entrySet()){

            //删除宠物战斗对象缓存
            entry.getValue().setRedisObjectUtil(redisObjectUtil);
            entry.getValue().delete();

        }

    }


    public boolean removeCache(User user){
        //TODO 清除用户所有缓存信息
        this.deletePet(user);
        String key = RedisKeyUtil.getKey(RedisKey.USER, user.getUid());
        return redisObjectUtil.delete(key);
    }

}
