package com.haoyou.spring.cloud.alibaba.commons.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
public class SkillResout implements Serializable {
    private static final long serialVersionUID = 8012219678970270017L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uid;

    /**
     * 技能uid
     */
    @Column(name = "skill_uid")
    private String skillUid;

    /**
     * 技能效果uid
     */
    @Column(name = "resout_uid")
    private String resoutUid;


}