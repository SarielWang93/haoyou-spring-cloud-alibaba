package com.haoyou.spring.cloud.alibaba.sofabolt.protocol;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {},ignoreUnknown = true)
public class RedisObjKV implements Serializable {


    private static final long serialVersionUID = -864804696900877336L;



    private String key;

    private byte[] val;

    public RedisObjKV(String key, byte[] val) {
        this.key = key;
        this.val = val;
    }

    public RedisObjKV() {
    }
}
