package com.haoyou.spring.cloud.alibaba.cultivate.rmb.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.service.CommodityBuyService;
import com.haoyou.spring.cloud.alibaba.cultivate.service.CurrencyUseService;
import com.haoyou.spring.cloud.alibaba.cultivate.service.RMBUseService;
import com.haoyou.spring.cloud.alibaba.cultivate.service.RewardService;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.CyrrencyUseMsg;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.RMBUseMsg;
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
public abstract class RMBUseHandle {


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
        RMBUseService.register(this);
    }

    /**
     * 重写方法，配置标识
     */
    protected abstract void setHandleType();


    /**
     * 消费处理
     * @return
     */
    public abstract MapBody handle(RMBUseMsg rmbUseMsg);

    /**
     * 入口方法
     * @param rmbUseMsg
     * @return
     */
    public MapBody rmbUse(RMBUseMsg rmbUseMsg){
        return handle(rmbUseMsg);
    }




    /**
     * 保存user
     *
     * @param user
     * @return
     */
    protected boolean save(User user) {
        //保存修改
        user.setLastUpdateDate(new Date());
        if(redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.USER, user.getUid()), user)){
            return true;
        }
        return false;
    }


    // ********************************     以下为常量      ************************************

    //月卡
    final static public int MONTHLI_CARD = 1;
    //至尊月卡
    final static public int MONTHLI_CARD_EXTREME = 2;
    //基金
    final static public int FUND = 3;
    //终身饲养员
    final static public int LIFETIME_BREEDER = 4;
    //商品
    final static public int COMMODITY = 5;

    // ********************************     以上为使用类型，以下为返回类型，错误信息     ************************************


    //超出上限
    final static public int LIMIT = 1005;
}
