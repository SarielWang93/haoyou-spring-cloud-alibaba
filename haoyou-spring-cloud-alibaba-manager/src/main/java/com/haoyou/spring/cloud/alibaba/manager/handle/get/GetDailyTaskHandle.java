package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 获取每日任务
 */
@Service
public class GetDailyTaskHandle extends ManagerHandle {


    private static final Logger logger = LoggerFactory.getLogger(GetDailyTaskHandle.class);
    private static final long serialVersionUID = -9187389077840416711L;

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_RANK;
    }

    @Override
    public BaseMessage handle(MyRequest req) {


        User user = req.getUser();

        MapBody mapBody = new MapBody<>();



        Map<String, Object> msgMap = this.getMsgMap(req);

        //当前完成度
        mapBody.put("dailyTaskTntegral",user.getUserNumericalMap().get("daily_task_integral"));

        HashMap<String, DailyTask> stringDailyTaskHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.DAILY_TASK), DailyTask.class);

        //每日任务完成度奖励信息
        List<Map> dailyTaskIntegralMsg = new ArrayList<>();
        //每日任务信息
        List<Map> dailyTaskMsg = new ArrayList<>();

        for (DailyTask dailyTask : stringDailyTaskHashMap.values()) {

            String type = RedisKeyUtil.getKey(RedisKey.DAILY_TASK, dailyTask.getName());
            String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid(), type);

            boolean canReceive = false;
            //奖励
            Award award = redisObjectUtil.get(key, Award.class);

            if(award!=null){
                if(!award.isUsed()){
                    canReceive = true;
                }
            }else{
                award = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD,dailyTask.getAwardType()),Award.class);
            }
            //完成度
            if(dailyTask.getName().startsWith("daily_task_integral_")){
                Map integral = new HashMap();
                integral.put("name",dailyTask.getName());
                integral.put("aim",dailyTask.getAim());
                integral.put("canReceive",canReceive);
                integral.put("award",award);
                if(canReceive){
                    integral.put("type",type);
                }
                dailyTaskIntegralMsg.add(integral);
            }
            //每日任务
            else{
                Map dailyTaskMap = new HashMap();
                dailyTaskMap.put("name",dailyTask.getName());
                dailyTaskMap.put("nowValue",user.getUserNumericalMap().get(dailyTask.getNumericalName()));
                dailyTaskMap.put("aim",dailyTask.getAim());
                dailyTaskMap.put("award",award);
                dailyTaskMap.put("canReceive",canReceive);
                dailyTaskMap.put("integral",dailyTask.getIntegral());
                if(canReceive){
                    dailyTaskMap.put("type",type);
                }
                dailyTaskMsg.add(dailyTaskMap);
            }


        }

        mapBody.put("dailyTaskIntegralMsg", dailyTaskIntegralMsg);
        mapBody.put("dailyTaskMsg", dailyTaskMsg);
        mapBody.setState(ResponseMsg.MSG_ERR);
        return mapBody;
    }
}
