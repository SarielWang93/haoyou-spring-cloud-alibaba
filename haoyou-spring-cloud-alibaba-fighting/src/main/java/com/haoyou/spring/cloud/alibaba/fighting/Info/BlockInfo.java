package com.haoyou.spring.cloud.alibaba.fighting.Info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(value = {},ignoreUnknown = true)
public class BlockInfo {


    private int x;
    private int y;

    private int randomID;

    public BlockInfo(int x, int y, int randomID) {
        this.x = x;
        this.y = y;
        this.randomID = randomID;
    }

    public BlockInfo() {
    }
}
