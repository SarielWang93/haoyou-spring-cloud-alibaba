package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
@Table(name = "activity_award")
public class ActivityAward implements Serializable {
    private static final long serialVersionUID = -6868982083947127604L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "activiti_id")
    private Integer activitiId;

    /**
     * 进度
     */
    private Integer schedule;

    /**
     * 目标值
     */
    private Long aim;

    /**
     * 奖励
     */
    @Column(name = "award_type")
    private String awardType;

    /**
     * 次数限制
     */
    private Integer times;


}
