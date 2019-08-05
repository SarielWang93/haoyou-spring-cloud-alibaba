package com.haoyou.spring.cloud.alibaba.cultivate.controller;

import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.RewardType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.mapper.AwardMapper;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.PetType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.service.EmailService;
import com.haoyou.spring.cloud.alibaba.cultivate.service.RewardService;
import com.haoyou.spring.cloud.alibaba.cultivate.settle.handle.RankSettleHandle;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PetUpLevMsg;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.UpdateIsworkMsg;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.service.cultivate.CultivateService;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class ManagerController {
    private final static Logger logger = LoggerFactory.getLogger(ManagerController.class);


    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private CultivateService cultivateService;
    @Autowired
    private SendMsgUtil sendMsgUtil;
    @Autowired
    private AwardMapper awardMapper;
    @Autowired
    private RewardService rewardService;
    @Autowired
    private EmailService emailService;

    /**
     * 设置PVE胜利奖励
     *
     * @param award
     * @return
     */
    @CrossOrigin
    @PostMapping(value = "setPVEAward")
    public String setPVEAward(@RequestBody Award award) {

        Award awardOld = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, award.getType()), Award.class);

        if (awardOld != null) {
            awardOld.init(award.getCoin(), award.getDiamond(), award.getExp(), award.getPetExp(), award.getPropsList());

            redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.AWARD, award.getType()), awardOld);

            awardMapper.updateByPrimaryKeySelective(awardOld);
        }else{
            awardOld = new Award().init(award.getCoin(), award.getDiamond(), award.getExp(), award.getPetExp(), award.getPropsList());
            awardOld.setType(award.getType());
            awardMapper.insertSelective(awardOld);
            redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.AWARD, awardOld.getType()), awardOld);
        }


        return "success";

    }

    /**
     * 获取PVE胜利奖励
     *
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "getPVEAward")
    public String getPVEAward(String type) {
        if (StrUtil.isEmpty(type)) {
            HashMap<String, Award> awards = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.AWARD), Award.class);
            for (Award award : awards.values()) {
                award.setPropsList(award.propList());
                award.setProps(null);
            }
            try {
                return MapperUtils.obj2jsonIgnoreNull(awards.values());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Award award = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, type), Award.class);
            award.setPropsList(award.propList());
            award.setProps(null);
            try {
                return MapperUtils.obj2jsonIgnoreNull(award);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    /**
     * 获得PVE胜利奖励
     *
     * @param userUid
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "getPVEReward")
    public String getPVEReward(String userUid,String type) {

        User user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, userUid), User.class);

        if (cultivateService.rewards(user, type)) {
            return "success";
        }
        return "err";

    }

    /**
     * 宠物升级
     *
     * @param userUid
     * @param petUid
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "petUplevel")
    public String petUplevel(String userUid, String petUid) {
        PetUpLevMsg msg = new PetUpLevMsg();
        msg.setPetUid(petUid);
        User user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, userUid), User.class);


        MyRequest myRequest = new MyRequest();
        myRequest.setMsg(sendMsgUtil.serialize(msg));
        myRequest.setUser(user);
        BaseMessage baseMessage = cultivateService.petUpLev(myRequest);
        try {
            return MapperUtils.obj2jsonIgnoreNull(baseMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取道具列表
     *
     * @param userUid
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "getProps")
    public String getProps(String userUid) {
        User user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, userUid), User.class);

        try {
            return MapperUtils.obj2jsonIgnoreNull(user.propList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用道具
     *
     * @param userUid
     * @param petUid
     * @param propInstenceUid
     * @param propCount
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "useProps")
    public String useProps(String userUid, String petUid, String propInstenceUid, int propCount, int type) {

        PropUseMsg propUseMsg = new PropUseMsg();
        propUseMsg.setPetUid(petUid);
        propUseMsg.setPropInstenceUid(propInstenceUid);
        propUseMsg.setPropCount(propCount);
        propUseMsg.setType(type);

        User user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, userUid), User.class);


        MyRequest myRequest = new MyRequest();
        myRequest.setMsg(sendMsgUtil.serialize(propUseMsg));
        myRequest.setUser(user);

        BaseMessage baseMessage = cultivateService.propUse(myRequest);

        try {
            return MapperUtils.obj2jsonIgnoreNull(baseMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 宠物设置出战
     *
     * @param userUid
     * @param petUid
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "updateIsWork")
    public String updateIsWork(String userUid, String petUid, int iswork) {
        UpdateIsworkMsg msg = new UpdateIsworkMsg();
        msg.setPetUid(petUid);
        msg.setIswork(iswork);
        User user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, userUid), User.class);

        MyRequest myRequest = new MyRequest();
        myRequest.setMsg(sendMsgUtil.serialize(msg));
        myRequest.setUser(user);

        if (cultivateService.updateIsWork(myRequest)) {
            return "success";
        }
        return "err";
    }

    /**
     * 重置宠物
     * @param userUid
     * @param petUid
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "reInitPet")
    public String reInitPet(String userUid, String petUid) {
        //获取用户，宠物，以及宠物类型
        User user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, userUid), User.class);
        FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, petUid, redisObjectUtil);
        Pet pet = fightingPet.getPet();
        String typeUid = fightingPet.getPet().getTypeUid();
        PetType petType = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PET_TYPE, typeUid), PetType.class);

        //新的宠物
        Pet newPet = new Pet(user, petType, 0);
        newPet.setUid(pet.getUid());
        newPet.setId(pet.getId());
        newPet.setOtherSkill(new ArrayList<>());

        FightingPet newFightingPet = new FightingPet(newPet, redisObjectUtil);
        String userUidKey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, user.getUid());
        String key = RedisKeyUtil.getKey(userUidKey, pet.getUid());
        newFightingPet.save(key);

        try {
            return MapperUtils.obj2jsonIgnoreNull(newFightingPet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 分服排名结算
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "emailTest")
    public String sendEmail(String userUid) {
        emailService.sendEmail(userUid,"emailTest","啊合适的功夫还是规范化飞过速度发货速度发货",rewardService.getAward("emailTest"));
        return "success";
    }


    @Autowired
    private RankSettleHandle rankSettleHandle;
    /**
     * 分服排名结算
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "rankSettle")
    public String rankSettle() {

        rankSettleHandle.handle();

        return "success";
    }

}
