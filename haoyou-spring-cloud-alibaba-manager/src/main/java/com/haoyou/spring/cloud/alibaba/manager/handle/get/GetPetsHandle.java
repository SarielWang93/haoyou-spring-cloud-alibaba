package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取宠物列表数据
 */
@Service
public class GetPetsHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetPetsHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_PETS;
    }

    @Override
    public BaseMessage handle(MyRequest req) {

        User user = req.getUser();


        Map<String, Object> msgMap = this.getMsgMap(req);

        Integer hasIswork = (Integer) msgMap.get("hasIswork");

        MapBody mapBody = new MapBody<>();
        mapBody.setState(ResponseMsg.MSG_SUCCESS);

        String userUidKey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, user.getUid());

        String key = RedisKeyUtil.getlkKey(userUidKey);
        HashMap<String, FightingPet> fightingPets = redisObjectUtil.getlkMap(key, FightingPet.class);

        List<Map> pets = new ArrayList<>();

        for (FightingPet fightingPet : fightingPets.values()) {

            Pet pet = fightingPet.getPet();

            if (hasIswork != null && hasIswork.equals(1) && (pet.getIswork() == null || pet.getIswork() == 0)) {
                continue;
            }

            Map<String, Object> petMap = new HashMap<>();

            petMap.put("petUid", pet.getUid());
            petMap.put("userUid", pet.getUserUid());
            petMap.put("nickName", pet.getNickName());
            petMap.put("iswork", pet.getIswork());
            petMap.put("starClass", pet.getStarClass());
            petMap.put("level", pet.getLevel());
            petMap.put("type", pet.getType());
            petMap.put("race", pet.getRace());
            petMap.put("typeId", pet.getTypeId());


            petMap.put("mb_max_hp", fightingPet.getMb_max_hp());
            petMap.put("mb_atn", fightingPet.getMb_atn());
            petMap.put("mb_def", fightingPet.getMb_def());
            petMap.put("mb_spd", fightingPet.getMb_spd());
            petMap.put("mb_luk", fightingPet.getMb_luk());

            pets.add(petMap);
//            sendMsgUtil.sendMsgOneNoReturn(user.getUid(),req.getId(),mapBody);
        }
        mapBody.put("pets", pets);

        Integer petMax = user.getCurrency().getPetMax();
        mapBody.put("petMaxUpDiamond", petMax / 10 * 10 * petMax + (petMax % 10) * 10);


        String helpPetKey = RedisKeyUtil.getlkKey(RedisKey.HELP_PET, user.getUid(), RedisKey.HELP);
        HashMap<String, String> stringStringHashMap = redisObjectUtil.getlkMap(helpPetKey, String.class);
        if (stringStringHashMap.size() == 1) {
            for (String value : stringStringHashMap.values()) {
                String[] split = value.split(":");
                int iswork = Integer.parseInt(split[1]);

                User friendUser = userUtil.getUserByUid(split[0]);
                String helpPetUid = friendUser.getUserData().getHelpPetUid();
                FightingPet fightingPet = FightingPet.getByUserAndPetUid(friendUser, helpPetUid, redisObjectUtil);

                Map<String, Object> petMap = new HashMap<>();

                Pet pet = fightingPet.getPet();

                petMap.put("petUid", pet.getUid());
                petMap.put("userUid", pet.getUserUid());
                petMap.put("nickName", pet.getNickName());
                petMap.put("iswork", iswork);
                petMap.put("starClass", pet.getStarClass());
                petMap.put("level", pet.getLevel());
                petMap.put("type", pet.getType());
                petMap.put("race", pet.getRace());
                petMap.put("typeId", pet.getTypeId());


                petMap.put("mb_max_hp", fightingPet.getMb_max_hp());
                petMap.put("mb_atn", fightingPet.getMb_atn());
                petMap.put("mb_def", fightingPet.getMb_def());
                petMap.put("mb_spd", fightingPet.getMb_spd());
                petMap.put("mb_luk", fightingPet.getMb_luk());

                mapBody.put("helpPet", petMap);
            }
        }


        return mapBody;
    }
}
