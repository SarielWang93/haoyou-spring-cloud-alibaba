package com.haoyou.spring.cloud.alibaba.cultivate.rmb.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Commodity;
import com.haoyou.spring.cloud.alibaba.commons.entity.Fund;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.RMBUseMsg;
import org.springframework.stereotype.Service;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/8/5 16:35
 * 购买月卡
 */
@Service
public class CommodityRMBHandle extends RMBUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = COMMODITY;
    }

    @Override
    public MapBody handle(RMBUseMsg rmbUseMsg) {

        MapBody mapBody = new MapBody();

        User user = rmbUseMsg.getUser();

        Commodity commodity = commodityBuyService.getCommodity(rmbUseMsg.getStoreName(), rmbUseMsg.getName());

        if(!commodityBuyService.commodityBuy(user,commodity)){
            mapBody.setState(ResponseMsg.MSG_ERR);
        }else{
            this.save(user);
            mapBody.setState(ResponseMsg.MSG_SUCCESS);
        }

        return mapBody;
    }
}
