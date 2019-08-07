package com.haoyou.spring.cloud.alibaba.cultivate.currency.use.handle;

import cn.hutool.core.util.RandomUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.CyrrencyUseMsg;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/22 10:11
 * <p>
 * 宠物培养一次
 */
@Service
public class CultureHandle extends CurrencyUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = PET_CULTURE;
    }

    @Override
    public int handle(CyrrencyUseMsg cyrrencyUseMsg) {

        User user = cyrrencyUseMsg.getUser();

        String petUid = cyrrencyUseMsg.getPetUid();

        boolean diamond = cyrrencyUseMsg.isDiamond();

        //获取目标宠物
        FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, petUid, redisObjectUtil);

        Pet pet = fightingPet.getPet();

        //培养上限
        Integer level = pet.getLevel();
        if (level > 100) {
            level = 100;
        }
        Integer cultureLimit = pet.getCultureLimit();

        cultureLimit += level;

        //当前培养等级
        Integer culture = pet.getCulture();

        //培养液道具
        List<Prop> props = user.propList();

        Prop cultureMedium = null;

        for (Prop prop : props) {
            if ("CultureMedium".equals(prop.getName())) {
                cultureMedium = prop;
            }
        }

        //所需金币
        int coinCount = culture / 10 * 400 * culture + 200 * (culture % 10)+100;
        int nCoin = user.getCurrency().getCoin() - coinCount;
        if (nCoin < 0) {
            return COIN_LESS;
        } else {
            user.getCurrency().setCoin(nCoin);
        }

        //所需钻石
        if (diamond) {
            int nDiamond = user.getCurrency().getDiamond() - 10;
            if (nDiamond < 0) {
                return DIAMOND_LESS;
            } else {
                user.getCurrency().setDiamond(nDiamond);
            }
        }

        int propCountNow = 0;
        if (cultureMedium != null) {
            propCountNow = cultureMedium.getCount();
        }

        //所需道具
        int propCount = culture / 10 * 4 * culture + (culture % 10)+1;
        int nprop = propCountNow - propCount;
        if (nprop < 0) {
            return PROP_LESS;
        } else {
            user.deleteProp(cultureMedium, propCount);
        }

        //上限校验
        if (culture >= cultureLimit) {
            return LIMIT;
        }


        //培养结果，1~2
        double r = 0;
        if (diamond) {
            r = 2;
        } else {
            r = RandomUtil.randomInt(10, 21) / 10d;
        }
        fightingPet.upCulture(r);

        //保存
        if (!save(user, fightingPet)) {
            return ResponseMsg.MSG_ERR;
        }


        //数值系统
        cultivateService.numericalAdd(user,"culture",1L);
        //数值系统
        cultivateService.numericalAdd(user,"daily_pet_culture",1L);

        return ResponseMsg.MSG_SUCCESS;
    }
}
