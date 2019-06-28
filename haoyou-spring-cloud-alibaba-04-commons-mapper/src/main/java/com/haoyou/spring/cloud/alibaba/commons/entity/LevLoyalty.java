package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
@Table(name = "lev_loyalty")
public class LevLoyalty {
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
     * 1/2星宠物需要食材
     */
    private Integer ingredients12;

    @Column(name = "ingredients12_sum")
    private Integer ingredients12Sum;

    /**
     * 3/4星宠物需要食材
     */
    private Integer ingredients34;

    @Column(name = "ingredients34_sum")
    private Integer ingredients34Sum;

    /**
     * 5/6星宠物需要食材
     */
    private Integer ingredients56;

    @Column(name = "ingredients56_sum")
    private Integer ingredients56Sum;

    /**
     * 食材星级
     */
    @Column(name = "ingredients_star")
    private Integer ingredientsStar;

}
