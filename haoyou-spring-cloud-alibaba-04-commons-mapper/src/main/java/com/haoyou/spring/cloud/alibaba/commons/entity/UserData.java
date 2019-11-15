package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@JsonIgnoreProperties(value = {"userUid"}, ignoreUnknown = true)
@Table(name = "user_data")
public class UserData implements Serializable {
    private static final long serialVersionUID = -5089780981559996922L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_uid")
    private String userUid;

    /**
     * 昵称
     */
    private String name;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 经验
     */
    private Long exp;

    /**
     * 升级所需经验
     */
    @Column(name = "up_lev_exp")
    private Long upLevExp;

    /**
     * 每日签到
     */
    @Column(name = "daily_check_in")
    private byte[] dailyCheckIn;


    /**
     * 购买月卡时间
     */
    @Column(name = "monthly_card_date")
    private Date monthlyCardDate;


    @Column(name = "monthly_card_award")
    private String monthlyCardAward;

    /**
     * 购买至尊月卡时间
     */
    @Column(name = "monthly_card_extreme_date")
    private Date monthlyCardExtremeDate;


    @Column(name = "monthly_card_extreme_award")
    private String monthlyCardExtremeAward;


    /**
     * 购买的基金
     */
    private byte[] funds;


    /**
     * 邮件
     */
    private byte[] emails;

    /**
     * 徽章
     */
    private byte[] badges;


    /**
     * 购买终身饲养员时间
     */
    @Column(name = "lifetime_breeder_date")
    private Date lifetimeBreederDate;

    /**
     * 助战宠物uid
     */
    @Column(name = "help_pet_uid")
    private String helpPetUid;


    /**
     * 种植系统等级
     */
    @Column(name = "planting_system_level")
    private Integer plantingSystemLevel;

}
