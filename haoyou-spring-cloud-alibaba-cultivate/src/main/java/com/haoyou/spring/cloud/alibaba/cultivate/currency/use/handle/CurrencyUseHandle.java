package com.haoyou.spring.cloud.alibaba.cultivate.currency.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.service.CommodityBuyService;
import com.haoyou.spring.cloud.alibaba.cultivate.service.CurrencyUseService;
import com.haoyou.spring.cloud.alibaba.cultivate.service.NumericalService;
import com.haoyou.spring.cloud.alibaba.cultivate.service.RewardService;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.CyrrencyUseMsg;
import com.haoyou.spring.cloud.alibaba.service.cultivate.CultivateService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;

@Service
@Data
public abstract class CurrencyUseHandle {


    @Autowired
    protected RedisObjectUtil redisObjectUtil;
    @Autowired
    protected SendMsgUtil sendMsgUtil;
    @Autowired
    protected PetMapper petMapper;

    @Autowired
    protected UserMapper userMapper;
    @Autowired
    protected RewardService rewardService;

    @Autowired
    protected CultivateService cultivateService;
    @Autowired
    protected UserUtil userUtil;

    @Autowired
    protected CommodityBuyService commodityBuyService;

    @Autowired
    private NumericalService numericalService;

    /**
     * 处理标识
     */
    protected Integer handleType;

    /**
     * 设置类型并注册到处理中心
     */
    @PostConstruct
    protected void init(){
        setHandleType();
        CurrencyUseService.register(this);
    }

    /**
     * 重写方法，配置标识
     */
    protected abstract void setHandleType();


    /**
     * 消费处理
     * @return
     */
    public abstract int handle(CyrrencyUseMsg cyrrencyUseMsg);

    /**
     * 入口方法
     * @param cyrrencyUseMsg
     * @return
     */
    public int currencyUse(CyrrencyUseMsg cyrrencyUseMsg){

        Currency currency = cyrrencyUseMsg.getUser().getCurrency();
        Long diamond = currency.getDiamond();

        //消费
        int handle = handle(cyrrencyUseMsg);


        Currency currency1 = cyrrencyUseMsg.getUser().getCurrency();
        Long diamond1 = currency1.getDiamond();
        if(diamond1 < diamond){
            numericalService.numericalAdd(cyrrencyUseMsg.getUser(),"daily_use_diamond",diamond-diamond1);
            numericalService.numericalAdd(cyrrencyUseMsg.getUser(),"use_diamond",diamond-diamond1);
        }


        return handle;

    }

    /**
     * 保存user
     *
     * @param user
     * @return
     */
    protected boolean save(User user) {
        return save(user,null);
    }
    protected boolean save(User user, FightingPet fightingPet) {
        //保存修改
        user.setLastUpdateDate(new Date());
        if(redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.USER, user.getUid()), user)){
            if(fightingPet!=null){
                fightingPet.save();
            }
            return true;
        }
        return false;
    }


    // ********************************     以下为常量      ************************************

    //培养
    final static public int PET_CULTURE = 1;
    //提升培养上限
    final static public int CULTURE_LIMIT = 2;
    //提升技能道具上限
    final static public int PROP_LIMIT = 3;
    //提升宠物栏位
    final static public int PET_LIMIT = 4;
    //购买商品
    final static public int COMMODITY = 5;
    //种植系统等级
    final static public int PLANTING_SYSTEM_LEVEL = 6;
    //种植系统土地等级
    final static public int PLANTING_LAND_LEVEL = 7;

    // ********************************     以上为使用类型，以下为返回类型      ************************************

    //材料不足
    final static public int COIN_LESS = 1001;
    final static public int DIAMOND_LESS = 1002;
    final static public int PROP_LESS = 1003;
    final static public int OTHER_LESS = 1004;

    //超出上限
    final static public int LIMIT = 1005;
}
