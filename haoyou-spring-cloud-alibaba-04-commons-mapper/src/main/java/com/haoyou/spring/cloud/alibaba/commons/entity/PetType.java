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

    private String description;

    private String uid;

    /**
     * 种族
     */
    private Integer race;

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
     * 技能盘满的时候加成技能
     */
    @Column(name = "full_skill_board")
    private String fullSkillBoard;

    /**
     * 既能配置对象
     */
    @Column(name = "skill_board")
    private byte[] skillBoard;

    /**
     * 食材1
     */
    @Column(name = "ingredients_name1")
    private String ingredientsName1;
    @Column(name = "ingredients_attr1")
    private String ingredientsAttr1;

    /**
     * 食材2
     */
    @Column(name = "ingredients_name2")
    private String ingredientsName2;
    @Column(name = "ingredients_attr2")
    private String ingredientsAttr2;


    /**
     * 食材3
     */
    @Column(name = "ingredients_name3")
    private String ingredientsName3;
    @Column(name = "ingredients_attr3")
    private String ingredientsAttr3;


    /**
     * 食材4
     */
    @Column(name = "ingredients_name4")
    private String ingredientsName4;
    @Column(name = "ingredients_attr4")
    private String ingredientsAttr4;


    /**
     * 其他技能uid
     */
    @Transient
    private PetTypeAi petTypeAi;

}
