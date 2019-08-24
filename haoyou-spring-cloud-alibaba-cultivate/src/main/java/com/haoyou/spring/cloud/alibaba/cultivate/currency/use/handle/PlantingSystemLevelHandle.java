package com.haoyou.spring.cloud.alibaba.cultivate.currency.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.entity.UserData;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.CyrrencyUseMsg;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/22 10:11
 *
 * 宠物容量提升
 *
 */
@Service
public class PlantingSystemLevelHandle extends CurrencyUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = PLANTING_SYSTEM_LEVEL;
    }

    @Override
    public int handle(CyrrencyUseMsg cyrrencyUseMsg) {

        User user = cyrrencyUseMsg.getUser();

        UserData userData = user.getUserData();
        //当前等级
        Integer plantingSystemLevel = userData.getPlantingSystemLevel();
        //上限校验
        if (plantingSystemLevel >= 40) {
            return LIMIT;
        }

        //耗材道具
        List<Prop> props = user.propList();
        Prop wood = null;

        for (Prop prop : props) {
            if ("Wood".equals(prop.getName())) {
                wood = prop;
            }
        }

        long propCountNow = 0;
        if (wood != null) {
            propCountNow = wood.getCount();
        }

        //所需道具
        long propCount = 2000;
        int nextLev = plantingSystemLevel+1;
        if(nextLev<4){
            propCount = plantingSystemLevel*5;
        }else if(nextLev<5){
            propCount = 20;
        }else if(nextLev<40){
            propCount = (nextLev-4)*50;
        }

        long nprop = propCountNow - propCount;
        if (nprop < 0) {
            return PROP_LESS;
        } else {
            user.deleteProp(wood, propCount);
        }


        userData.setPlantingSystemLevel(nextLev);

        //保存修改
        if(!save(user)){
            return ResponseMsg.MSG_ERR;
        }
        //增加土地
        if((nextLev-4)%6 == 0){
            userUtil.addLand(user.getUid());
        }

//        MapBody mapBody = new MapBody<>();
//        mapBody.put("petMaxUpDiamond",petMax / 10 * 10 * petMax + (petMax % 10)*10);
//        mapBody.put("diamond",user.getCurrency().getDiamond());
//        mapBody.setState(ResponseMsg.MSG_SUCCESS);
//        sendMsgUtil.sendMsgOneNoReturn(user.getUid(), SendType.GET_PETS,mapBody);

        return ResponseMsg.MSG_SUCCESS;
    }
}
