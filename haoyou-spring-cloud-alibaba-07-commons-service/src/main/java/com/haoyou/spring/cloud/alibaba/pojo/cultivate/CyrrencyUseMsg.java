package com.haoyou.spring.cloud.alibaba.pojo.cultivate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.ZIP;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class CyrrencyUseMsg extends BaseMessage implements Serializable {


    private static final long serialVersionUID = -6071861020649838993L;

    //消费类型
    private Integer type;

    //目标宠物
    private String petUid;
    //是否钻石操作
    private boolean diamond;

    //商店名称
    private String storeName;
    //商品名称
    private String commodityName;


    //用户
    private User user;

}
