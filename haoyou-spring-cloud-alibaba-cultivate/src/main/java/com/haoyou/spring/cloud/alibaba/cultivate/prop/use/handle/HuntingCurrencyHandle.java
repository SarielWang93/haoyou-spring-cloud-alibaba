package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/5 10:48
 * <p>
 * 狩猎货币
 */
@Service
public class HuntingCurrencyHandle extends PeopUseHandle {


    @Override
    protected void setHandleType() {
        this.handleType = "HuntingCurrency";
    }

    @Override
    public MapBody handle(PropUseMsg propUseMsg) {

        User user = propUseMsg.getUser();
        long propCount = propUseMsg.getPropCount();


        //狩猎货币转狩猎贡献
        cultivateService.numericalAdd(user, "hunting_currency", propCount);

        //当前贡献
        Long huntingCurrency = user.getUserNumericalMap().get("hunting_currency").getValue();
        //当前狩猎等级
        Long huntingCurrencyLevel = user.getUserNumericalMap().get("hunting_currency_level").getValue();

        //下一级
        Long nextHuntingCurrencyLevel = huntingCurrencyLevel + 1;
        String nextHuntingCurrencyKey = RedisKeyUtil.getKey(RedisKey.HUNTING_ASSOCIATION, nextHuntingCurrencyLevel.toString());
        HuntingAssociation nextHuntingAssociation = redisObjectUtil.get(nextHuntingCurrencyKey, HuntingAssociation.class);

        Long aim = nextHuntingAssociation.getAim();
        //升级
        if (huntingCurrency >= aim) {
            cultivateService.numericalAdd(user, "hunting_currency_level", 1L);

            String awardType = nextHuntingAssociation.getAwardType();

            Award award = rewardService.getAward(awardType);

            rewardService.doAward(user, award);
        }


        MapBody rt = MapBody.beSuccess();
        return rt;
    }
}
