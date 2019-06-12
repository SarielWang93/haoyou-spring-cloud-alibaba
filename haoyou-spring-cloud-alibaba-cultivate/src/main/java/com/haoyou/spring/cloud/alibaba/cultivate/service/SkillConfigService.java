package com.haoyou.spring.cloud.alibaba.cultivate.service;

import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.entity.PetSkill;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetSkillMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.cultivate.msg.SkillConfigMsg;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.SkillBoard;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape.Tetromino;
import com.haoyou.spring.cloud.alibaba.serialization.JsonSerializer;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private PetSkillMapper petSkillMapper;

    /**
     * 宠物添加技能
     * @param user
     * @param skillConfigMsg
     * @return
     */
    public boolean addPetSkill(User user, SkillConfigMsg skillConfigMsg){
        Prop prop = skillConfigMsg.getProp();
        FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, skillConfigMsg.getPetUid(), redisObjectUtil);
        //获取宠物对应的技能盘
        SkillBoard skillBoard=getSkillBoard(fightingPet);
        if(skillBoard!=null){
            //技能盘，添加技能
            if(skillBoard.addSkill(skillConfigMsg.getTetromino(),prop)){
                try {
                    //修改缓存
                    fightingPet.getPet().setSkillBoard(redisObjectUtil.serialize(skillBoard));
                    fightingPet.getPet().getOtherSkill().add(new PetSkill(fightingPet.getUid(),prop.getProperty1()));
                    fightingPet.save();
                    petMapper.updateByPrimaryKeySelective(fightingPet.getPet());
                    //添加数据库
                    PetSkill ps=new PetSkill(fightingPet.getUid(),prop.getProperty1());
                    petSkillMapper.insertSelective(ps);
                    return true;
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
                    fightingPet.save();
                    petMapper.updateByPrimaryKeySelective(fightingPet.getPet());
                    //删除数据库
                    petSkillMapper.delete(bySkillUid);
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
            return new SkillBoard(9,9);
        }
    }

}
