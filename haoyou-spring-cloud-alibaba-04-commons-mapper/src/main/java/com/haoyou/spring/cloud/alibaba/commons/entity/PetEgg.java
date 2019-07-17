package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
@Table(name = "pet_egg")
public class PetEgg implements Serializable {
    private static final long serialVersionUID = 3073519645263582057L;
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

    /**
     * 5星概率%
     */
    private Integer star5;

    /**
     * 4星概率%
     */
    private Integer star4;

    /**
     * 产出宠物类型
     */
    @Column(name = "pet_type")
    private String petType;

    /**
     * 是否卡池
     */
    @Column(name = "is_pool")
    private String isPool;


}
