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
        this.handleType = SendType.GET_DAILY_TASK;
    }

    @Override
    public BaseMessage handle(MyRequest req) {


        User user = req.getUser();

        MapBody mapBody = new MapBody<>();



        Map<String, Object> msgMap = this.getMsgMap(req);

        //当前完成度
        mapBody.put("dailyTaskTntegral",user.getUserNumericalMap().get("daily_task_integral").getValue());

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
            //完成度奖励
            if(dailyTask.getName().startsWith("daily_task_integral_")){
                Map integral = new HashMap();
                //名称
                integral.put("name",dailyTask.getName());
                //中文名称
                integral.put("l10n",dailyTask.getL10n());
                //描述
                integral.put("description",dailyTask.getDescription());
                //目标值
                integral.put("aim",dailyTask.getAim());
                //能否领取
                integral.put("canReceive",canReceive);
                //奖励
                integral.put("award",award);
                if(canReceive){
                    //奖励领取type
                    integral.put("type",type);
                }
                dailyTaskIntegralMsg.add(integral);
            }
            //每日任务
            else{
                Map dailyTaskMap = new HashMap();
                //名称
                dailyTaskMap.put("name",dailyTask.getName());
                //中文名称
                dailyTaskMap.put("l10n",dailyTask.getL10n());
                //描述
                dailyTaskMap.put("description",dailyTask.getDescription());
                //当前值
                dailyTaskMap.put("nowValue",user.getUserNumericalMap().get(dailyTask.getNumericalName()).getValue());
                //目标值
                dailyTaskMap.put("aim",dailyTask.getAim());
                //奖励
                dailyTaskMap.put("award",award);
                //能否领取
                dailyTaskMap.put("canReceive",canReceive);
                //完成度积分
                dailyTaskMap.put("integral",dailyTask.getIntegral());
                if(canReceive){
                    //奖励领取type
                    dailyTaskMap.put("type",type);
                }
                dailyTaskMsg.add(dailyTaskMap);
            }


        }

        mapBody.put("dailyTaskIntegralMsg", dailyTaskIntegralMsg);
        mapBody.put("dailyTaskMsg", dailyTaskMsg);
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }
}
