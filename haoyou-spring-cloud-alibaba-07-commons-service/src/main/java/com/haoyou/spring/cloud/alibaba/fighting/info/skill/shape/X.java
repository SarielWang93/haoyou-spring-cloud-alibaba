package com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape;

/**
 * @Author: wanghui
 * @Date: 2019/5/10 10:56
 * @Version 1.0
 */

public class X extends Tetromino {
    //T型位置
    public X() {
        type="X";
        cells = new Cell[1];
        cells[0] = new Cell(0,0);
    }
}
