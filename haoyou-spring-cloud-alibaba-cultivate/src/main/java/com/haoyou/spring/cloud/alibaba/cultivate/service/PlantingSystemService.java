package com.haoyou.spring.cloud.alibaba.cultivate.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.domain.SkillType;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/22 10:04
 * <p>
 * 种植系统
 */
@Service
public class PlantingSystemService {

//    final static public int PLANTING_TIME = 6 * 60 * 60;
    final static public int PLANTING_TIME = 1 * 60;

    final static public int WATERING_TIME = 30 * 60;

    public static List<String> cropTypes = new ArrayList<>();

    static {
        cropTypes.add("watermelon");
        cropTypes.add("grape");
        cropTypes.add("orange");
        cropTypes.add("banana");
        cropTypes.add("strawberry");
        cropTypes.add("potato");
        cropTypes.add("radish");
        cropTypes.add("corn");
    }

    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private SendMsgUtil sendMsgUtil;
    @Autowired
    private RewardService rewardService;
    @Autowired
    private UserUtil userUtil;
    @Autowired
    private CurrencyUseService currencyUseService;
    @Autowired
    private NumericalService numericalService;


    /**
     * 种植信息处理
     *
     * @param req
     * @return
     */
    public MapBody handle(MyRequest req) {
        User user = req.getUser();

        Map<String, Object> msgMap = userUtil.getMsgMap(req);
        int type = (Integer) msgMap.get("type");

        MapBody mapBody = MapBody.beErr();

        switch (type) {
            //种植
            case 1:
                mapBody = planting(user, msgMap);
                break;
            //设置种地宠物
            case 2:
                mapBody = setPet(user, msgMap);
                break;
            //收货奖励
            case 3:
                mapBody = getCrop(user, msgMap);
                break;
            //偷取或者浇水
            case 4:
                mapBody = stolenOrWatering(user, msgMap);
                break;
        }

        mapBody.put("type", type);

        return mapBody;
    }

    /**
     * 偷取或者浇水
     *
     * @param user
     * @param msgMap
     * @return
     */
    private MapBody stolenOrWatering(User user, Map<String, Object> msgMap) {


        String friendUid = (String) msgMap.get("friendUid");
        int stolenOrWatering = (Integer) msgMap.get("stolenOrWatering");
        String landUid = (String) msgMap.get("landUid");

        if (StrUtil.isEmpty(landUid)) {
            List<Land> lands = userUtil.getLands(friendUid);
            for (Land land : lands) {
                switch (stolenOrWatering) {
                    case 1:
                        stolen(user, land);
                        break;
                    case 2:
                        watering(user, land);
                        break;
                }
            }
        } else {
            //好友土地
            Land land = userUtil.getLand(friendUid, landUid);
            switch (stolenOrWatering) {
                case 1:
                    if (!stolen(user, land)) {
                        return MapBody.beErr();
                    }
                    break;
                case 2:
                    if (!watering(user, land)) {
                        return MapBody.beErr();
                    }
                    break;
            }
        }
        return MapBody.beSuccess();
    }

    /**
     * 给作物浇水
     *
     * @param user
     * @param land
     * @return
     */
    private boolean watering(User user, Land land) {
        //没有种植
        if (land.getSeedUid() == null) {
            return false;
        }
        String key = RedisKeyUtil.getKey(RedisKey.WATERING_LAND, land.getUid(), land.getSeedUid(), user.getUid());
        //是否已经浇水
        Land land1 = redisObjectUtil.get(key, Land.class);
        if (land1 != null) {
            return false;
        }
        //校验时间
        Date plantingTime = land.getPlantingTime();
        DateTime date = DateUtil.date();
        long also = plantingTime.getTime() - date.getTime();
        if (also < WATERING_TIME) {
            return false;
        }
        //浇水使成熟时间减少5分钟
        land.setPlantingTime(DateUtil.offsetMinute(land.getPlantingTime(), -5));
        userUtil.saveLand(land);

        //浇水记录
        redisObjectUtil.save(key, land, -1);
        return true;
    }

