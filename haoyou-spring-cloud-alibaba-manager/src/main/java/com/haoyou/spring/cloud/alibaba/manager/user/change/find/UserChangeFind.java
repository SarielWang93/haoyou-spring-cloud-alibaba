package com.haoyou.spring.cloud.alibaba.manager.user.change.find;

import cn.hutool.core.util.ReflectUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.entity.UserData;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/11/15 10:41
 */
@Component
public class UserChangeFind {


    private static List<String> currencyAttributes = new ArrayList<>();
    private static List<String> userDataAttributes = new ArrayList<>();

    static{
        currencyAttributes.add("coin");
        currencyAttributes.add("diamond");
        currencyAttributes.add("vitality");
        currencyAttributes.add("petExp");
        currencyAttributes.add("propMax");
        currencyAttributes.add("petMax");
        currencyAttributes.add("yuan");



        userDataAttributes.add("name");
        userDataAttributes.add("avatar");
        userDataAttributes.add("level");
        userDataAttributes.add("exp");
        userDataAttributes.add("upLevExp");
//        userDataAttributes.add("monthlyCardDate");
//        userDataAttributes.add("monthlyCardExtremeDate");
//        userDataAttributes.add("lifetimeBreederDate");
        userDataAttributes.add("helpPetUid");
        userDataAttributes.add("plantingSystemLevel");

    }


    @Autowired
    protected UserUtil userUtil;
    @Autowired
    protected SendMsgUtil sendMsgUtil;
    @Autowired
    private RedisObjectUtil redisObjectUtil;


    /**
     * 数值改变校验
     */
    public void findChange(User userOld,User userNew){

        boolean hasChange = false;

        User user = new User();


        Currency currency = new Currency();
        user.setCurrency(currency);
        UserData userData = new UserData();
        user.setUserData(userData);
        Map<String, Object> otherMsg = new HashMap<>();
        user.setOtherMsg(otherMsg);



        //货币校验
        boolean hasChange1 = attributeChange(userOld.getCurrency(),userNew.getCurrency(),currency,currencyAttributes,Currency.class);
        //用户信息
        boolean hasChange2 = attributeChange(userOld.getUserData(),userNew.getUserData(),userData,userDataAttributes,UserData.class);
        //另外信息
        boolean hasChange3 = mapAttributeChange(userOld.getOtherMsg(),userNew.getOtherMsg(),otherMsg);

        if(hasChange || hasChange1 || hasChange2 || hasChange3){
            //发送信息
            if(sendMsgUtil.sendMsgOneNoReturn(userOld.getUid(), SendType.LOGIN, user)){
                //刷新用户信息B
                redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.USER_SEND, userNew.getUid()), userNew);
            }
        }
    }

    /**
     * 属性改变校验
     * @param userOld
     * @param userNew
     * @param change
     * @param attributes
     * @param a
     * @return
     */
    private boolean attributeChange(Object userOld,Object userNew,Object change,List<String> attributes,Class<?> a){
        boolean hasChange = false;
        for(String attribute:attributes) {
            Field field = ReflectUtil.getField(a, attribute);

            try {
                Object oldVal = ReflectUtil.getFieldValue(userOld,field);
                Object newVal = ReflectUtil.getFieldValue(userNew,field);
                if ((newVal != null && oldVal != null && !newVal.equals(oldVal)) || (newVal == null && oldVal != null)) {
                    field.set(change, newVal);
                    hasChange = true;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return hasChange;
    }

    /**
     * MapVal改变校验
     * @param userOld
     * @param userNew
     * @param otherMsg
     * @return
     */
    private boolean mapAttributeChange(Map<String, Object> userOld, Map<String, Object> userNew, Map<String, Object> otherMsg){
        boolean hasChange = false;
        for(String key:userOld.keySet()){
            Object oldVal = userOld.get(userOld);
            Object newVal = userNew.get(userNew);
            if ((newVal != null && oldVal != null && !newVal.equals(oldVal)) || (newVal == null && oldVal != null)) {
                otherMsg.put(key, newVal);
                hasChange = true;
            }
        }
        return hasChange;
    }
}
