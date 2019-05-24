package com.haoyou.spring.cloud.alibaba.commons.entity;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
@Data
public class Prop implements Serializable {
    private static final long serialVersionUID = 592449855996734073L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uid;

    /**
     * 道具名称
     */
    private String name;

}