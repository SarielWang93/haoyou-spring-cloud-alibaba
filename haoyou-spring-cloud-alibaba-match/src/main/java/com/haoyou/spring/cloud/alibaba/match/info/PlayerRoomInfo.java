package com.haoyou.spring.cloud.alibaba.match.info;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.match.service.impl.MatchPoolServiceImpl;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * 房间对象
 */
@Data
@JsonIgnoreProperties(value = {"matchPoolPlayer"},ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class PlayerRoomInfo extends BaseMessage implements Serializable {


    private static final long serialVersionUID = 7632334750835270281L;
    private String uid;
    private Long creatTime;//创建房间时间
    HashMap<String, MatchPoolPlayerInfo> matchPoolPlayer;

    /**
     * 匹配成功创建房间
     *
     * @param matchPoolPlayer
     */
    public PlayerRoomInfo(List<MatchPoolPlayerInfo> matchPoolPlayer) {
        this.matchPoolPlayer = new HashMap<>();
        this.uid = IdUtil.simpleUUID();
        for (MatchPoolPlayerInfo playerInfo : matchPoolPlayer) {
            playerInfo.setIsAccept(1);
            this.matchPoolPlayer.put(playerInfo.getPlayerId(), playerInfo);
        }

        this.creatTime = System.currentTimeMillis();
    }

    /**
     * 根据房间uid获取房间
     *
     * @param uid
     */
    public PlayerRoomInfo(String uid, RedisObjectUtil redisObjectUtil) {
        this.uid = uid;
        String playerRoomUidKey = RedisKeyUtil.getKey(RedisKey.MATCH_PLAYER_ROOM, uid);
        this.creatTime=redisObjectUtil.get(RedisKeyUtil.getKey(playerRoomUidKey, "creatTime"),Long.class);

        HashMap<String, MatchPoolPlayerInfo> matchPoolPlayer = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKeyUtil.getKey(playerRoomUidKey, "playerInfo")),MatchPoolPlayerInfo.class);


        this.matchPoolPlayer = new HashMap<>();
        for(MatchPoolPlayerInfo playerInfo:matchPoolPlayer.values()){
            if (playerInfo != null) {
                this.matchPoolPlayer.put(playerInfo.getPlayerId(), playerInfo);
            }
        }


    }

    public PlayerRoomInfo() {

    }

    /**
     * redis缓存
     */
    public void save(RedisObjectUtil redisObjectUtil, MatchPoolServiceImpl matchPoolServiceImpl) {
        String playerRoomUidKey = RedisKeyUtil.getKey(RedisKey.MATCH_PLAYER_ROOM, this.uid);
        //redis存储房间创建时间
        redisObjectUtil.save(RedisKeyUtil.getKey(playerRoomUidKey, "creatTime"), this.creatTime, matchPoolServiceImpl.MAX_TIME);
        //redis存储房间玩家信息
        for(MatchPoolPlayerInfo playerInfo:matchPoolPlayer.values()){
            playerInfo.save(RedisKeyUtil.getKey(playerRoomUidKey, "playerInfo"),redisObjectUtil, matchPoolServiceImpl);
        }


    }

    /**
     * redis缓存删除
     */
    public void delete(RedisObjectUtil redisObjectUtil) {
        String playerRoomUidKey = RedisKeyUtil.getKey(RedisKey.MATCH_PLAYER_ROOM, this.uid);
        //redis存储房间创建时间
        redisObjectUtil.delete(RedisKeyUtil.getKey(playerRoomUidKey, "creatTime"));
        //redis存储房间玩家信息
        for(MatchPoolPlayerInfo playerInfo:matchPoolPlayer.values()){
            playerInfo.delete(RedisKeyUtil.getKey(playerRoomUidKey, "playerInfo"),redisObjectUtil);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerRoomInfo that = (PlayerRoomInfo) o;
        return Objects.equals(uid, that.uid) &&
                Objects.equals(creatTime, that.creatTime) &&
                Objects.equals(matchPoolPlayer, that.matchPoolPlayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, creatTime, matchPoolPlayer);
    }
}
