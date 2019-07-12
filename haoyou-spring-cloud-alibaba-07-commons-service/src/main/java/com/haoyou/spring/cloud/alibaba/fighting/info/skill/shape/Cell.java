package com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape;

import cn.hutool.core.clone.CloneSupport;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * @Author: wanghui
 * @Date: 2019/5/10 10:51
 * @Version 1.0
 */
@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class Cell extends CloneSupport<Cell> {
    /*
     * 俄罗斯方块中最小单位
     */
    private int x; //行号
    private int y; //列号

    public Cell() {
    }

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    //小方块向左移动
    public void left(int h){
        this.x-=h;
    }

    //小方块向下移动
    public void drop(int h){
        this.y-=h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return x == cell.x &&
                y == cell.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }


}
