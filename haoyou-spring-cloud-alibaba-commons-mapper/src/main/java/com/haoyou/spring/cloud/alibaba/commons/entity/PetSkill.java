package com.haoyou.spring.cloud.alibaba.commons.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
public class PetSkill implements Serializable {
    private static final long serialVersionUID = 2736079752257864138L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 宠物uid
     */
    @Column(name = "pet_uid")
    private String petUid;

    /**
     * 技能uid
     */
    @Column(name = "skill_uid")
    private String skillUid;


}