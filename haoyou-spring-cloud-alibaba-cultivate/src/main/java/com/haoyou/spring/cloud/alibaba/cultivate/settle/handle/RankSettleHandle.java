package com.haoyou.spring.cloud.alibaba.cultivate.settle.handle;

import cn.hutool.core.date.DateTime;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Server;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Long aLong = scoreRankService.zCard(RedisKeyUtil.getKey(RedisKey.RANKING, server.getServerNum().toString()));

            List<String> list = scoreRankService.list(RedisKeyUtil.getKey(RedisKey.RANKING, server.getServerNum().toString()), 0l, aLong);


            for (int i = 0; i < list.size(); i++) {
                //名次
                int r = i + 1;
                Map<String, Object> player = null;
                try {
                    player = MapperUtils.json2map(list.get(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String useruid = (String) player.get("useruid");
                Award award = this.getAward(r);
                if (award != null) {
                    String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, useruid, RedisKey.RANKING);
                    redisObjectUtil.save(key, award);
                }

            }


        }
    }

    @Override
    public boolean chackDate(DateTime date) {
        int hour = date.hour(true);
        return hour == 3;
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
    private Award getAward(int r) {

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
            awardType = Integer.toString(r);
        }

        return redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.AWARD, RedisKey.RANKING + awardType), Award.class);
    }
}
