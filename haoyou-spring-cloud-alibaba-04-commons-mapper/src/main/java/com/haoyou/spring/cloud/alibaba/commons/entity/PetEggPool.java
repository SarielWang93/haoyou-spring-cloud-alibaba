package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
@Table(name = "pet_egg_pool")
public class PetEggPool implements Serializable {
    private static final long serialVersionUID = 6340306820526520427L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 蛋的池
     */
    @Column(name = "egg_id")
    private Integer eggId;

    /**
     * 宠物模板uid
     */
    @Column(name = "pet_type_uid")
    private String petTypeUid;

    /**
     * 宠物星级
     */
    @Column(name = "star_class")
    private Integer starClass;

    /**
     * 概率%
     */
    private Integer probability;

    /**
     * 权重
     */
    private Double weights;


}
