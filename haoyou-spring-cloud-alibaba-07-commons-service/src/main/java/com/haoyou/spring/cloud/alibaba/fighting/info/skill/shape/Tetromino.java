package com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wanghui
 * @Date: 2019/5/10 10:54
 * @Version 1.0
 */
@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Tetromino implements Serializable {
    private static final long serialVersionUID = 9209587284511104508L;

    protected static Map<String, Class<? extends Tetromino>> all = new HashMap<>();

    static {
        all.put("T", T.class);
        all.put("Z", Z.class);
        all.put("O", O.class);
        all.put("I", I.class);
        all.put("J", J.class);
        all.put("L", L.class);
        all.put("S", S.class);
        all.put("X", X.class);
    }


    /**
     * 随机生成一种图形
     */
    public static Tetromino randomOne() {
        Tetromino t = null;
        int num = RandomUtil.randomInt(7);//生成0~6随机数，来生成7种不同的随机方块
        switch (num) {
            case 0:
                t = new T();
                break;
            case 1:
                t = new Z();
                break;
            case 2:
                t = new O();
                break;
            case 3:
                t = new I();
                break;
            case 4:
                t = new J();
                break;
            case 5:
                t = new L();
                break;
            case 6:
                t = new S();
                break;
        }
        return t;
    }

    /**
     * 获取 一种图形
     *
     * @param type
     * @return
     */
    public static Tetromino get(String type) {


        Tetromino t = null;
        try {
            t = all.get(type).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return t;
    }


    //组合的方块都是有最小单位的方块组成的
    protected Cell[] cells;
    //存储最初位置的
    protected Cell[] baseCells;
    //种类
    protected String type;

    //存储最初的位置和方向
    public void baseC() {
        baseCells = new Cell[cells.length];
        for (int i = 0; i < cells.length; i++) {
            baseCells[i] = cells[i].clone();
        }
    }

    //方块向左移动
    public void moveLeft(int h) {
        for (Cell c : cells) {
            c.left(h);
        }
    }

    //方块向下移动
    public void moveDrop(int h) {
        for (Cell c : cells) {
            c.drop(h);
        }
    }


    /**
     * 根据坐标校验形状
     *
     * @return
     */
    public String checkType() {

        if(this.cells.length == 1){
            return "X";
        }

        //移动到左下角
        leftAndDrop();

        int i = 0;
        do {

            for (Class<? extends Tetromino> aClass : Tetromino.all.values()) {
                Tetromino tetromino = null;
                try {
                    tetromino = aClass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (this.equals(tetromino)) {
                    return tetromino.type;
                }
            }
            this.rotateRight();
            i++;
        } while (i < 4);
        return null;

    }

    /**
     * 移动到左下角
     */
    public void leftAndDrop() {
        Cell c = cells[0];
        int minx = c.getX();
        int miny = c.getY();
        for (int i = 1; i < cells.length; i++) {
            Cell o = cells[i];
            if (o.getX() < minx) {
                minx = o.getX();
            }
            if (o.getY() < miny) {
                miny = o.getY();
            }
        }
        //移动到左下角
        moveLeft(minx);
        moveDrop(miny);
    }

    /**
     * 顺时针旋转90度
     */
    public void rotateRight() {
        Cell c = cells[0];

        for (int i = 1; i < 4; i++) {
            Cell o = cells[i];
            this.rotateRightOneCell(c, o);
        }
        leftAndDrop();

    }

    /**
     * 点o绕点c顺时针旋转90度
     *
     * @param c
     * @param o
     */
    private void rotateRightOneCell(Cell c, Cell o) {
        int rx0 = c.getX();
        int ry0 = c.getY();
        int x = o.getX();
        int y = o.getY();

        int x0 = rx0 - (y - ry0);
        int y0 = ry0 + (x - rx0);

        o.setX(x0);
        o.setY(y0);

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(Tetromino.class.isAssignableFrom(o.getClass()))) return false;
        Tetromino tetromino = (Tetromino) o;
        boolean noDifferent = true;
        for (Cell c : this.cells) {
            boolean noC = true;
            for (Cell d : tetromino.cells) {
                if (c.equals(d)) {
                    noC = false;
                }
            }
            if (noC) {
                noDifferent = false;
            }

        }

        return noDifferent;
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(cells);
    }
}
