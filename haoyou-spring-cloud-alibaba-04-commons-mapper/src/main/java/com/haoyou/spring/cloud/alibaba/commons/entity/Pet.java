package com.haoyou.spring.cloud.alibaba.commons.entity;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
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
     * 培养等级
     */
    private Integer culture;

    /**
     * 培养上限提升值
     */
    @Column(name = "culture_limit")
    private Integer cultureLimit;

    /**
     * 培养结果
     */
    @Column(name = "culture_resoult")
    private Double cultureResoult;

    /**
     * 经验值
     */
    private Long exp;

    /**
     * 升级所需经验值
     */
    @Column(name = "lev_up_exp")
    private Long levUpExp;


    /**
     * 等级
     */
    private Integer level;

    /**
     * 忠诚度
     */
    @Column(name = "loyalty_lev")
    private Integer loyaltyLev;

    /**
     * 食材1
     */
    @Column(name = "ingredients_name1")
    private String ingredientsName1;
    @Column(name = "ingredients_count1")
    private Integer ingredientsCount1;
    @Column(name = "ingredients_attr1")
    private String ingredientsAttr1;
    @Transient
    private Integer ingredientsPieces1;

    /**
     * 食材2
     */
    @Column(name = "ingredients_name2")
    private String ingredientsName2;
    @Column(name = "ingredients_count2")
    private Integer ingredientsCount2;
    @Column(name = "ingredients_attr2")
    private String ingredientsAttr2;
    @Transient
    private Integer ingredientsPieces2;

    /**
     * 食材3
     */
    @Column(name = "ingredients_name3")
    private String ingredientsName3;
    @Column(name = "ingredients_count3")
    private Integer ingredientsCount3;
    @Column(name = "ingredients_attr3")
    private String ingredientsAttr3;
    @Transient
    private Integer ingredientsPieces3;

    /**
     * 食材4
     */
    @Column(name = "ingredients_name4")
    private String ingredientsName4;
    @Column(name = "ingredients_count4")
    private Integer ingredientsCount4;
    @Column(name = "ingredients_attr4")
    private String ingredientsAttr4;
    @Transient
    private Integer ingredientsPieces4;


    /**
     * 状态
     */
    @Column(name = "status")
    private Integer status;

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
    @Column(name = "skill_board")
    private byte[] skillBoard;


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

    public Pet() {
    }

    /**
     * 根据用户和宠物种类生成宠物对象
     *
     * @param user
     * @param petType
     */
    public Pet(User user, PetType petType, int iswork) {
        this.uid = IdUtil.simpleUUID();
        this.atn = petType.getAtn();
        this.atnGr = petType.getAtnGr();
        this.def = petType.getDef();
        this.defGr = petType.getDefGr();
        this.hp = petType.getHp();
        this.hpGr = petType.getHpGr();
        this.typeUid = petType.getUid();
        this.culture = 0;
        this.cultureLimit = 0;
        this.cultureResoult = 0d;
        this.userUid = user.getUid();
        this.type = petType.getType();
        this.spd = petType.getSpd();
        this.luk = petType.getLuk();
        this.starClass = petType.getStarClass();

        this.iswork = iswork;

        this.inhSkill = petType.getInhSkill();
        this.uniqueSkill = petType.getUniqueSkill();
        this.talentSkill = petType.getTalentSkill();
        this.specialAttack = petType.getSpecialAttack();
        this.skillBoard = petType.getSkillBoard();
        this.exp = 0l;
        this.levUpExp = 260l;
        this.level = 1;
        this.loyaltyLev = 0;


        this.ingredientsName1 = petType.getIngredientsName1();
        this.ingredientsAttr1 = petType.getIngredientsAttr1();
        this.ingredientsCount1 = 0;
        this.ingredientsName2 = petType.getIngredientsName2();
        this.ingredientsAttr2 = petType.getIngredientsAttr2();
        this.ingredientsCount2 = 0;
        this.ingredientsName3 = petType.getIngredientsName3();
        this.ingredientsAttr3 = petType.getIngredientsAttr3();
        this.ingredientsCount3 = 0;
        this.ingredientsName4 = petType.getIngredientsName4();
        this.ingredientsAttr4 = petType.getIngredientsAttr4();
        this.ingredientsCount4 = 0;


        this.nickname = petType.getL10n();
        this.creatDate = new Date();

    }

    /**
     * 通过技能uid获取中间链接类
     *
     * @param skillUid
     * @return
     */
    public PetSkill getBySkillUid(String skillUid) {
        for (PetSkill petSkill : this.otherSkill) {
            if (petSkill.getSkillUid().equals(skillUid)) {
                return petSkill;
            }
        }
        return null;
    }

    /**
     * 刷新食材条数
     */
    public void initIngredientsPieces() {
        for (int i = 1; i < 5; i++) {
            Integer count = (Integer) ReflectUtil.getFieldValue(this, String.format("ingredientsCount%s", i));

            int needCount = 0;

            for (int pieces = 1; ; pieces++) {
                needCount += (pieces / 20 + 2) * 5;
                if (needCount > count) {
                    ReflectUtil.setFieldValue(this, String.format("ingredientsPieces%s", i), pieces);
                    break;
                }
            }
        }
    }

    /**
     * 根据条数算出食材当前条吃满所需
     *
     * @return
     */
    public int piecesNeedCount(int pieces) {
        int needCount = 0;

        for (int i = 1; i <= pieces; i++) {
            needCount += (i / 20 + 2) * 5;
        }

        return needCount;
    }

    /**
     * 食材总数
     *
     * @return
     */
    public int allIngredientsCount() {
        return this.ingredientsCount1 + this.ingredientsCount2 + this.ingredientsCount3 + this.ingredientsCount4;

    }

}