    /**
     * 偷取作物
     *
     * @param user
     * @param land
     * @return
     */
    private boolean stolen(User user, Land land) {
        //当天偷取果实个数，限制100个
        UserNumerical userNumerical = user.getUserNumericalMap().get("daily_land_stolen_count");
        if (userNumerical.getValue() >= 100) {
            return false;
        }

        //没有种植
        if (land.getSeedUid() == null) {
            return false;
        }
        String key = RedisKeyUtil.getKey(RedisKey.STOLEN_LAND, land.getUid(), land.getSeedUid(), user.getUid());
        //是否已经偷过
        Land land1 = redisObjectUtil.get(key, Land.class);
        if (land1 != null) {
            return false;
        }

        //校验成熟
        Date plantingTime = land.getPlantingTime();
        DateTime date = DateUtil.date();
        if (plantingTime.getTime() > date.getTime()) {
            return false;
        }

        Integer cropCount = land.getCropCount();

        Integer beingStolen = land.getBeingStolen();


        int stolenCount = cropCount / 10;

        //只能偷到50%
        if (beingStolen + stolenCount >= cropCount / 2) {
            return false;
        }

        land.setBeingStolen(beingStolen + stolenCount);

        //生成果实
        String propKey = RedisKeyUtil.getKey(RedisKey.PROP, "Ingredients");
        Prop prop = redisObjectUtil.get(propKey, Prop.class);
        prop.setProperty1(land.getCropType());
        prop.setProperty2(land.getSeedStar().toString());
        prop.setProperty3(land.getCropL10n());
        prop.setCount(stolenCount);
        //封装奖励
        List<Prop> propsList = new ArrayList<>();
        propsList.add(prop);
        Award award = new Award();
        award.setPropsList(propsList);
        if (rewardService.doAward(user, award)) {
            //增加当天偷取果实个数
            userUtil.saveLand(land);
            numericalService.numericalSet(user, "daily_land_stolen_count", userNumerical.getValue() + stolenCount);
            userUtil.saveUser(user);
            //偷取记录
            redisObjectUtil.save(key, land, -1);
            return true;
        }
        return false;
    }

    /**
     * 收获奖励
     *
     * @param user
     * @param msgMap
     * @return
     */
    private MapBody getCrop(User user, Map<String, Object> msgMap) {
        String landUid = (String) msgMap.get("landUid");
        if (StrUtil.isEmpty(landUid)) {
            List<Land> lands = userUtil.getLands(user.getUid());
            for (Land land : lands) {
                getCrop(user, land);
            }
        } else {
            //土地对象
            Land land = userUtil.getLand(user.getUid(), landUid);
            if (!getCrop(user, land)) {
                return MapBody.beErr();
            }
        }
        return MapBody.beSuccess();

    }

