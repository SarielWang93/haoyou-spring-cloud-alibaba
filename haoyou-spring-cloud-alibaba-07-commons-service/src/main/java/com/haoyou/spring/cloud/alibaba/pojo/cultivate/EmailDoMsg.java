package com.haoyou.spring.cloud.alibaba.pojo.cultivate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: wanghui
 * @Date: 2019/5/13 14:00
 * @Version 1.0
 */
@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class EmailDoMsg implements Serializable {


    private static final long serialVersionUID = 3802113330026730109L;

    private String emailUid;

    //1：领取奖励   2：已读 3：删除
    private int type;


}
