package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class PetSkill implements Serializable {
    private static final long serialVersionUID = 2736079752257864138L;

    public PetSkill() {
    }

    public PetSkill(String petUid, String skillUid) {
        this.petUid = petUid;
        this.skillUid = skillUid;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PetSkill petSkill = (PetSkill) o;
        return Objects.equals(id, petSkill.id) &&
                Objects.equals(petUid, petSkill.petUid) &&
                Objects.equals(skillUid, petSkill.skillUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, petUid, skillUid);
    }
}