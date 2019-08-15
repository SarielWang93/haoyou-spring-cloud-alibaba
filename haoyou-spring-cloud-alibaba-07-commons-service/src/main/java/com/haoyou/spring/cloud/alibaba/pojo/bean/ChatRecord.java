package com.haoyou.spring.cloud.alibaba.pojo.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 */

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class ChatRecord extends BaseMessage implements Serializable {

    private String userUid;

    private Date sendTime;

    private String sendMsg;

    private boolean notRead;

    public ChatRecord() {
    }

    public ChatRecord(String userUid, Date sendTime, String sendMsg) {
        this.userUid = userUid;
        this.sendTime = sendTime;
        this.sendMsg = sendMsg;
    }
}
