package com.haoyou.spring.cloud.alibaba.manager.handle.login;


import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Email;
import com.haoyou.spring.cloud.alibaba.commons.entity.Server;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 登录处理
 */
@Service
public class LoginHandle extends ManagerHandle {
    private static final long serialVersionUID = 7039822205281201138L;
    private static final Logger logger = LoggerFactory.getLogger(LoginHandle.class);
    @Override
    protected void setHandleType() {
        this.handleType = SendType.LOGIN;
    }


    @Override
    public BaseMessage handle(MyRequest req) {
        User login = loginService.login(req);

        List<FightingPet> byUser = FightingPet.getByUser(login, redisObjectUtil);


        if(ResponseMsg.MSG_SUCCESS == login.getState() || ResponseMsg.MSG_LOGINOUT_FIGHTING == login.getState()){
            Map<String,Object> otherMsg = new HashMap<>();

            //宠物个数
            otherMsg.put("petsCount",byUser.size());

            //服务器名字
            Server server = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.SERVER, login.getServerId().toString()), Server.class);
            otherMsg.put("serverName",server.getServerName());
            //服务器时间
            otherMsg.put("serverDate",new Date());

            //当前服排名
            Long aLong = scoreRankUtil.find(RedisKeyUtil.getKey(RedisKey.RANKING, server.getId().toString()), login);
            otherMsg.put("serverRankNum",aLong);

            //未读邮件数量
            TreeMap<Date, Email> emails = userUtil.getEmails(login);
            int i = 0;
            for(Email email1:emails.values()){
                if(!email1.isHaveRead()){
                    i++;
                }
            }
            otherMsg.put("emailsCount",i);
            login.setOtherMsg(otherMsg);
        }

        return login.notTooLong();
    }
}
