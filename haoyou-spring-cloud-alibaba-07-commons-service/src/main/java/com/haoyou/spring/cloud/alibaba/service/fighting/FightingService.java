package com.haoyou.spring.cloud.alibaba.service.fighting;

import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;

import java.util.List;
import java.util.Map;

/**
 * 调用manager服务
 */

public interface FightingService {

    /**
     * 闯关模式
     *
     * @param user
     * @param chapterName
     * @param idNum
     * @param difficult
     * @return
     */
    boolean start(User user, String chapterName, int idNum, int difficult);

    boolean start(User user, String chapterName, int idNum, int difficult, boolean isAi);

    boolean start(List<User> users);

    boolean start(List<User> users, Map<String, Boolean> allIsAi);


    MapBody receiveFightingMsg(MyRequest req);

}
