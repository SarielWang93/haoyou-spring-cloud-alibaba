package com.haoyou.spring.cloud.alibaba.pojo.cultivate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class RMBUseMsg extends BaseMessage implements Serializable {


    private static final long serialVersionUID = -6071861020649838993L;



    //消费类型
    private Integer type;
    //消费数额
    private Integer rmb;
    //消费名称(购买项目的名称)
    private String name;






    //用户
    private User user;

}
