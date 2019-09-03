package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Friends;
import com.haoyou.spring.cloud.alibaba.commons.entity.Land;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.entity.UserNumerical;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 获取种植系统信息
 */
@Service
public class GetPlantingSystemHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetPlantingSystemHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_PLANTINGSYSTEM;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        User user = req.getUser();

        Map<String, Object> msgMap = this.getMsgMap(req);

        int type = (Integer) msgMap.get("type");

        MapBody mapBody = MapBody.beErr();


        switch (type){
            case 1:
                //种植系统主界面信息
                mapBody = mainPage(user,msgMap);
                break;
            case 2:
                //种植系统好友列表
                mapBody = friandsLand(user,msgMap);
                break;
            case 3:
                //好友土地列表
                mapBody = friandLands(user,msgMap);
                break;
        }

        mapBody.put("type", type);


        return mapBody;
    }

    /**
     *  种植系统主界面信息
     * @param user
     * @param msgMap
     * @return
     */
    private MapBody mainPage(User user, Map<String, Object> msgMap){
        MapBody mapBody = MapBody.beSuccess();


        List<Land> lands = userUtil.getLands(user.getUid());

        List<Map> landsMsg = new ArrayList<>();

        for(Land land:lands){
            Map<String,Object> landMsg = new HashMap<>();

            landMsg.put("land",land);

            String petUid = land.getPetUid();

            if(StrUtil.isNotEmpty(petUid)){
                FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, petUid, redisObjectUtil);
                if(fightingPet != null){
                    landMsg.put("petTypeId",fightingPet.getPet().getTypeId());
                }
            }

            landsMsg.add(landMsg);
        }
        //土地信息
        mapBody.put("landsMsg",landsMsg);

        Integer plantingSystemLevel = user.getUserData().getPlantingSystemLevel();

        //种植系统等级
        mapBody.put("plantingSystemLevel",plantingSystemLevel);
        //当前等级能种植的土地个数
        mapBody.put("plantingSystemLevelLandCount",(((plantingSystemLevel-4)<0?-6:(plantingSystemLevel-4))/6+2));
        if(plantingSystemLevel<40){
            //所需道具
            int propCount = 2000;
            int nextLev = plantingSystemLevel+1;
            if(nextLev<4){
                propCount = plantingSystemLevel*5;
            }else if(nextLev<5){
                propCount = 20;
            }else if(nextLev<40){
                propCount = (nextLev-4)*50;
            }
            //升级所需木头
            mapBody.put("upLevNeedWood",propCount);
            //下一等级能拥有的土地个数
            mapBody.put("nextPlantingSystemLevelLandCount",(((nextLev-4)<0?-6:(nextLev-4))/6+2));
        }

        return mapBody;
    }

    /**
     * 种植系统好友列表
     * @param user
     * @param msgMap
     * @return
     */
    private MapBody friandsLand(User user, Map<String, Object> msgMap){
        MapBody mapBody = MapBody.beSuccess();

        List<String> friendsUid = userUtil.getFriendsUid(user.getUid());

        List<Map> friendsMsg = new ArrayList<>();

        for(String friendUid : friendsUid){
            Map<String,Object> friendMsg = new HashMap<>();

            User friend = userUtil.getUserByUid(friendUid);

            friendMsg.put("friendUid",friendUid);
            friendMsg.put("name",friend.getUserData().getName());
            friendMsg.put("avatar",friend.getUserData().getAvatar());
            friendMsg.put("level",friend.getUserData().getLevel());

            List<Land> lands = userUtil.getLands(friendUid);

            int canStolen = 0;
            int canWatering = 0;

            for(Land land : lands){
                //没有种植
                if (land.getSeedUid() == null) {
                    continue;
                }
                //校验时间
                Date plantingTime = land.getPlantingTime();
                DateTime date = DateUtil.date();
                long also = plantingTime.getTime() - date.getTime();
                //校验成熟
                if(also>0){
                    //能否浇水
                    String wkey = RedisKeyUtil.getKey(RedisKey.WATERING_LAND, land.getUid(), land.getSeedUid(), user.getUid());
                    Land land1 = redisObjectUtil.get(wkey, Land.class);
                    if (land1 == null) {
                        if (also > 30 * 60 * 1000) {
                            canWatering++;
                        }
                    }
                }else{
                    String skey = RedisKeyUtil.getKey(RedisKey.STOLEN_LAND, land.getUid(), land.getSeedUid(), user.getUid());
                    //是否已经偷过
                    Land land2 = redisObjectUtil.get(skey, Land.class);
                    if (land2 == null) {
                        Integer cropCount = land.getCropCount();
                        Integer beingStolen = land.getBeingStolen();
                        int stolenCount = cropCount / 10;
                        //只能偷到50%
                        if (beingStolen + stolenCount < cropCount / 2) {
                            canStolen++;
                        }
                    }
                }
            }

            friendMsg.put("canStolen",canStolen);
            friendMsg.put("canWatering",canWatering);
            friendsMsg.add(friendMsg);
        }

        mapBody.put("friendsMsg",friendsMsg);

        return mapBody;
    }

    /**
     * 好友土地列表
     * @param user
     * @param msgMap
     * @return
     */
    private MapBody friandLands(User user, Map<String, Object> msgMap){
        MapBody mapBody = MapBody.beSuccess();
        String friendUid = (String)msgMap.get("friendUid");
        List<Land> lands = userUtil.getLands(friendUid);

        List<Map> landsMsg = new ArrayList<>();

        for(Land land:lands){
            Map<String,Object> landMsg = new HashMap<>();
            landMsg.put("land",land);
            String petUid = land.getPetUid();
            if(StrUtil.isNotEmpty(petUid)){
                FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, petUid, redisObjectUtil);
                if(fightingPet != null){
                    landMsg.put("petTypeId",fightingPet.getPet().getTypeId());
                }
            }

            //是否浇过水
            String wkey = RedisKeyUtil.getKey(RedisKey.WATERING_LAND, land.getUid(), land.getSeedUid(), user.getUid());
            Land land1 = redisObjectUtil.get(wkey, Land.class);
            if(land1 !=null ){
                landMsg.put("hasWatering",true);
            }
            String skey = RedisKeyUtil.getKey(RedisKey.STOLEN_LAND, land.getUid(), land.getSeedUid(), user.getUid());
            //是否已经偷过
            Land land2 = redisObjectUtil.get(skey, Land.class);
            if(land2 !=null ){
                landMsg.put("hasStolen",true);
            }
            landsMsg.add(landMsg);
        }
        //土地信息
        mapBody.put("landsMsg",landsMsg);

        UserNumerical userNumerical = user.getUserNumericalMap().get("daily_land_stolen_count");

        mapBody.put("dailyLandStolenCount",userNumerical.getValue());

        return mapBody;
    }
}
