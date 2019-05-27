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
                    fightingPet.getPet().setSkillBoardJosn(MapperUtils.obj2json(skillBoard));
                    fightingPet.getPet().getOtherSkill().add(prop.getProperty1());
                    fightingPet.save();
                    //添加数据库
                    PetSkill ps=new PetSkill();
                    ps.setPetUid(fightingPet.getUid());
                    ps.setSkillUid(prop.getProperty1());
                    petSkillMapper.insert(ps);
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
            //技能盘，添加技能
            String skillUid = skillBoard.removeSkill(tetromino);
            if(StrUtil.isNotEmpty(skillUid)){
                try {
                    //修改缓存
                    fightingPet.getPet().setSkillBoardJosn(MapperUtils.obj2json(skillBoard));
                    fightingPet.getPet().getOtherSkill().remove(skillUid);
                    fightingPet.save();
                    //删除数据库
                    PetSkill ps=new PetSkill();
                    ps.setPetUid(fightingPet.getUid());
                    ps.setSkillUid(skillUid);
                    petSkillMapper.delete(ps);
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
        String skillBoardJosn = fightingPet.getPet().getSkillBoardJosn();

        if(StrUtil.isNotEmpty(skillBoardJosn)){
            try {
                return MapperUtils.json2pojo(skillBoardJosn, SkillBoard.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
