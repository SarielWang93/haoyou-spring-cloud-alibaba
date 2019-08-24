package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Fund implements Serializable {
    private static final long serialVersionUID = -8925274109108599033L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 中文名
     */
    private String l10n;

    /**
     * 奖励
     */
    @Column(name = "award_type")
    private String awardType;

    /**
     * 基金时长
     */
    private Integer days;

    /**
     * 截止时间
     */
    @Column(name = "over_time")
    private Date overTime;

    /**
     * 描述
     */
    private String description;

    /**
     * 价格
     */
    private Integer price;


    /**
     * 回报率
     */
    @Column(name = "response_rate")
    private Integer responseRate;


}
