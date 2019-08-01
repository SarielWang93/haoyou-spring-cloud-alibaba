package com.haoyou.spring.cloud.alibaba.cultivate.currency.use.handle;

import cn.hutool.core.util.RandomUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.CyrrencyUseMsg;
import org.springframework.stereotype.Service;

import java.util.Date;
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
public class CultureLimitHandle extends CurrencyUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = CULTURE_LIMIT;
    }

    @Override
    public int handle(CyrrencyUseMsg cyrrencyUseMsg) {

        User user = cyrrencyUseMsg.getUser();

        String petUid = cyrrencyUseMsg.getPetUid();


        //获取目标宠物
        FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, petUid, redisObjectUtil);

        Pet pet = fightingPet.getPet();

        //培养液道具
        List<Prop> props = user.propList();

        Prop cultureMedium = null;

        for (Prop prop : props) {
            if ("CultureMedium".equals(prop.getName())) {
                cultureMedium = prop;
            }
        }

        //培养上限

        Integer cultureLimit = pet.getCultureLimit();

        if(cultureLimit >=100 ){
            return LIMIT;
        }

        //所需道具
        int propCount = cultureLimit / 10 * 1 * cultureLimit + (cultureLimit % 10)+10;
        int nprop = cultureMedium.getCount() - propCount;
        if(nprop <0){
            return PROP_LESS;
        }else{
            user.deleteProp(cultureMedium,propCount);
        }

        //所需钻石
        int diamondCount = cultureLimit / 10 * 10 * cultureLimit + (cultureLimit % 10)*10+100;
        int nDiamond = user.getCurrency().getDiamond() - diamondCount;
        if (nDiamond < 0) {
            return DIAMOND_LESS;
        } else {
            user.getCurrency().setDiamond(nDiamond);
        }

        pet.setCultureLimit(cultureLimit+1);


        //保存修改
        if(!save(user,fightingPet)){
            return ResponseMsg.MSG_ERR;
        }

        return ResponseMsg.MSG_SUCCESS;
    }
}
