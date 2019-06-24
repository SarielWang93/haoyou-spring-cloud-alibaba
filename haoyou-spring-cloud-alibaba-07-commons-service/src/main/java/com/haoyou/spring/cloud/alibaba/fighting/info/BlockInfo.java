package com.haoyou.spring.cloud.alibaba.fighting.info;

import cn.hutool.core.clone.CloneSupport;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {},ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class BlockInfo extends CloneSupport<BlockInfo> implements Serializable {
    private static final long serialVersionUID = -1469748303950691453L;


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
