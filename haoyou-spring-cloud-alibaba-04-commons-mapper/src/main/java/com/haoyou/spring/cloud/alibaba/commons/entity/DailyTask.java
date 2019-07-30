package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
@Table(name = "daily_task")
public class DailyTask implements Serializable {
    private static final long serialVersionUID = 8589855292815772034L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String l10n;

    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 数值
     */
    @Column(name = "numerical_name")
    private String numericalName;

    /**
     * 目标
     */
    private Long aim;

    /**
     * 奖励
     */
    @Column(name = "award_type")
    private String awardType;

    /**
     * 完成奖励的完成度积分
     */
    private Long integral;


}
