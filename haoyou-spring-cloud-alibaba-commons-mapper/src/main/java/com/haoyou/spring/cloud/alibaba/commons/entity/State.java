package com.haoyou.spring.cloud.alibaba.commons.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
public class State implements Serializable {
    private static final long serialVersionUID = 1602416243494461450L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String uid;

    /**
     * 状态类型
     */
    private Integer type;

    /**
     * 操作类型
     */
    @Column(name = "action_type")
    private Integer actionType;

    /**
     * 删除逻辑控制
     */
    @Column(name = "delete_type")
    private String deleteType;

    /**
     * 名称
     */
    private String name;
    private String l10n;

    /**
     * 描述
     */
    @Column(name = "`describe`")
    private String describe;


    /**
     * 血量阈值百分比
     */
    @Column(name = "blood_threshold")
    private Integer bloodThreshold;


    /**
     * 影响属性（FightingPet对象中的属性）
     */
    @Column(name = "inf_attr")
    private String infAttr;

    /**
     * 影响百分比
     */
    private Integer percent;

    /**
     * 固定数值
     */
    private Integer fixed;

    /**
     * 影响回合数
     */
    private Integer round;

    /**
     * 操作类全名
     */
    @Column(name = "action_name")
    private String actionName;

    /**
     * 主动消除块数影响的属性（百分比，数值，回合数）
     */
    @Column(name = "eliminate_attr")
    private String eliminateAttr;

    /**
     * 主动消除2个块的处理
     */
    private Integer eliminate2;

    /**
     * 主动消除3个块的处理
     */
    private Integer eliminate3;

    /**
     * 主动消除4个块的处理
     */
    private Integer eliminate4;

    /**
     * 主动消除5个块的处理
     */
    private Integer eliminate5;

    /**
     * 主动消除6个块的处理
     */
    private Integer eliminate6;

    /**
     * 主动消除7个以上块的处理
     */
    private Integer eliminate7;


    /**
     * 技能品质影响的属性（百分比，数值，回合数）
     */
    @Column(name = "quality_attr")
    private String qualityAttr;

    /**
     * 1级品质处理
     */
    private Integer quality1;

    /**
     * 2级品质处理
     */
    private Integer quality2;

    /**
     * 3级品质处理
     */
    private Integer quality3;

    /**
     * 4级品质处理
     */
    private Integer quality4;

    /**
     * 状态效果uid
     */
    @Transient
    private List<Resout> resouts;

}