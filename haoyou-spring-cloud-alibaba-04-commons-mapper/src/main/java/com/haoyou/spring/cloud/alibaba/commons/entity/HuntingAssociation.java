package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
@Table(name = "hunting_association")
public class HuntingAssociation implements Serializable {
    private static final long serialVersionUID = 1904627303743480014L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String l10n;

    private String description;

    /**
     * 目标值
     */
    private Long aim;

    @Column(name = "award_type")
    private String awardType;

    /**
     * 等级编号
     */
    @Column(name = "id_num")
    private Integer idNum;


}
