package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Commodity implements Serializable {
    private static final long serialVersionUID = -3078155850278791524L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 中文名称
     */
    private String l10n;

    /**
     * 描述
     */
    private String description;

    /**
     * 所在商店名称（
     *  单品限购：SingleProductPurchase
     *  特惠礼包：SpecialPackage
     *  充值：Recharge
     *  ）
     */
    @Column(name = "store_name")
    private String storeName;

    /**
     * 限购范围（每日1,每周7，每月30）
     */
    private Integer refresh;

    /**
     * 限购次数
     */
    @Column(name = "refresh_times")
    private Integer refreshTimes;

    /**
     * 花费类型（diamond，coin，rmb）
     */
    @Column(name = "spend_type")
    private String spendType;

    /**
     * 价格
     */
    private Integer price;

    /**
     * 奖励
     */
    @Column(name = "award_type")
    private String awardType;


    /**
     * 上架
     */
    private Integer shelf;


}
