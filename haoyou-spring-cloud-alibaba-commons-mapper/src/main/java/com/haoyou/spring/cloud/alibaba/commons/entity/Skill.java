package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Skill implements Serializable {
    private static final long serialVersionUID = -3107489438999542079L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uid;

    /**
     * 技能名称
     */
    private String name;
    private String l10n;
    /**
     * 技能类型（主动，被动（全局，进入战斗，每回合，攻击触发，特殊触发））
     */
    private Integer type;

    /**
     * 描述
     */
    @Column(name = "`describe`")
    private String describe;

    /**
     * 技能所属类型（治疗，攻击，防御，辅助）
     */
    @Column(name = "attribute_type")
    private Integer attributeType;

    /**
     * 技能品质
     */
    private Integer quality;

    /**
     * 技能效果uid
     */
    @Transient
    private List<Resout> resouts;


}