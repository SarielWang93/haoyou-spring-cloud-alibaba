package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
public class Resout implements Serializable {
    private static final long serialVersionUID = -2627638396160058717L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 名称
     */
    private String name;

    private String uid;

    /**
     * 作用人数类型（单人，多人（固定几人，属性影响））
     */
    @Column(name = "num_type")
    private Integer numType;

    /**
     * 成功率类型（100%，不是100%（固定概率，属性影响））
     */
    @Column(name = "rate_type")
    private Integer rateType;

    /**
     * 影响类型（攻击，回复，复活，状态（冰冻，麻痹，中毒，虚弱））
     */
    @Column(name = "state_uid")
    private String stateUid;


    /**
     * 技能效果uid
     */
    @Transient
    private State state;


}