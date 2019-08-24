package com.haoyou.spring.cloud.alibaba.cultivate.currency.use.handle;

import cn.hutool.core.math.MathUtil;
import cn.hutool.core.util.NumberUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.Land;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.entity.UserData;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.CyrrencyUseMsg;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/22 10:11
 * <p>
 * 宠物容量提升
 */
@Service
public class PlantingLandLevelHandle extends CurrencyUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = PLANTING_LAND_LEVEL;
    }

    @Override
    public int handle(CyrrencyUseMsg cyrrencyUseMsg) {

        User user = cyrrencyUseMsg.getUser();

        String landUid = cyrrencyUseMsg.getLandUid();

        Integer landUpLevType = cyrrencyUseMsg.getLandUpLevType();

        //土地对象
        Land land = userUtil.getLand(user.getUid(), landUid);

        //当前土地等级
        Integer level = land.getLevel();
        Integer nextLevel = level+1;

        Integer reductionTime = land.getReductionTime();
        Integer increaseOutput = land.getIncreaseOutput();

        switch (landUpLevType){
            case 1:
                reductionTime++;
                if(reductionTime > 20){
                    return LIMIT;
                }
                break;
            case 2:
                increaseOutput++;
                if(increaseOutput > 20){
                    return LIMIT;
                }
                break;
        }

        //所需金币
        long coinCount = 100;
        for (int i = 0; i < nextLevel-2; i++) {
            coinCount *= 150/100;
        }
        long nCoin = user.getCurrency().getCoin() - coinCount;
        if (nCoin < 0) {
            return COIN_LESS;
        } else {
            user.getCurrency().setCoin(nCoin);
        }

        //土地等级提升
        land.setLevel(nextLevel);
        land.setReductionTime(reductionTime);
        land.setIncreaseOutput(increaseOutput);


        //保存修改
        //保存修改
        if (!save(user)) {
            return ResponseMsg.MSG_ERR;
        }
        userUtil.saveLand(land);

//        MapBody mapBody = new MapBody<>();
//        mapBody.put("petMaxUpDiamond",petMax / 10 * 10 * petMax + (petMax % 10)*10);
//        mapBody.put("diamond",user.getCurrency().getDiamond());
//        mapBody.setState(ResponseMsg.MSG_SUCCESS);
//        sendMsgUtil.sendMsgOneNoReturn(user.getUid(), SendType.GET_PETS,mapBody);

        return ResponseMsg.MSG_SUCCESS;
    }
}
