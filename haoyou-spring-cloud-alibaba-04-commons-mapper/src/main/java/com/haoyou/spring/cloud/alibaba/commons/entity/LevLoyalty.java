package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
@Table(name = "lev_loyalty")
public class LevLoyalty implements Serializable {
    private static final long serialVersionUID = -9213398173415378122L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 忠诚
     */
    @Column(name = "loyalty_lev")
    private Integer loyaltyLev;

    /**
     * 等级上限
     */
    @Column(name = "level_max")
    private Integer levelMax;

    /**
     * 需要食材
     */
    private Integer ingredients;

    @Column(name = "ingredients_sum")
    private Integer ingredientsSum;


}
