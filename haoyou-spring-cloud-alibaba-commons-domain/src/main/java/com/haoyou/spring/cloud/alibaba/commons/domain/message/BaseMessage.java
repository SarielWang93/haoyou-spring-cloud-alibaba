package com.haoyou.spring.cloud.alibaba.commons.domain.message;


import lombok.Data;

import java.io.Serializable;

/**
 * 信息基类
 */
@Data
public class BaseMessage implements Serializable {


    private static final long serialVersionUID = -4373103763918267983L;
    /**
     * 信息状态
     */
    protected Integer state;



}
