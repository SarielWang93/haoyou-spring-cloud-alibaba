package com.haoyou.spring.cloud.alibaba.cultivate.currency.use.handle;

import cn.hutool.core.util.RandomUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.Commodity;
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
public class CommodityDiamondHandle extends CurrencyUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = PET_CULTURE;
    }

    @Override
    public int handle(CyrrencyUseMsg cyrrencyUseMsg) {

        User user = cyrrencyUseMsg.getUser();

        Commodity commodity = commodityBuyService.getCommodity(cyrrencyUseMsg.getStoreName(),cyrrencyUseMsg.getCommodityName());

        //购买上限
        if(commodity.getRefreshTimes() != -1){
            String numericalName = String.format("commodity_%s", commodity.getName());
            Long count = user.getUserNumericalMap().get(numericalName).getValue();
            if(count >= commodity.getRefreshTimes()){
                return LIMIT;
            }
        }
        //花费类型
        String spendType = commodity.getSpendType();
        //价格
        Integer price = commodity.getPrice();
        if("diamond".equals(spendType)){
            //花费钻石
            int nDiamond = user.getCurrency().getDiamond()-price;
            if (nDiamond < 0) {
                return DIAMOND_LESS;
            } else {
                user.getCurrency().setDiamond(nDiamond);
            }
        }else if("coin".equals(spendType)){
            //花费金币
            int nCoin = user.getCurrency().getCoin() - price;
            if (nCoin < 0) {
                return COIN_LESS;
            } else {
                user.getCurrency().setCoin(nCoin);
            }
        }else if("rmb".equals(spendType)){
            return OTHER_LESS;
        }else{
            //花费道具
            List<Prop> props = user.propList();
            Prop p = null;
            for (Prop prop : props) {
                if (spendType.equals(prop.getName())) {
                    p = prop;
                }
            }
            if(p == null){
                return PROP_LESS;
            }
            int propCountNow = p.getCount();

            //所需道具
            int nprop = propCountNow - price;
            if (nprop < 0) {
                return PROP_LESS;
            } else {
                user.deleteProp(p, price);
            }

        }

        //获得商品
        commodityBuyService.commodityBuy(user,commodity);

        //保存
        if (!save(user)) {
            return ResponseMsg.MSG_ERR;
        }




        return ResponseMsg.MSG_SUCCESS;
    }
}
