package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Email;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 获取用户邮件信息
 */
@Service
public class GetEmailsHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetEmailsHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_EMAILS;
    }

    @Override
    public BaseMessage handle(MyRequest req) {

        MapBody mapBody = new MapBody<>();

        User user = req.getUser();

        String lkKey = RedisKeyUtil.getlkKey(RedisKey.USER_AWARD, user.getUid(), RedisKey.EMIL);


        HashMap<String, Email> stringEmailHashMap = redisObjectUtil.getlkMap(lkKey, Email.class);

        int whileDestroy = 0;

        TreeMap<Long, Email> emailTreeMap = new TreeMap<>();
        for (Email email : stringEmailHashMap.values()) {
            emailTreeMap.put(email.getCreatDate().getTime(), email);
            if ((new Date().getTime() - email.getCreatDate().getTime()) < Email.EMAIL_ALIVE_TIME
                    && !(email.isHaveRead() && email.getAward().isUsed())) {

                whileDestroy++;
            }
        }

        ArrayList<Email> emails = CollUtil.newArrayList(emailTreeMap.values());
        mapBody.put("emails", emails);
        mapBody.put("whileDestroy", whileDestroy);
        mapBody.setState(ResponseMsg.MSG_SUCCESS);

        return mapBody;
    }
}
