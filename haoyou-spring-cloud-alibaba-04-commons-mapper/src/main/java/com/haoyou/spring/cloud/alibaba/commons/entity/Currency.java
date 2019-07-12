package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {"userUid"}, ignoreUnknown = true)
public class Currency implements Serializable {
    private static final long serialVersionUID = 5753446568150586341L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_uid")
    private String userUid;

    /**
     * 金币
     */
    private Integer coin;

    /**
     * 钻石
     */
    private Integer diamond;

    /**
     * 体力
     */
    private Integer vitality;

    /**
     * 宠物升级经验
     */
    @Column(name = "pet_exp")
    private Long petExp;

    /**
     * 包裹道具栏个数
     */
    @Column(name = "prop_max")
    private Integer propMax;

    /**
     * 宠物数量最大值
     */
    @Column(name = "pet_max")
    private Integer petMax;

    /**
     * 道具（json存储）
     */
    private byte[] props;

}
