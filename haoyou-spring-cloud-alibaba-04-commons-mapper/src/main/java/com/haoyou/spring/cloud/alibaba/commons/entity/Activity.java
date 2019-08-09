package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Activity implements Serializable {
    private static final long serialVersionUID = -6100492815798718271L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String l10n;

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
     * 活动类型：
     *
     * 天天充值  DailyRecharge
     * 累计充值  AccumulatedRecharge
     * 单笔充值  SingleRecharge
     */
    @Column(name = "activity_type")
    private String activityType;

    /**
     * 刷新模式（每日1,每周7，每月30）
     * 如果要设定每两天刷新，则2+100,102
     */
    private Integer refresh;

    /**
     * 预设启用
     */
    @Column(name = "preset_enabled")
    private Integer presetEnabled;

    @Transient
    private List<ActivityAward> activityAwards;



    @Transient
    private boolean current;


}
