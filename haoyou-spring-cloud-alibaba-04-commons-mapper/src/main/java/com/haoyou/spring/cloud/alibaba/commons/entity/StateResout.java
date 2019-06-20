package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class StateResout implements Serializable {
    private static final long serialVersionUID = -8114194949728036265L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uid;

    /**
     * 技能uid
     */
    @Column(name = "state_uid")
    private String stateUid;

    /**
     * 技能效果uid
     */
    @Column(name = "resout_uid")
    private String resoutUid;

}