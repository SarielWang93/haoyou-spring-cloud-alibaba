package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "version_control")
@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class VersionControl implements Serializable {
    private static final long serialVersionUID = -1003467842480104746L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uid;

    /**
     * 版本号
     */
    private String version;

    /**
     * 版本补丁地址
     */
    private String url;

    /**
     * 创建时间
     */
    private Date date;

    /**
     * 描述
     */
    @Column(name = "`describe`")
    private String describe;


}