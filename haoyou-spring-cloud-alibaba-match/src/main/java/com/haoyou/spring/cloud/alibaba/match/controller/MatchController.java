package com.haoyou.spring.cloud.alibaba.match.controller;

import org.apache.dubbo.config.annotation.Service;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.match.info.PlayerRoomInfo;
import com.haoyou.spring.cloud.alibaba.match.service.MatchPoolService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Service(version = "${match.service.version}")
public class MatchController implements com.haoyou.spring.cloud.alibaba.service.match.MatchService{
    private final static Logger logger = LoggerFactory.getLogger(MatchController.class);
    @Autowired
    private MatchPoolService matchPoolService;
    @Autowired
    private SendMsgUtil sendMsgUtil;
    @Autowired
    private RedisObjectUtil redisObjectUtil;


    public void doMatch(){
        matchPoolService.doMatch();
    }



    /**
     * 添加到匹配池
     * @param user
     * @return
     */
    public boolean putPlayerIntoMatchPool( User user){
        logger.info(String.format("putPlayerIntoMatchPool: %s ",user));
        return matchPoolService.putPlayerIntoMatchPool(user);
    }

    /**
     * 从匹配池移除
     * @param user
     * @return
     */
    public boolean removePlayerFromMatchPool( User user){
        logger.info(String.format("removePlayerFromMatchPool: %s ",user));
        return matchPoolService.removePlayerFromMatchPool(user);
    }

    /**
     * 同意与否
     * @param req
     * @param accept 1同意，2拒绝
     * @return
     */
    public boolean playerAccept(MyRequest req, int accept){

        PlayerRoomInfo playerRoomrInfo = sendMsgUtil.deserialize(req.getMsg(), PlayerRoomInfo.class);

        logger.info(String.format("playerAccept: %s %s %s",req.getUser(),accept,playerRoomrInfo));
        return matchPoolService.playerAccept(req.getUser(),accept,playerRoomrInfo);
    }

}
