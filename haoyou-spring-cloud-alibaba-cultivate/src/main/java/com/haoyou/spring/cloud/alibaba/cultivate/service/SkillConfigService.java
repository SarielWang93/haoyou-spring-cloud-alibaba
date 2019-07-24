package com.haoyou.spring.cloud.alibaba.cultivate.service;

import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetSkillMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetTypeMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.SkillConfigMsg;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.SkillBoard;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape.Tetromino;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @Author: wanghui
 * @Date: 2019/5/13 14:33
 * @Version 1.0
 */
@Service
public class SkillConfigService {
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private SendMsgUtil sendMsgUtil;
    @Autowired
    private PetMapper petMapper;
    @Autowired
    private PetTypeMapper petTypeMapper;
    @Autowired
    private PetSkillMapper petSkillMapper;

    @Autowired
    protected UserMapper userMapper;

    /**
     * 宠物添加技能
     * @param user
     * @param skillConfigMsg
     * @return
     */
    public boolean addPetSkill(User user, SkillConfigMsg skillConfigMsg,Prop prop){
        FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, skillConfigMsg.getPetUid(), redisObjectUtil);
        //获取宠物对应的技能盘
        SkillBoard skillBoard=getSkillBoard(fightingPet);
        if(skillBoard!=null){
            //技能盘，添加技能
            if(skillBoard.addSkill(skillConfigMsg.getTetromino(),prop)){
                try {
                    //修改缓存
                    fightingPet.getPet().setSkillBoard(redisObjectUtil.serialize(skillBoard));
                    fightingPet.getPet().getOtherSkill().add(new PetSkill(fightingPet.getUid(),prop.getProperty4()));
                    //删除道具
                    if(user.deleteProp(prop,1)){
                        fightingPet.init().save();
                        return true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 删除宠物技能
     * @param user
     * @param skillConfigMsg
     * @return
     */
    public boolean removePetSkill(User user, SkillConfigMsg skillConfigMsg){
        Tetromino tetromino = skillConfigMsg.getTetromino();

        FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, skillConfigMsg.getPetUid(), redisObjectUtil);
        //获取宠物对应的技能盘
        SkillBoard skillBoard=getSkillBoard(fightingPet);
        if(skillBoard!=null){
            //技能盘，移除技能
            String skillUid = skillBoard.removeSkill(tetromino);
            if(StrUtil.isNotEmpty(skillUid)){
                try {
                    //修改缓存
                    fightingPet.getPet().setSkillBoard(redisObjectUtil.serialize(skillBoard));
                    PetSkill bySkillUid = fightingPet.getPet().getBySkillUid(skillUid);
                    fightingPet.getPet().getOtherSkill().remove(bySkillUid);
                    fightingPet.init().save();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }


    /**
     * 给petType固有技能配置技能盘
     * @param user
     * @param skillConfigMsg
     * @return
     */
    public boolean setPetTypeSkill(User user, SkillConfigMsg skillConfigMsg){

        FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, skillConfigMsg.getPetUid(), redisObjectUtil);
        Pet pet = fightingPet.getPet();
        String skillUid = skillConfigMsg.getSkillUid();

        //只能配置基础技能
        if(!Objects.equals(pet.getInhSkill(),skillUid)
                && Objects.equals(pet.getUniqueSkill(),skillUid)
                && Objects.equals(pet.getTalentSkill(),skillUid)){
            return false;
        }


        //获取宠物对应的技能盘
        SkillBoard skillBoard=getSkillBoard(fightingPet);

        Prop prop = new Prop();
        prop.setProperty2(skillConfigMsg.getTetromino().getType());
        prop.setProperty4(skillUid);

        if(skillBoard!=null && StrUtil.isNotEmpty(skillUid)){
            //技能盘，添加技能
            if(skillBoard.addSkill(skillConfigMsg.getTetromino(),prop)){
                try {

                    PetType petType = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PET_TYPE, fightingPet.getPet().getTypeUid()), PetType.class);


                    //修改缓存
                    petType.setSkillBoard(redisObjectUtil.serialize(skillBoard));

                    redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.PET_TYPE, fightingPet.getPet().getTypeUid()),petType);
                    petTypeMapper.updateByPrimaryKeySelective(petType);


//                    Pet spet = new Pet();
//                    spet.setTypeUid(petType.getUid());
//
//                    List<Pet> select = petMapper.select(spet);
//                    for(Pet petx:select){
//                        petx.setSkillBoard(petType.getSkillBoard());
//                        petx.setLastUpdateDate(null);
//                        petMapper.updateByPrimaryKeySelective(petx);
//                    }


                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;

    }






    /**
     * 获取技能配置盘
     * @param fightingPet
     * @return
     */
    private SkillBoard getSkillBoard(FightingPet fightingPet){

        if(fightingPet.getPet().getSkillBoard()!=null){
            return redisObjectUtil.deserialize(fightingPet.getPet().getSkillBoard(), SkillBoard.class);
        }else{
            return new SkillBoard(6,6);
        }
    }

}
