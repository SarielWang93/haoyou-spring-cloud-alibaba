package com.haoyou.spring.cloud.alibaba.match.info;

import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.match.service.impl.MatchPoolServiceImpl;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * 匹配玩家对象
 */
@Data
public class MatchPoolPlayerInfo implements Serializable {

    private static final long serialVersionUID = -2709685016914807961L;
    private String playerId;//玩家ID
    private User player;//玩家ID
    private int rank;//玩家分数
    private long startMatchTime;//开始匹配时间
    private int isAccept;//0:待定 1：接受  2：拒绝

    public MatchPoolPlayerInfo() {

    }

    public MatchPoolPlayerInfo(User player) {
        super();
        this.isAccept=0;
        this.playerId = player.getUid();
        this.player = player;
        this.rank = player.getRank();
        this.startMatchTime = System.currentTimeMillis();
    }


    /**
     * redis缓存
     */
    public boolean save(String playerRoomUidKey, RedisObjectUtil redisObjectUtil, MatchPoolServiceImpl matchPoolServiceImpl) {

        return redisObjectUtil.save(RedisKeyUtil.getKey(playerRoomUidKey, this.playerId),this, matchPoolServiceImpl.MAX_TIME);

    }

    /**
     * redis缓存删除
     */
    public boolean delete(String playerRoomUidKey,RedisObjectUtil redisObjectUtil) {
        return redisObjectUtil.delete(RedisKeyUtil.getKey(playerRoomUidKey, this.playerId));
    }

}