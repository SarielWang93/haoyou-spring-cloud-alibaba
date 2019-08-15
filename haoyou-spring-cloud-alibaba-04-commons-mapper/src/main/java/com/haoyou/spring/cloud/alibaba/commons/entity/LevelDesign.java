package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
@Table(name = "level_design")
public class LevelDesign implements Serializable {
    private static final long serialVersionUID = 7889798222299820933L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 章节名称
     */
    @Column(name = "chapter_name")
    private String chapterName;

    /**
     * 关卡编号
     */
    @Column(name = "id_num")
    private Integer idNum;

    private String name;

    private String l10n;

    private String description;

    /**
     * 基础等级
     */
    @Column(name = "base_level")
    private Integer baseLevel;

    /**
     * 普通首胜奖励
     */
    @Column(name = "ordinary_first_award")
    private String ordinaryFirstAward;

    /**
     * 普通奖励
     */
    @Column(name = "ordinary_award")
    private String ordinaryAward;

    /**
     * 困难首胜奖励
     */
    @Column(name = "difficulty_first_award")
    private String difficultyFirstAward;

    /**
     * 困难奖励
     */
    @Column(name = "difficulty_award")
    private String difficultyAward;

    /**
     * 疯狂首胜奖励
     */
    @Column(name = "crazy_first_award")
    private String crazyFirstAward;

    /**
     * 疯狂奖励
     */
    @Column(name = "crazy_award")
    private String crazyAward;

    /**
     * 怪物1
     */
    @Column(name = "pet_type1")
    private String petType1;

    /**
     * 怪物1等级
     */
    @Column(name = "pet_level1")
    private Integer petLevel1;

    /**
     * 怪物2
     */
    @Column(name = "pet_type2")
    private String petType2;

    /**
     * 怪物2等级
     */
    @Column(name = "pet_level2")
    private Integer petLevel2;

    /**
     * 怪物3
     */
    @Column(name = "pet_type3")
    private String petType3;

    /**
     * 怪物3等级
     */
    @Column(name = "pet_level3")
    private Integer petLevel3;

}
