package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

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
    private Integer exp;

    /**
     * 升级所需经验
     */
    @Column(name = "up_lev_exp")
    private Long upLevExp;


}
