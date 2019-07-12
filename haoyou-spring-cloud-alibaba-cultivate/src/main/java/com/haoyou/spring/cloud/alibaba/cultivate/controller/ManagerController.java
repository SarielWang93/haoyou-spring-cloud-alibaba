package com.haoyou.spring.cloud.alibaba.cultivate.controller;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.PetType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.msg.PetUpLevMsg;
import com.haoyou.spring.cloud.alibaba.cultivate.msg.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.cultivate.msg.UpdateIsworkMsg;
import com.haoyou.spring.cloud.alibaba.cultivate.reward.Award;
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

@RestController
public class ManagerController {
    private final static Logger logger = LoggerFactory.getLogger(ManagerController.class);


    @Autowired
    protected RedisObjectUtil redisObjectUtil;
    @Autowired
    protected CultivateService cultivateService;
    @Autowired
    protected SendMsgUtil sendMsgUtil;

    /**
     * 设置PVE胜利奖励
     *
     * @param award
     * @return
     */
    @CrossOrigin
    @PostMapping(value = "setPVEAward")
    public String setPVEAward(@RequestBody Award award) {

        redisObjectUtil.save("award:pve", award);

        return "success";

    }

    /**
     * 获取PVE胜利奖励
     *
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "getPVEAward")
    public String getPVEAward() {

        Award award = redisObjectUtil.get("award:pve", Award.class);

        try {
            return MapperUtils.obj2json(award);
        } catch (Exception e) {
            e.printStackTrace();
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
    public String getPVEReward(String userUid) {

        User user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, userUid), User.class);

        if (cultivateService.rewards(user, 1)) {
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
        MapBody mapBody = cultivateService.petUpLev(myRequest);
        try {
            return MapperUtils.obj2jsonIgnoreNull(mapBody);
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
    public String useProps(String userUid, String petUid, String propInstenceUid, int propCount,int type) {

        PropUseMsg propUseMsg = new PropUseMsg();
        propUseMsg.setPetUid(petUid);
        propUseMsg.setPropInstenceUid(propInstenceUid);
        propUseMsg.setPropCount(propCount);
        propUseMsg.setType(type);

        User user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, userUid), User.class);


        MyRequest myRequest = new MyRequest();
        myRequest.setMsg(sendMsgUtil.serialize(propUseMsg));
        myRequest.setUser(user);

        MapBody mapBody = cultivateService.propUse(myRequest);

        try {
            return MapperUtils.obj2jsonIgnoreNull(mapBody);
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


}
