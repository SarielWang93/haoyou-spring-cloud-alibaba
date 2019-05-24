package com.haoyou.spring.cloud.alibaba.match.service;

import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.match.info.MatchPoolPlayerInfo;
import com.haoyou.spring.cloud.alibaba.match.info.PlayerRoomInfo;


public interface MatchPoolService {
    void doMatch();
    boolean putPlayerIntoMatchPool(User player);
    MatchPoolPlayerInfo findPlayerFromMatchPool(User player);
    boolean removePlayerFromMatchPool(User player);
    boolean playerAccept(User player, int accept, PlayerRoomInfo playerRoomrInfo);
}
