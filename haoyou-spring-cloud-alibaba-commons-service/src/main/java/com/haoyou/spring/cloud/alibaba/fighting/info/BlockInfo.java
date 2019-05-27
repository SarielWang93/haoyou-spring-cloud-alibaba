package com.haoyou.spring.cloud.alibaba.fighting.info;

import cn.hutool.core.clone.CloneSupport;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(value = {},ignoreUnknown = true)
public class BlockInfo extends CloneSupport<BlockInfo> {


    private int x;
    private int y;

    private int randomID;
    //增益效果
    private int upType;
    //减益效果
    private int disType;

    public BlockInfo(int x, int y, int randomID) {
        this.x = x;
        this.y = y;
        this.randomID = randomID;
    }

    public BlockInfo() {
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s",x,y,randomID,upType,disType);
    }
}
