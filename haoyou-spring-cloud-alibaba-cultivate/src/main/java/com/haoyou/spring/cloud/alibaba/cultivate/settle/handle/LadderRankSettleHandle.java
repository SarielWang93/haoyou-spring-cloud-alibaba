package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.bean.RankUser;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/30 10:42
 * <p>
 * 传奇联赛排名结算奖励
 */
@Service
public class LadderRankSettleHandle extends SettleHandle {

    private String rankKey = null;

    @Override
    public void handle() {

        DateTime dateTime = DateUtil.offsetMonth(this.date, -1);

        String yyMM = dateTime.toString("yyMM");

        rankKey = RedisKeyUtil.getKey(RedisKey.LADDER_RANKING, yyMM);

        for(User user:this.users){
            String type = null;
            Long ladder_level_max = user.getUserNumericalMap().get("ladder_level_max").getValue();
            if(ladder_level_max < 15){
                //未进入传奇联赛
                type = String.format("ladder%s", ladder_level_max);
            }else{
                //传奇联赛排名
                //ladder_ranking6-10
                //ladder_ranking11-20
                //ladder_ranking21-50
                //ladder_ranking51-100
                //ladder_ranking101-200
                //ladder_ranking201-500
                //ladder_ranking501-1000
                //ladder_ranking1001
                Long aLong = scoreRankUtil.find(rankKey, user);
                if(aLong<6){
                    type = String.format("ladder_ranking%s", aLong);
                }else if(aLong<11){
                    type = "ladder_ranking6-10";
                }else if(aLong<21){
                    type = "ladder_ranking11-20";
                }else if(aLong<51){
                    type = "ladder_ranking21-50";
                }else if(aLong<101){
                    type = "ladder_ranking51-100";
                }else if(aLong<201){
                    type = "ladder_ranking101-200";
                }else if(aLong<501){
                    type = "ladder_ranking201-500";
                }else if(aLong<1001){
                    type = "ladder_ranking501-1000";
                }else {
                    type = "ladder_ranking1001";
                }
            }
            if(StrUtil.isNotEmpty(type)){
                upAward(user.getUid(),type);
            }

            //修改上月积分数据
            Long ladder_integral = user.getUserNumericalMap().get("ladder_integral").getValue();
            Long ladder_integral_last_month = user.getUserNumericalMap().get("ladder_integral_last_month").getValue();
            scoreRankUtil.incrementScore(rankKey,user,-ladder_integral_last_month);
            scoreRankUtil.incrementScore(rankKey,user,ladder_integral);

        }
    }

    private void upAward(String userUid,String type){
        Award award = rewardService.getAward(type);
        rewardService.refreshUpAward(userUid,award,this.rankKey);
    }

    @Override
    public boolean chackDate() {
        return isRefresh(30);
    }


}
