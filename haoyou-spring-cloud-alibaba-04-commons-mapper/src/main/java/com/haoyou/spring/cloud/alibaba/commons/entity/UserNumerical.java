package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
@Table(name = "user_numerical")
public class UserNumerical implements Serializable {
    private static final long serialVersionUID = -9046677954613620672L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_uid")
    private String userUid;

    /**
     * 数值
     */
    @Column(name = "numerical_name")
    private String numericalName;



    private Long value;

}
