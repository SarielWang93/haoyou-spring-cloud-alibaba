package com.haoyou.spring.cloud.alibaba.cultivate.service;

import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.commons.entity.Email;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
    @Autowired
    private UserUtil userUtil;



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
        User userByUid = userUtil.getUserByUid(userUid);


        Email email = new Email(title, text, award);

        userUtil.addEmail(userByUid, email);

        userUtil.saveUser(userByUid);

        //发送新的未读邮件数量
        TreeMap<Date, Email> emails = userUtil.getEmails(userByUid);
        int i = 0;
        for(Email email1:emails.values()){
            if(!email1.isHaveRead()){
                i++;
            }
        }
        MapBody mapBody = new MapBody();
        mapBody.put("emailsCount",i);
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        sendMsgUtil.sendMsgOneNoReturn(userUid, SendType.EMIL, mapBody);
        return true;

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
        TreeMap<Date, Email> emails = userUtil.getEmails(user);

        //获取邮件对象
        Email email = null;

        for(Email email1 : emails.values()){
            if(email1.getUid().equals(emailUid)){
                email = email1;
            }
        }
        if(email == null){
            email = new Email();
            email.setState(ResponseMsg.MSG_ERR);
            return email;
        }

        //操作邮件
        boolean b = this.emailDo(user, email, type);


        //返回信息
        if(type == 3){
            email = new Email();
        }
        if(b){
            email.setState(ResponseMsg.MSG_SUCCESS);
        }else{
            email.setState(ResponseMsg.MSG_ERR);
        }

        return email;
    }

    /**
     * 一键操作
     *
     * @param user
     * @param type 1：领取奖励   2：已读 3：删除
     */
    public void emailAll(User user, int type) {
        TreeMap<Date, Email> emails = userUtil.getEmails(user);

        for (Email email : emails.values()) {
            this.emailDo(user, email, type);
        }

    }

    /**
     * 邮件操作
     *
     * @param user
     * @param email
     * @param type 1：领取奖励   2：已读 3：删除
     */
    private boolean emailDo(User user, Email email, int type) {

        switch (type) {
            case 1:
                if(!email.isHaveRead()){
                    email.setHaveRead(true);
                    userUtil.addEmail(user,email);
                    return true;
                }
                break;
            case 2:
                if(!email.isHaveRead()){
                    email.setHaveRead(true);
                    if(this.emailReceive(user, email)){
                        userUtil.addEmail(user,email);
                        return true;
                    }
                }
                break;
            case 3:
                if (email.isHaveRead() && (email.getAward() == null || email.getAward().isUsed())) {
                    userUtil.deleteEmail(user,email);
                    return true;
                }
                break;
        }
        return false;
    }


    /**
     * 领取奖励
     *
     * @param user
     * @param email
     */
    private boolean emailReceive(User user, Email email) {

        Award award = email.getAward();
        if (award != null && !award.isUsed()) {
            if (rewardService.doAward(user, award)) {
                award.setUsed(true);
                return true;
            }
        }
        return false;

    }

}
