package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Numerical implements Serializable {
    private static final long serialVersionUID = 7170050058782607895L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String l10n;

    /**
     * 描述
     */
    private String description;

}
