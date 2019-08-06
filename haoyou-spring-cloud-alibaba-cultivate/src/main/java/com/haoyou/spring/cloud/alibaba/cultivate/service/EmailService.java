package com.haoyou.spring.cloud.alibaba.cultivate.service;

import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.commons.entity.Email;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/22 10:04
 * <p>
 * 邮件系统
 */
@Service
public class EmailService {



    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private SendMsgUtil sendMsgUtil;
    @Autowired
    private RewardService rewardService;

    /**
     * 发送邮件
     * @param userUid
     * @param title
     * @param text
     * @param award
     * @return
     */
    public boolean sendEmail(String userUid, String title, String text, Award award) {

        if(StrUtil.isEmpty(userUid)){
            return false;
        }

        Email email = new Email(title, text, award);
        String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, userUid, RedisKey.EMIL, email.getUid());

        if (redisObjectUtil.save(key, email, Email.EMAIL_ALIVE_TIME)) {
            sendMsgUtil.sendMsgOneNoReturn(userUid, SendType.EMIL, email);
            return true;
        }

        return false;
    }

    /**
     * 单个Email操作
     *
     * @param user
     * @param type     1：领取奖励   2：已读 3：删除
     * @param emailUid
     * @return
     */
    public Email emailOne(User user, int type, String emailUid) {
        String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, user.getUid(), RedisKey.EMIL, emailUid);
        Email email = redisObjectUtil.get(key, Email.class);
        this.emailDo(key, user, email, type);
        if(type == 3){
            email = new Email();
        }
        email.setState(ResponseMsg.MSG_SUCCESS);
        return email;
    }

    /**
     * 一键操作
     *
     * @param user
     * @param type 1：领取奖励   2：已读 3：删除
     */
    public void emailAll(User user, int type) {
        String lkKey = RedisKeyUtil.getlkKey(RedisKey.USER_AWARD, user.getUid(), RedisKey.EMIL);
        HashMap<String, Email> stringEmailHashMap = redisObjectUtil.getlkMap(lkKey, Email.class);

        for (Map.Entry<String, Email> entry : stringEmailHashMap.entrySet()) {
            String key = entry.getKey();
            Email email = entry.getValue();
            this.emailDo(key, user, email, type);
        }

    }

    /**
     * 邮件操作
     *
     * @param key
     * @param user
     * @param email
     * @param type
     */
    private void emailDo(String key, User user, Email email, int type) {

        switch (type) {
            case 1:
                email.setHaveRead(true);
                break;
            case 2:
                email.setHaveRead(true);
                this.emailReceive(user, email);
                break;
            case 3:
                if (email.isHaveRead() && (email.getAward() == null || email.getAward().isUsed())) {
                    redisObjectUtil.delete(key);
                }
                break;
        }

        if (type != 3) {
            redisObjectUtil.save(key, email);
        }
    }


    /**
     * 领取奖励
     *
     * @param user
     * @param email
     */
    private void emailReceive(User user, Email email) {

        Award award = email.getAward();
        if (award != null) {
            if (rewardService.doAward(user, award)) {
                award.setUsed(true);
            }
        }

    }

}
