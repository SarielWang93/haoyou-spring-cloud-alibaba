package com.haoyou.spring.cloud.alibaba.service.match;

import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;


/**
 * 调用manager服务
 */

public interface MatchService {

    void doMatch();

    boolean putPlayerIntoMatchPool( User user);


    boolean removePlayerFromMatchPool( User user);

    boolean playerAccept(MyRequest req, int accept);
}
