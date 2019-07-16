package com.haoyou.spring.cloud.alibaba.pojo.cultivate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape.Tetromino;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: wanghui
 * @Date: 2019/5/13 14:00
 * @Version 1.0
 */
@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class PetUpLevMsg implements Serializable {

    private static final long serialVersionUID = 3142197269533216894L;

    private String petUid;

}
