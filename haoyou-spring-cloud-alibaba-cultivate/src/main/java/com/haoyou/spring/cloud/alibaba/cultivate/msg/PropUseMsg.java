package com.haoyou.spring.cloud.alibaba.cultivate.msg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
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
public class PropUseMsg implements Serializable {


    private static final long serialVersionUID = 3809867545634783274L;

    /**
     * 前端传入参数
     */
    private String petUid;
    private String propInstenceUid;
    //道具操作个数
    private int propCount;
    //操作类型
    private int type;

    /**
     * 内部传递使用
     */
    private Prop prop;
    private User user;
}
