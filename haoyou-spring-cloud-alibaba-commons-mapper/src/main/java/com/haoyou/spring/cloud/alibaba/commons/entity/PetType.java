package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class PetType implements Serializable {
    private static final long serialVersionUID = -1696392797076441734L;
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

    private String uid;

    /**
     * 宠物类型（物攻，法功，肉盾，辅助，远程，近战）
     */
    private Integer type;

    /**
     * 物攻初始基础值
     */
    private Integer atn;

    /**
     * 物防初始基础值
     */
    private Integer def;

    /**
     * 速度初始基础值
     */
    private Integer spd;

    /**
     * 血量初始基础值
     */
    private Integer hp;

    /**
     * 暴击率初始基础值
     */
    private Integer luk;

    /**
     * 星级初始基础值
     */
    @Column(name = "star_class")
    private Integer starClass;


    /**
     * 攻击成长率
     */
    @Column(name = "atn_gr")
    private Integer atnGr;

    /**
     * 防御成长率
     */
    @Column(name = "def_gr")
    private Integer defGr;

    /**
     * 血量成长率
     */
    @Column(name = "hp_gr")
    private Integer hpGr;

    /**
     * 固有技能（主动）
     */
    @Column(name = "inh_skill")
    private String inhSkill;

    /**
     * 必杀技
     */
    @Column(name = "unique_skill")
    private String uniqueSkill;

    /**
     * 天赋技能（被动）
     */
    @Column(name = "talent_skill")
    private String talentSkill;


    /**
     * 特殊攻击
     */
    @Column(name = "special_attack")
    private String specialAttack;

    /**
     * 既能配置对象
     */
    @Column(name = "skill_board_josn")
    private String skillBoardJosn;


    /**
     * 其他技能uid
     */
    @Transient
    private PetTypeAi petTypeAi;

}