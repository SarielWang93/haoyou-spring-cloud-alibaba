package com.haoyou.spring.cloud.alibaba.service.cultivate;

import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;

/**
 * @Author: wanghui
 * @Date: 2019/5/13 11:36
 * @Version 1.0
 * 养成系统服务
 */
public interface CultivateService {

    /**
     * 技能配置系统
     */
    boolean skillConfig(MyRequest req);

    /**
     * 道具使用
     * @param req
     * @return
     */
    BaseMessage propUse(MyRequest req);

    /**
     * 宠物生成（临时）
     */
    boolean petGeneration(MyRequest req);


    /**
     * 宠物升级
     */
    BaseMessage petUpLev(MyRequest req);

    /**
     * 奖励获取
     */
    boolean rewards(User user,String type);

    /**
     * 奖励领取
     * @param req
     * @return
     */
    BaseMessage receiveAward(MyRequest req);


    /**
     * 修改出战
     * @param req
     * @return
     */
    boolean updateIsWork(MyRequest req);

    /**
     * 使用货币
     * @param req
     * @return
     */
    BaseMessage currencyUse (MyRequest req);


    /**
     *  数值系统
     * @param user
     * @param numericalName
     * @param value
     * @return
     */
    boolean numericalAdd (User user,String numericalName,long value);


    /**
     * 计时执行
     */
    void doSettlement();



    /**
     * 邮件操作
     *
     * @param req
     * @return
     */
    BaseMessage emailDo(MyRequest req);

    /**
     * 人民币消费
     * @param req
     * @return
     */
    BaseMessage rmbUse(MyRequest req);

}
