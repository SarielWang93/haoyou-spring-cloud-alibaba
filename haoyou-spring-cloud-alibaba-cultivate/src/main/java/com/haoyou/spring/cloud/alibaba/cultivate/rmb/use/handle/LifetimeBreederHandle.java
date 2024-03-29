package com.haoyou.spring.cloud.alibaba.cultivate.rmb.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Fund;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.RMBUseMsg;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/8/5 16:35
 * 购买终身饲养员
 */
@Service
public class LifetimeBreederHandle extends RMBUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = LIFETIME_BREEDER;
    }

    @Override
    public MapBody handle(RMBUseMsg rmbUseMsg) {

        User user = rmbUseMsg.getUser();

        user.getUserData().setLifetimeBreederDate(new Date());


        //发放每日奖励
        Award award = rewardService.getAward(RedisKey.LIFETIME_BREEDER);

        rewardService.upAward(user.getUid(),award,RedisKey.LIFETIME_BREEDER);



        this.save(user);

        MapBody mapBody = new MapBody();
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }
}
