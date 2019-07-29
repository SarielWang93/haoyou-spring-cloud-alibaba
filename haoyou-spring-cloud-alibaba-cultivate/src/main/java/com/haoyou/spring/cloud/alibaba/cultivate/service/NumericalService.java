package com.haoyou.spring.cloud.alibaba.cultivate.service;


import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.entity.UserNumerical;
import com.haoyou.spring.cloud.alibaba.cultivate.numerical.NumericalCheck;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * @Author: wanghui
 * @Date: 2019/5/23 15.52
 * @Version 1.0
 * 数值系统
 */
@Service
public class NumericalService {

    private static List<NumericalCheck> handleList = new ArrayList<>();


    public static void register(NumericalCheck numericalCheck) {
        handleList.add(numericalCheck);
    }

    /**
     * 数值系统
     *
     * @param user
     * @param numericalName
     * @param value
     * @return
     */
    public boolean numericalAdd(User user, String numericalName, long value) {

        if(user!=null && user.getUserNumericalMap()!=null){
            UserNumerical userNumerical = user.getUserNumericalMap().get(numericalName);
            if (userNumerical != null) {

                for (NumericalCheck numericalCheck : handleList) {
                    numericalCheck.check(user, numericalName, value);
                }

                userNumerical.setValue(userNumerical.getValue() + value);
                return true;
            }
        }


        return false;
    }

    /**
     * 归零
     * @param user
     * @param numericalName
     * @return
     */
    public boolean numericalTo0(User user, String numericalName) {

        UserNumerical userNumerical = user.getUserNumericalMap().get(numericalName);
        if (userNumerical != null) {
            userNumerical.setValue(0l);
            return true;
        }

        return false;

    }
}
