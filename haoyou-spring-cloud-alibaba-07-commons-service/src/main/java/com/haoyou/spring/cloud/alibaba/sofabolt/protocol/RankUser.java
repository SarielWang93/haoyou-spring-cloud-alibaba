package com.haoyou.spring.cloud.alibaba.sofabolt.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wanghui
 * @version 1.0
 */

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class RankUser implements Serializable {

    private String userUid;

    private String name;

    private String avatar;

    private Integer integral;


}
