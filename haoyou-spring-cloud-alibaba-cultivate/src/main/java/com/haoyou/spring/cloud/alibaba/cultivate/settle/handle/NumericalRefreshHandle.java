package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import cn.hutool.core.date.DateTime;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Numerical;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.entity.UserNumerical;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/30 10:42
 * <p>
 * 数值系统刷新
 */
@Service
public class NumericalRefreshHandle extends SettleHandle {


    @Override
    public void handle() {
        int dayOfWeek = this.date.dayOfWeek();
        int dayOfMonth = this.date.dayOfMonth();


        HashMap<String, Numerical> stringNumericalHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.NUMERICAL), Numerical.class);

        for (Numerical numerical : stringNumericalHashMap.values()) {
            String name = numerical.getName();
            Integer refresh = numerical.getRefresh();

            //执行日清零，周清零，月清零
            if (refresh.equals(1) || (dayOfWeek == 2 && refresh.equals(7)) || (dayOfMonth == 1 && refresh.equals(30))) {
                this.refresh(name);
            }

        }

    }

    @Override
    public boolean chackDate() {
        int hour = this.date.hour(true);
        return hour == 0;
    }

    /**
     * 清零数值
     *
     * @param numericalName
     */
    private void refresh(String numericalName) {
        UserNumerical userNumericalselect = new UserNumerical();
        userNumericalselect.setNumericalName(numericalName);
        List<UserNumerical> userNumericals = userNumericalMapper.select(userNumericalselect);

        for (UserNumerical userNumerical : userNumericals) {
            //本身为0则不必清零
            if(!userNumerical.getValue().equals(0)){
                //如果有缓存则缓存清零，否则数据库清零
                String userUid = userNumerical.getUserUid();
                User user = userUtil.getUserByUid(userUid);
                if(user == null){
                   continue;
                }else{
                    user.getUserNumericalMap().get("numericalName").setValue(0L);
                    userUtil.saveUser(user);
                }
            }
        }
    }
}
