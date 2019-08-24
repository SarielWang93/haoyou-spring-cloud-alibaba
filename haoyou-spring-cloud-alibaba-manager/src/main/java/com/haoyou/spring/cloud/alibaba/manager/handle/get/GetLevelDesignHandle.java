package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.collection.CollUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.pojo.bean.Badge;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 获取大厅数据
 */
@Service
public class GetLevelDesignHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetLevelDesignHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_LEVEL_DESIGN;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        User user = req.getUser();


        Map<String, Object> msgMap = this.getMsgMap(req);

        Integer type = Integer.valueOf((String)msgMap.get("type")) ;

        MapBody mapBody = MapBody.beErr();

        switch (type) {
            case 1:
                mapBody = getChaptersMsg();
                break;
            case 2:
                mapBody = getLevelDesignBaseMsg(user, msgMap);
                break;
            case 3:
                mapBody = getLevelDesignMsg(msgMap);
                break;
        }
        mapBody.put("type",type);
        return mapBody;
    }

    private MapBody getChaptersMsg() {
        MapBody mapBody = MapBody.beSuccess();
        //获取章节信息，并排序
        String chapterlkKey = RedisKeyUtil.getlkKey(RedisKey.CHAPTER);
        HashMap<String, Chapter> stringChapterHashMap = redisObjectUtil.getlkMap(chapterlkKey, Chapter.class);
        TreeMap<Integer, Chapter> chapterTreeMap = new TreeMap<>();
        for (Chapter chapter : stringChapterHashMap.values()) {
            chapterTreeMap.put(chapter.getIdNum(), chapter);
        }
        mapBody.put("chapters", CollUtil.newArrayList(chapterTreeMap.values()));

        return mapBody;
    }

    private MapBody getLevelDesignMsg(Map<String, Object> msgMap) {
        MapBody mapBody = MapBody.beSuccess();

        //传入章节名称
        String chapterName = (String) msgMap.get("chapterName");

        //传入章节名称
        Integer idNum = (Integer)msgMap.get("idNum") ;

        String levelDesignKey = RedisKeyUtil.getKey(RedisKey.LEVEL_DESIGN, chapterName,idNum.toString());
        LevelDesign levelDesign = redisObjectUtil.get(levelDesignKey, LevelDesign.class);


        //不同难度奖励
        mapBody.put("ordinaryFirstAward", userUtil.getAward(levelDesign.getOrdinaryFirstAward()));
        mapBody.put("ordinaryAward", userUtil.getAward(levelDesign.getOrdinaryAward()));

        mapBody.put("difficultyFirstAward", userUtil.getAward(levelDesign.getDifficultyFirstAward()));
        mapBody.put("difficultyAward", userUtil.getAward(levelDesign.getDifficultyAward()));

        mapBody.put("crazyFirstAward", userUtil.getAward(levelDesign.getCrazyFirstAward()));
        mapBody.put("crazyAward", userUtil.getAward(levelDesign.getCrazyAward()));

        //怪物信息
        PetType petType1 = getPetType(levelDesign.getPetType1());
        mapBody.put("pet1Id", petType1.getId());
        mapBody.put("pet1L10n", petType1.getL10n());
        mapBody.put("pet1Name", petType1.getName());
        mapBody.put("pet1StarClass", petType1.getStarClass());
        mapBody.put("pet1Level", levelDesign.getPetLevel1());

        PetType petType2 = getPetType(levelDesign.getPetType2());
        mapBody.put("pet2Id", petType2.getId());
        mapBody.put("pet2L10n", petType2.getL10n());
        mapBody.put("pet2Name", petType2.getName());
        mapBody.put("pet2StarClass", petType2.getStarClass());
        mapBody.put("pet2Level", levelDesign.getPetLevel2());


        PetType petType3 = getPetType(levelDesign.getPetType3());
        mapBody.put("pet3Id", petType3.getId());
        mapBody.put("pet3L10n", petType3.getL10n());
        mapBody.put("pet3Name", petType3.getName());
        mapBody.put("pet3StarClass", petType3.getStarClass());
        mapBody.put("pet3Level", levelDesign.getPetLevel3());

        return mapBody;
    }
    /**
     * 获取基础信息
     *
     * @param user
     * @param msgMap
     * @return
     */
    private MapBody getLevelDesignBaseMsg(User user, Map<String, Object> msgMap) {

        MapBody mapBody = MapBody.beSuccess();
        //获取徽章，并根据关卡分类
        List<Badge> badges = userUtil.getBadges(user);
        Map<String, List<Badge>> badgeMap = new HashMap<>();
        TreeMap<Date, String> badgeTreeMap = new TreeMap<>();
        for (Badge badge : badges) {
            String levelDesignKey = RedisKeyUtil.getKey(RedisKey.LEVEL_DESIGN, badge.getChapterName(), badge.getIdNum().toString());
            if (badgeMap.containsKey(levelDesignKey)) {
                badgeMap.get(levelDesignKey).add(badge);
            } else {
                List<Badge> badgeList = new ArrayList<>();
                badgeList.add(badge);
                badgeMap.put(levelDesignKey, badgeList);
            }
            if (badge.getDifficult() == 0) {
                badgeTreeMap.put(badge.getCreatTime(), levelDesignKey);
            }
        }
        //传入章节名称
        String chapterName = (String) msgMap.get("chapterName");
        if(chapterName == null){
            return MapBody.beErr();
        }
        String chapterKey = RedisKeyUtil.getKey(RedisKey.CHAPTER, chapterName);
        Chapter chapter = redisObjectUtil.get(chapterKey, Chapter.class);
        if(chapter == null){
            return MapBody.beErr();
        }
        //获取关卡信息

        List<Map> levelDesignsMsg = new ArrayList<>();
        String levelDesignlkKey = RedisKeyUtil.getlkKey(RedisKey.LEVEL_DESIGN, chapter.getName());
        HashMap<String, LevelDesign> stringLevelDesignHashMap = redisObjectUtil.getlkMap(levelDesignlkKey, LevelDesign.class);
        TreeMap<Integer, LevelDesign> levelDesignTreeMap = new TreeMap<>();
        for (LevelDesign levelDesign : stringLevelDesignHashMap.values()) {
            levelDesignTreeMap.put(levelDesign.getIdNum(), levelDesign);
        }
        for (LevelDesign levelDesign : levelDesignTreeMap.values()) {
            Map<String, Object> levelDesignMsg = new HashMap<>();

            //基础信息
            levelDesignMsg.put("idNum", levelDesign.getIdNum());
            levelDesignMsg.put("name", levelDesign.getName());
            levelDesignMsg.put("l10n", levelDesign.getL10n());
            levelDesignMsg.put("description", levelDesign.getDescription());

            //徽章首胜获得信息
            String levelDesignKey = RedisKeyUtil.getKey(RedisKey.LEVEL_DESIGN, levelDesign.getChapterName(), levelDesign.getIdNum().toString());
            List<Badge> levelDesignBadges = badgeMap.get(levelDesignKey);
            if (levelDesignBadges != null) {
                for (Badge badge : levelDesignBadges) {
                    int difficult = badge.getDifficult();
                    switch (difficult) {
                        case 0:
                            levelDesignMsg.put("ordinaryHasBadge", true);
                            break;
                        case 1:
                            levelDesignMsg.put("difficultyHasBadge", true);
                            break;
                        case 2:
                            levelDesignMsg.put("crazyHasBadge", true);
                            break;
                    }
                }
            }


            levelDesignsMsg.add(levelDesignMsg);
        }

        mapBody.put("levelDesigns", levelDesignsMsg);


        return mapBody;
    }


    private PetType getPetType(String uid) {
        return redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PET_TYPE, uid), PetType.class);
    }
}
