package com.haoyou.spring.cloud.alibaba.pojo.cultivate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: wanghui
 * @Date: 2019/5/13 14:00
 * @Version 1.0
 */
@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class FriendsDoMsg implements Serializable {


    private static final long serialVersionUID = 3027933837770101556L;

    //对方uid
    private String userUid;

    //1：好友申请，2：同意好友申请，3：一键拒绝，4：赠送礼物，5：发送信息
    private int type;

    private boolean oneButton;

    private User user;


}
