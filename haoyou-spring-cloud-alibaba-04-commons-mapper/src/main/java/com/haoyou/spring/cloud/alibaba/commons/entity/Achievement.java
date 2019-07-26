package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Achievement implements Serializable {
    private static final long serialVersionUID = -3330871641916870673L;
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
     * 成就目标节点
     */
    @Transient
    private List<AchievementAims> achievementAims;

}
