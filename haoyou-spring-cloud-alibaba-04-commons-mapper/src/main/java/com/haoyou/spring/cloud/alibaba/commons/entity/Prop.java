package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Prop implements Serializable {
    private static final long serialVersionUID = 592449855996734073L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uid;

    /**
     * 中文名
     */
    private String l10n;

    /**
     * 道具名称
     */
    private String name;

    /**
     * 字段灵活使用
     */
    private String property1;

    /**
     * 字段灵活使用
     */
    private String property2;

    /**
     * 字段灵活使用
     */
    private String property3;

    /**
     * 字段灵活使用
     */
    private String property4;

    /**
     * 字段灵活使用
     */
    private String property5;

    /**
     * 道具描述
     */
    @Column(name = "`describe`")
    private String describe;

    /**
     * 字段使用描述
     */
    @Column(name = "property_describe")
    private String propertyDescribe;



    /**
     * 道具数量
     */
    @Transient
    private int count;
    /**
     * 道具编号
     */
    @Transient
    private String propInstenceUid;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prop prop = (Prop) o;
        return Objects.equals(id, prop.id) &&
                Objects.equals(uid, prop.uid) &&
                Objects.equals(l10n, prop.l10n) &&
                Objects.equals(name, prop.name) &&
                Objects.equals(property1, prop.property1) &&
                Objects.equals(property2, prop.property2) &&
                Objects.equals(property3, prop.property3) &&
                Objects.equals(property4, prop.property4) &&
                Objects.equals(property5, prop.property5) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uid, l10n, name, property1, property2, property3, property4, property5);
    }
}
