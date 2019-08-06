package com.haoyou.spring.cloud.alibaba.cultivate.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.cultivate.settle.handle.SettleHandle;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/16 10:18
 */
@Service
@Data
public class SettlementService {


    private static List<SettleHandle> handleList = new ArrayList<>();


    public static void register(SettleHandle settleHandle) {
        handleList.add(settleHandle);
    }


    @Autowired
    protected UserUtil userUtil;
    /**
     * 每隔一小时，检查结算
     */
//    @scheduled(cron = "0 0 */1 * * ?")
    public void inspect() {

        DateTime date = DateUtil.date();
        List<User> users = userUtil.allUser();
        for (SettleHandle settleHandle : handleList) {
            settleHandle.setUsers(users);
            settleHandle.setDate(date);
            settleHandle.doHandle();
        }

    }
}
