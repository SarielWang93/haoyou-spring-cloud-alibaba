package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
@Table(name = "achievement_aims")
public class AchievementAims implements Serializable {
    private static final long serialVersionUID = -5486023068682763003L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "achievement_id")
    private Integer achievementId;

    /**
     * 优先级
     */
    @Column(name = "priority_order")
    private Integer priorityOrder;

    /**
     * 目标
     */
    private Long aim;

    /**
     * 奖励
     */
    @Column(name = "award_type")
    private String awardType;


}