    /**
     * 获取产物
     *
     * @param user
     * @param land
     * @return
     */
    private boolean getCrop(User user, Land land) {
        //没有种植无法收获
        if (land.getSeedUid() == null) {
            return false;
        }
        //校验成熟
        Date plantingTime = land.getPlantingTime();
        DateTime date = DateUtil.date();
        if (plantingTime.getTime() > date.getTime()) {
            return false;
        }
        //生成果实
        String propKey = RedisKeyUtil.getKey(RedisKey.PROP, "Ingredients");
        Prop prop = redisObjectUtil.get(propKey, Prop.class);
        prop.setProperty1(land.getCropType());
        prop.setProperty2(land.getSeedStar().toString());
        prop.setProperty3(land.getCropL10n());
        prop.setCount(land.getCropCount() - land.getBeingStolen());
        //封装奖励
        List<Prop> propsList = new ArrayList<>();
        propsList.add(prop);
        Award award = new Award();
        award.setPropsList(propsList);
        if (rewardService.doAward(user, award)) {
            userUtil.saveUser(user);
        }


        land.setSeedUid(null);
        land.setSeedStar(null);
        land.setSeedType(null);
        land.setCropType(null);
        land.setCropL10n(null);
        land.setCropCount(0);

        land.setPlantingTime(null);
        land.setBeingStolen(0);

        land.setPetUid(null);

        userUtil.saveLand(land);

        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.STOLEN_LAND, land.getUid(), land.getSeedUid()));
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.WATERING_LAND, land.getUid(), land.getSeedUid()));
        return true;
    }


    /**
     * 种植
     *
     * @param user
     * @param msgMap
     * @return
     */
    private MapBody planting(User user, Map<String, Object> msgMap) {

        String propInstenceUid = (String) msgMap.get("propInstenceUid");
        String landUid = (String) msgMap.get("landUid");
        //土地对象
        Land land = userUtil.getLand(user.getUid(), landUid);
        //正在种植无法种植
        if (land.getSeedUid() != null) {
            return MapBody.beErr();
        }


        Prop prop = userUtil.getPropByInstenceUid(user, propInstenceUid);

        if (prop == null || !prop.getName().equals("Seed")) {
            return MapBody.beErr();
        }

        user.deleteProp(prop, 1);
        userUtil.saveUser(user);
        //1果实种类，2星级，3果实中文名
        String seedType = prop.getProperty1();
        String seedStar = prop.getProperty2();
        String cropL10n = prop.getProperty3();
        land.setSeedUid(propInstenceUid);
        land.setSeedStar(Integer.parseInt(seedStar));
        land.setSeedType(seedType);

        //基础数量
        int cropCount = 50;

        String cropType = "";
        //Gold
        if ("gold".equals(seedType)) {
            cropCount = 200;
            int i = RandomUtil.randomInt(8);
            cropType = cropTypes.get(i);

        } else {
            cropType = seedType;
        }

        land.setCropType(cropType);
        land.setCropL10n(cropL10n);

        //成熟时间
        Integer reductionTime = land.getReductionTime();
//        long time = Double.valueOf(PLANTING_TIME * (1 - 0.015 * reductionTime)).longValue();
        int t = 15 * reductionTime;
        //产出数量
        Integer increaseOutput = land.getIncreaseOutput();
//        cropCount = Double.valueOf(cropCount * (1 + 0.025 * increaseOutput)).intValue();
        int c = 25 * increaseOutput;
        //宠物技能影响
        String petUid = land.getPetUid();
        FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, petUid, redisObjectUtil);
        if (fightingPet != null) {
            List<Skill> PlantingSkills = fightingPet.getSkillsByType(SkillType.PLANTING);
            if (PlantingSkills.isEmpty()) {
                t += 20;
                c += 20;
            } else {
                for (Skill skill : PlantingSkills) {
                    List<Resout> resouts = skill.getResouts();
                    for (Resout resout : resouts) {
                        State state = resout.getState();
                        Integer percent = state.getPercent();
                        String infAttr = state.getInfAttr();
                        if ("reduction_time".equals(infAttr)) {
                            t += percent * 10;
                        } else if ("increase_output".equals(infAttr)) {
                            c += percent * 10;
                        }
                    }
                }
            }
        }

        int time = PLANTING_TIME * (1000 - t) / 1000;
        cropCount = cropCount * (1000 + c) / 1000;

        DateTime dateTime = DateUtil.offsetSecond(DateUtil.date(), time);

        //成熟时间以及产量
        land.setPlantingTime(dateTime.toJdkDate());
        land.setCropCount(cropCount);
        //保存
        userUtil.saveLand(land);


        numericalService.numericalAdd(user,"daily_planting",1L);
        numericalService.numericalAdd(user,"planting",1L);

        return MapBody.beSuccess();
    }

    /**
     * 设置种地宠物
     *
     * @param user
     * @param msgMap
     * @return
     */
    private MapBody setPet(User user, Map<String, Object> msgMap) {

        String petUid = (String) msgMap.get("petUid");
        String landUid = (String) msgMap.get("landUid");
        //土地对象
        Land land = userUtil.getLand(user.getUid(), landUid);
        //正在种植无法更换宠物
        if (land.getSeedUid() != null) {
            return MapBody.beErr();
        }


        //校验宠物是否已经在种地
        List<Land> lands = userUtil.getLands(user.getUid());

        for (Land land1 : lands) {
            String petUid1 = land1.getPetUid();
            if (petUid.equals(petUid1)) {
                if(land1.getSeedUid() != null){
                    return MapBody.beErr();
                }else{
                    land1.setPetUid(null);
                    //保存
                    userUtil.saveLand(land1);
                }
            }
        }

        if (petUid != null) {
            //添加种地宠物
            FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, petUid, redisObjectUtil);
            if (fightingPet == null) {
                return MapBody.beErr();
            }
        }

        land.setPetUid(petUid);
        //保存
        userUtil.saveLand(land);

        return MapBody.beSuccess();
    }
}

