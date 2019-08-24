package com.haoyou.spring.cloud.alibaba.cultivate.currency.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.CyrrencyUseMsg;
import org.springframework.stereotype.Service;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/22 10:11
 *
 * 宠物容量提升
 *
 */
@Service
public class PetLimitHandle extends CurrencyUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = PET_LIMIT;
    }

    @Override
    public int handle(CyrrencyUseMsg cyrrencyUseMsg) {

        User user = cyrrencyUseMsg.getUser();

        Currency currency = user.getCurrency();
        Integer petMax = currency.getPetMax();


        //所需钻石
        int diamondCount = petMax / 10 * 10 * petMax + (petMax % 10)*10;
        long nDiamond = currency.getDiamond() - diamondCount;
        if (nDiamond < 0) {
            return DIAMOND_LESS;
        } else {
            currency.setDiamond(nDiamond);
        }

        currency.setPetMax(++petMax);


        //保存修改
        if(!save(user)){
            return ResponseMsg.MSG_ERR;
        }

        MapBody mapBody = new MapBody<>();
        mapBody.put("petMaxUpDiamond",petMax / 10 * 10 * petMax + (petMax % 10)*10);
        mapBody.put("diamond",user.getCurrency().getDiamond());
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        sendMsgUtil.sendMsgOneNoReturn(user.getUid(), SendType.GET_PETS,mapBody);

        return ResponseMsg.MSG_SUCCESS;
    }
}
