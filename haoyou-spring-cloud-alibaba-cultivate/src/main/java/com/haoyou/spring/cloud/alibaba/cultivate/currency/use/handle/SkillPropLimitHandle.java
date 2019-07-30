package com.haoyou.spring.cloud.alibaba.cultivate.currency.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.CyrrencyUseMsg;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/22 10:11
 *
 * 宠物培养上限增加
 *
 */
@Service
public class SkillPropLimitHandle extends CurrencyUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = PROP_LIMIT;
    }

    @Override
    public int handle(CyrrencyUseMsg cyrrencyUseMsg) {

        User user = cyrrencyUseMsg.getUser();


        Integer propMax = user.getCurrency().getPropMax();


        //所需钻石
        int diamondCount = propMax / 10 * 10 * propMax + (propMax % 10)*10;
        int nDiamond = user.getCurrency().getDiamond() - diamondCount;
        if (nDiamond < 0) {
            return DIAMOND_LESS;
        } else {
            user.getCurrency().setDiamond(nDiamond);
        }

        user.getCurrency().setPropMax(propMax+1);


        //保存修改
        if(!save(user)){
            return ResponseMsg.MSG_ERR;
        }

        MapBody mapBody = new MapBody<>();
        mapBody.put("propMaxUpDiamond",propMax / 10 * 10 * propMax + (propMax % 10)*10);
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        sendMsgUtil.sendMsgOneNoReturn(user.getUid(), SendType.GET_PROPS,mapBody);

        return ResponseMsg.MSG_SUCCESS;
    }
}
