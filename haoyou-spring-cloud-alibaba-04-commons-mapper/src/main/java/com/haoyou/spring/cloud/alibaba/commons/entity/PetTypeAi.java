package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "pet_type_ai")
@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class PetTypeAi implements Serializable {
    private static final long serialVersionUID = 7505303166600851629L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 宠物种类uid
     */
    @Column(name = "pet_type_uid")
    private String petTypeUid;

    /**
     * 攻击权重
     */
    private Integer attack;

    /**
     * 特殊攻击权重
     */
    @Column(name = "special_attack")
    private Integer specialAttack;

    /**
     * 护盾权重
     */
    private Integer shield;

    /**
     * 技能权重
     */
    private Integer skill;

}