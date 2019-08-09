package com.haoyou.spring.cloud.alibaba.cultivate.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Server;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.settle.handle.SettleHandle;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/16 10:18
 */
@Service
@Data
public class SettlementService {

    @Autowired
    private RedisObjectUtil redisObjectUtil;


    private static List<SettleHandle> handleList = new ArrayList<>();


    public static void register(SettleHandle settleHandle) {
        handleList.add(settleHandle);
    }


    @Autowired
    private UserUtil userUtil;

    private Long runingDays;

    /**
     * 每隔一小时，检查结算
     */
//    @scheduled(cron = "0 0 */1 * * ?")
    public void inspect() {
        //当前时间，和运行天数计算
        DateTime date = DateUtil.date();
        getRuningDays(date);

        //执行结算处理器
        for (SettleHandle settleHandle : handleList) {
            settleHandle.setDate(date);
            settleHandle.setRuningDays(this.runingDays);
            settleHandle.doHandle();
        }

    }





    //直接执行所有结算
    public void doAll() {
        DateTime date = DateUtil.date();
        getRuningDays(date);
        //执行结算处理器
        List<User> users = userUtil.allUser();
        for (SettleHandle settleHandle : handleList) {
            settleHandle.setRuningDays(this.runingDays);
            settleHandle.setDate(date);
            settleHandle.doWithUsers();
        }
    }

    private void getRuningDays(DateTime date){
        int hour = date.hour(true);
        if(this.runingDays == null || hour == 0){
            HashMap<String, Server> stringServerHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.SERVER), Server.class);
            TreeMap<Date,Server> serverTreeMap = new TreeMap<>();
            for(Server server : stringServerHashMap.values()){
                serverTreeMap.put(server.getCreatDate(),server);
            }
            Map.Entry<Date, Server> firstEntry = serverTreeMap.firstEntry();

            this.runingDays = DateUtil.betweenDay(firstEntry.getKey(),date,true);
        }
    }
}
