package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
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
 * 获取用户邮件信息
 */
@Service
public class GetActivityHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetActivityHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_ACTIVITY;
    }

    @Override
    public BaseMessage handle(MyRequest req) {

        MapBody mapBody = new MapBody<>();

        User user = req.getUser();

        Map<String, Object> msgMap = this.getMsgMap(req);


        //活动类型
        String activityType = (String) msgMap.get("activityType");

        //获取活动对象
        HashMap<String, Activity> stringActivityHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.ACTIVITY, activityType), Activity.class);


        for (Activity activity : stringActivityHashMap.values()) {
            //当前生效的活动
            if (activity.isCurrent()) {
                Map<String, Object> activityMsg = new HashMap<>();
                //活动类型
                activityMsg.put("activityType", activity.getActivityType());
                //活动介绍
                activityMsg.put("description", activity.getDescription());

                //活动执行第几天
                DateTime date = DateUtil.date();
                Integer refresh = activity.getRefresh();
                if(refresh>100){
                    refresh-=100;
                    long d = userUtil.getRuningDays(date)%refresh;
                    activityMsg.put("days", d+1);
                }else{
                    if(refresh.equals(1)){
                        activityMsg.put("days", 1);
                    }else if(refresh.equals(7)){
                        int a=date.dayOfWeek() - 1;
                        if(a==0){
                            a=7;
                        }
                        activityMsg.put("days", a);
                    }else if(refresh.equals(30)){
                        activityMsg.put("days", date.dayOfMonth());
                    }                }
                //活动持续天数
                activityMsg.put("refresh", refresh);


                UserNumerical userNumerical = user.getUserNumericalMap().get(activity.getNumericalName());

                //活动奖励列表
                List<Map> activityAwardsMsg = new ArrayList<>();

                boolean today = false;

                for (ActivityAward activityAward : activity.getActivityAwards()) {

                    Map<String, Object> activityAwardMsg = new HashMap<>();

                    //目标值
                    activityAwardMsg.put("aim", activityAward.getAim());
                    //进度（天天充值）
                    activityAwardMsg.put("schedule", activityAward.getSchedule());
                    //领取次数（单笔充值）
                    activityAwardMsg.put("times", activityAward.getTimes());


                    String type = RedisKeyUtil.getKey(RedisKey.ACTIVITY, activity.getActivityType(), activityAward.getAwardType());

                    activityAwardMsg.put("canReceive", false);
                    Award award = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, activityAward.getAwardType()), Award.class);
                    activityAwardMsg.put("award", award);

                    //是否
                    if (activity.getActivityType().equals("SingleRecharge")) {
                        userNumerical = user.getUserNumericalMap().get(String.format("commodity_Recharge%s", activityAward.getAim()));

                        Long value = userNumerical.getValue();


                        if(userNumerical.getValue() > activityAward.getTimes()){
                            value = activityAward.getTimes().longValue();
                        }

                        //当前数值
                        activityAwardMsg.put("userNumerical", value);

                        for (int i = 1; i <= userNumerical.getValue(); i++) {
                            String typei = RedisKeyUtil.getKey(type, Integer.toString(i));
                            Award upAward = this.getUpAward(user.getUid(), typei);
                            if (upAward != null && !upAward.isUsed()) {
                                //是否已经达成目标
                                activityAwardMsg.put("canReceive", true);
                                //奖励
                                activityAwardMsg.put("award", upAward);
                                //领取时需要的type
                                activityAwardMsg.put("type", typei);

                                activityAwardMsg.put("userNumerical", i-1);
                                break;
                            }
                        }
                    } else {
                        //当前数值
                        activityAwardMsg.put("userNumerical", userNumerical.getValue());


                        Award upAward = this.getUpAward(user.getUid(), type);
                        if (upAward != null) {
                            //是否已经达成目标
                            activityAwardMsg.put("canReceive", true);
                            //奖励
                            activityAwardMsg.put("award", upAward);

                            if(activity.getActivityType().equals("DailyRecharge")){
                                //已达成的当前数值是目标数值
                                activityAwardMsg.put("userNumerical", activityAward.getAim());
                            }

                            if(!upAward.isUsed()){
                                activityAwardMsg.put("type", type);
                            }
                            //是否有今天奖励
                            if(DateUtil.betweenDay(upAward.getUpAwardDate(),new Date(),true) == 0){
                                today = true;
                            }
                        }
                        //天天充值判断当天进度显示逻辑
                        if(upAward == null && activity.getActivityType().equals("DailyRecharge")){
                            if(today){
                                userNumerical.setValue(0L);
                                activityAwardMsg.put("userNumerical", userNumerical.getValue());
                            }else{
                                today = true;
                            }
                        }
                    }

                    activityAwardsMsg.add(activityAwardMsg);
                }
                activityMsg.put("activityAwards", activityAwardsMsg);


                mapBody.put(activity.getActivityType(), activityMsg);
            }
        }


        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }

}
