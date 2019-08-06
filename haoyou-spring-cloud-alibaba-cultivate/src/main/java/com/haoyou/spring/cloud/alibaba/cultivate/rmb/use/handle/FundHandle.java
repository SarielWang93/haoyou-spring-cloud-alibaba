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
 * 购买月卡
 */
@Service
public class FundHandle extends RMBUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = FUND;
    }

    @Override
    public MapBody handle(RMBUseMsg rmbUseMsg) {

        User user = rmbUseMsg.getUser();
        String key = RedisKeyUtil.getKey(RedisKey.FUNDS,rmbUseMsg.getName());
        Fund fund = redisObjectUtil.get(key, Fund.class);

        userUtil.addFund(user,fund);

        //发放每日奖励
        Award award = rewardService.getAward(fund.getAwardType());
        rewardService.upAward(user.getUid(),award,key);



        MapBody mapBody = new MapBody();
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }
}
