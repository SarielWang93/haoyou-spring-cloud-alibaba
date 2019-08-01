package com.haoyou.spring.cloud.alibaba.commons.entity;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/8/1 14:33
 * 邮件对象
 */
@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Email extends BaseMessage implements Serializable {

    private static final long serialVersionUID = 8674882322481406010L;

    public static long EMAIL_ALIVE_TIME = 60L * 60L * 24L * 30L;




    private String uid;

    //邮件标题
    private String title;

    //创建时间
    private Date creatDate;

    //邮件内容
    private String text;

    //奖励
    private Award award;

    //是否已读
    private boolean haveRead;

    public Email() {
    }

    public Email(String title, String text, Award award) {
        this.uid = IdUtil.simpleUUID();
        this.title = title;
        this.text = text;
        this.award = award;
        this.haveRead = false;
        this.creatDate = new Date();
    }
}
