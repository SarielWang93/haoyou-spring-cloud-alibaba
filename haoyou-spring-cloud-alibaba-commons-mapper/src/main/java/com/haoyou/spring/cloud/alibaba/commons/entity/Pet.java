package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Pet implements Serializable {
    private static final long serialVersionUID = -8413159346985913469L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 种类
     */
    @Column(name = "type_uid")
    private String typeUid;
    @Transient
    private String typeName;
    @Transient
    private Integer typeId;

    private String uid;

    /**
     * 所属用户
     */
    @Column(name = "user_uid")
    private String userUid;

    /**
     * 宠物类型（物攻，法功，肉盾，辅助）
     */
    private Integer type;

    /**
     * 物攻基础值
     */
    private Integer atn;

    /**
     * 物防基础值
     */
    private Integer def;

    /**
     * 速度基础值
     */
    private Integer spd;

    /**
     * 血量基础值
     */
    private Integer hp;

    /**
     * 暴击率基础值
     */
    private Integer luk;

    /**
     * 星级值
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
     * 经验值
     */
    private Integer exp;

    /**
     * 升级所需经验值
     */
    @Column(name = "lev_up_exp")
    private Integer levUpExp;


    /**
     * 等级
     */
    private Integer level;

    /**
     * 忠诚度
     */
    private Integer loyalty;

    /**
     * 食材
     */
    private Integer ingredients;


    /**
     * 上阵位置（123），未上阵（0）
     */
    private Integer iswork;


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
     * 其他技能uid
     */
    @Transient
    private List<PetSkill> otherSkill;


    /**
     * 既能配置对象
     */
    @Column(name = "skill_board_josn")
    private byte[] skillBoardJosn;



    /**
     * 创建时间
     */
    @Column(name = "creat_date")
    private Date creatDate;

    /**
     * 创建时间
     */
    @Column(name = "last_update_date")
    private Date lastUpdateDate;


    /**
     * 通过技能uid获取中间链接类
     * @param skillUid
     * @return
     */
    public PetSkill getBySkillUid(String skillUid){
        for(PetSkill petSkill:this.otherSkill){
            if(petSkill.getSkillUid().equals(skillUid)){
                return petSkill;
            }
        }
        return null;
    }

}