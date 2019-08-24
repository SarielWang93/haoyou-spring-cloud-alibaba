package com.haoyou.spring.cloud.alibaba.manager.handle.fighting;


import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.FightingType;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.RewardType;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
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
 * 启动AI进行单机战斗
 */
@Service
public class FightingAIHandle extends ManagerHandle {
    private static final long serialVersionUID = 4622211830483903591L;
    private static final Logger logger = LoggerFactory.getLogger(FightingAIHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.FIGHTING_AI;
    }

    @Override
    public BaseMessage handle(MyRequest req) {

        MapBody mapBody = MapBody.beSuccess();

        User user = req.getUser();

        Map<String, Object> msgMap = this.getMsgMap(req);

        //参数获取

        String chapterName = (String) msgMap.get("chapterName");

        Integer idNum = (Integer) msgMap.get("idNum");

        Integer difficult = (Integer) msgMap.get("difficult");

        Integer isWin = (Integer) msgMap.get("isWin");

        if (StrUtil.isEmpty(chapterName) || idNum == null || difficult == null) {
            return mapBody.err();
        }

        //校验体力与上阵宠物

        Integer vitality = user.getCurrency().getVitality();

        int needVitality = (difficult + 1) * 10;

        if (vitality < needVitality) {
            mapBody.put("errMsg", "体力不足，请前往商店购买！");
            return mapBody.err();
        }

        List<FightingPet> fightingPets = FightingPet.getByUser(user, redisObjectUtil);

        int i = 0;
        for (FightingPet fightingPet : fightingPets) {
            Integer iswork = fightingPet.getPet().getIswork();
            if (iswork != null && iswork != 0) {
                i++;
            }
        }
        if (i < 3) {
            mapBody.put("errMsg", "您上阵的宠物不足3只！");
            return mapBody.err();
        }

        if (!fightingService.start(user, chapterName, idNum, difficult,isWin==1)) {
            mapBody.err();
        }

        //减体力
        user = userUtil.getUserByUid(user.getUid());
        user.getCurrency().setVitality(vitality - needVitality);
        userUtil.saveUser(user);

        return mapBody;
    }


}
