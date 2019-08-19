package com.haoyou.spring.cloud.alibaba.manager.handle.get;


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
        List<Map> chaptersMsg = new ArrayList<>();

        //获取章节信息，并排序
        String chapterlkKey = RedisKeyUtil.getlkKey(RedisKey.CHAPTER);
        HashMap<String, Chapter> stringChapterHashMap = redisObjectUtil.getlkMap(chapterlkKey, Chapter.class);
        TreeMap<Integer, Chapter> chapterTreeMap = new TreeMap<>();
        for (Chapter chapter : stringChapterHashMap.values()) {
            chapterTreeMap.put(chapter.getIdNum(), chapter);
        }

        //获取关卡信息
        for (Chapter chapter : chapterTreeMap.values()) {
            Map<String, Object> chapterMsg = new HashMap<>();
            chapterMsg.put("chapter", chapter);

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

                //不同难度奖励
                levelDesignMsg.put("ordinaryFirstAward", userUtil.getAward(levelDesign.getOrdinaryFirstAward()));
                levelDesignMsg.put("ordinaryAward", userUtil.getAward(levelDesign.getOrdinaryAward()));

                levelDesignMsg.put("difficultyFirstAward", userUtil.getAward(levelDesign.getDifficultyFirstAward()));
                levelDesignMsg.put("difficultyAward", userUtil.getAward(levelDesign.getDifficultyAward()));

                levelDesignMsg.put("crazyFirstAward", userUtil.getAward(levelDesign.getCrazyFirstAward()));
                levelDesignMsg.put("crazyAward", userUtil.getAward(levelDesign.getCrazyAward()));

                //怪物信息
                PetType petType1 = getPetType(levelDesign.getPetType1());
                levelDesignMsg.put("pet1L10n", petType1.getL10n());
                levelDesignMsg.put("pet1Name", petType1.getName());
                levelDesignMsg.put("pet1StarClass", petType1.getStarClass());
                levelDesignMsg.put("pet1Level", levelDesign.getPetLevel1());

                PetType petType2 = getPetType(levelDesign.getPetType2());
                levelDesignMsg.put("pet2L10n", petType2.getL10n());
                levelDesignMsg.put("pet2Name", petType2.getName());
                levelDesignMsg.put("pet2StarClass", petType2.getStarClass());
                levelDesignMsg.put("pet2Level", levelDesign.getPetLevel2());


                PetType petType3 = getPetType(levelDesign.getPetType3());
                levelDesignMsg.put("pet3L10n", petType3.getL10n());
                levelDesignMsg.put("pet3Name", petType3.getName());
                levelDesignMsg.put("pet3StarClass", petType3.getStarClass());
                levelDesignMsg.put("pet3Level", levelDesign.getPetLevel3());

                //徽章首胜获得信息
                String levelDesignKey = RedisKeyUtil.getKey(RedisKey.LEVEL_DESIGN, levelDesign.getChapterName(), levelDesign.getIdNum().toString());
                List<Badge> levelDesignBadges = badgeMap.get(levelDesignKey);
                if(levelDesignBadges!=null){
                    for (Badge badge : levelDesignBadges) {
                        int difficult = badge.getDifficult();
                        switch (difficult){
                            case 0:
                                levelDesignMsg.put("ordinaryHasBadge",true);
                                ((Award)levelDesignMsg.get("ordinaryFirstAward")).setUsed(true);
                                break;
                            case 1:
                                levelDesignMsg.put("difficultyHasBadge",true);
                                ((Award)levelDesignMsg.get("difficultyFirstAward")).setUsed(true);
                                break;
                            case 2:
                                levelDesignMsg.put("crazyHasBadge",true);
                                ((Award)levelDesignMsg.get("crazyFirstAward")).setUsed(true);
                                break;
                        }
                    }
                }


                levelDesignsMsg.add(levelDesignMsg);
            }

            chapterMsg.put("levelDesigns", levelDesignsMsg);
            chaptersMsg.add(chapterMsg);
        }

        mapBody.put("chapters", chaptersMsg);


        return user.notTooLong();
    }


    private PetType getPetType(String uid) {
        return redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PET_TYPE, uid), PetType.class);
    }
}
