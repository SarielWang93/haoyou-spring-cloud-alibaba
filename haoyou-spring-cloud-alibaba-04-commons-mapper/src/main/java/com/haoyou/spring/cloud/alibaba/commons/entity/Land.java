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
public class Land implements Serializable {
    private static final long serialVersionUID = 5059341530768693469L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    private String uid;

    /**
     * 玩家uid
     */
    @Column(name = "user_uid")
    private String userUid;

    private Integer level;

    /**
     * 缩减时长次数，每次1.5%
     */
    @Column(name = "reduction_time")
    private Integer reductionTime;

    /**
     * 增加产出次数，每次2.5%
     */
    @Column(name = "increase_output")
    private Integer increaseOutput;

    /**
     * 种子唯一标识
     */
    @Column(name = "seed_uid")
    private String seedUid;

    /**
     * 种子类型
     */
    @Column(name = "seed_type")
    private String seedType;

    /**
     * 农作物类型
     */
    @Column(name = "crop_type")
    private String cropType;

    /**
     * 农作物中文名
     */
    @Column(name = "crop_l10n")
    private String cropL10n;

    /**
     * 种子星级
     */
    @Column(name = "seed_star")
    private Integer seedStar;

    /**
     * 作物成熟时间
     */
    @Column(name = "planting_time")
    private Date plantingTime;


    /**
     * 作物产出数量
     */
    @Column(name = "crop_count")
    private Integer cropCount;

    /**
     * 种地宠物
     */
    @Column(name = "pet_uid")
    private String petUid;

    /**
     * 当前作物被偷个数
     */
    @Column(name = "being_stolen")
    private Integer beingStolen;

    /**
     * 开始种植时间
     */
    @Column(name = "start_time")
    private Date startTime;



}
