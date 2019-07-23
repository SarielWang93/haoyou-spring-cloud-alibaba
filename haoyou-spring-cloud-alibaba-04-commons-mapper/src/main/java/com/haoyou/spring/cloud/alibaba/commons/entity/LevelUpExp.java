package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
@Table(name = "level_up_exp")
public class LevelUpExp implements Serializable {
    private static final long serialVersionUID = -6723029437314454292L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 升级所需经验
     */
    @Column(name = "up_lev_exp")
    private Long upLevExp;

}
