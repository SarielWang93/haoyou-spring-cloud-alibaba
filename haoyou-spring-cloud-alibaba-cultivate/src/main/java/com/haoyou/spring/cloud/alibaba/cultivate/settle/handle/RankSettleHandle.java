package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Server;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.pojo.bean.RankUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/16 15:37
 * <p>
 * 分服排行榜结算
 */
@Service
public class RankSettleHandle extends SettleHandle {
    @Override
    public void handle() {

        HashMap<String, Server> servers = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.SERVER), Server.class);
        for (Server server : servers.values()) {
            String rankKey = RedisKeyUtil.getKey(RedisKey.RANKING, server.getServerNum().toString());

            Long aLong = scoreRankUtil.zCard(rankKey);

            TreeMap<Long, String> treeMap = scoreRankUtil.list(rankKey, 0l, aLong);

            long rank = aLong;

            for (long integral: treeMap.keySet()) {
                String userUid = treeMap.get(integral);
                Award award = this.getAward(rank--);
                rewardService.refreshUpAward(userUid,award,rankKey);


                //重置积分
                User user = userUtil.getUserByUid(userUid);

                Long daily_ladder_integral = user.getUserNumericalMap().get("daily_ladder_integral").getValue();

                Long ladder_level = user.getUserNumericalMap().get("ladder_level").getValue();

                scoreRankUtil.incrementScore(rankKey,user,-daily_ladder_integral);

                scoreRankUtil.incrementScore(rankKey,user,(ladder_level+1)*10);

            }


        }
    }

    @Override
    public boolean chackDate() {
        int hour = this.date.hour(true);
        return hour == 0;
    }

    /**
     * 根据名次获取奖励
     * 排名显示分为9档。
     * 1、2、3、4、5、6-10、
     * 11-20、21-50、51-100。
     * 如果再有1档为101-200。
     * <p>
     * 排行榜奖励 type字段应该是 RedisKey.RANKING 与 范围的拼接 例子："ranking51-100"
     *
     * @param r
     */
    private Award getAward(long r) {

        String awardType = "null";

        if (r > 100) {
            awardType = "101-200";
        } else if (r > 50) {
            awardType = "51-100";
        }
        if (r > 20) {
            awardType = "21-50";
        }
        if (r > 10) {
            awardType = "11-20";
        }
        if (r > 5) {
            awardType = "6-10";
        } else {
            awardType = Long.toString(r);
        }

        return redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, RedisKey.RANKING + awardType), Award.class);
    }
}
