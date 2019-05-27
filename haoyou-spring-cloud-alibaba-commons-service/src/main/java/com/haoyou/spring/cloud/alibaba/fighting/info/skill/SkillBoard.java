package com.haoyou.spring.cloud.alibaba.fighting.info.skill;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape.Cell;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape.Tetromino;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: wanghui
 * @Date: 2019/5/9 15:07
 * @Version 1.0
 *
 * 存储宠物的既能配置，内各节点存储的是技能uid
 * 以"NoDeletion:"开头的是禁止删除的
 */
@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class SkillBoard implements Serializable {
    private static final long serialVersionUID = -4006118858412392029L;


    private String[][] board;
    private int xLength;
    private int yLength;

    public SkillBoard() {
    }

    public SkillBoard(int x, int y) {
        this.xLength = x;
        this.yLength = y;
        this.board = new String[x][y];
    }

    /**
     * 增加技能
     *
     * @param tetromino 坐标
     * @param prop      道具
     * @return
     */
    public boolean addSkill(Tetromino tetromino, Prop prop) {
        if (tetromino == null || prop == null) {
            return false;
        }
        //不可以放入已有技能
        for (int x = 0; x < xLength; x++) {
            for (int y = 0; y < yLength; y++) {
                if (prop.getProperty1().equals(board[x][y])) {
                    return false;
                }
            }
        }

        tetromino.baseC();
        String tType = tetromino.checkType();
        if (tType.equals(prop.getProperty2())) {

            //检查是否可放
            for (Cell c : tetromino.getBaseCells()) {
                if (StrUtil.isNotEmpty(board[c.getX()][c.getY()])) {
                    return false;
                }
            }
            for (Cell c : tetromino.getBaseCells()) {
                board[c.getX()][c.getY()] = prop.getProperty1();
                //board[c.getX()][c.getY()] = String.format("%s:%s",prop.getProperty1(),prop.getPropInstenceUid());
            }
            return true;

        }

        return false;
    }

    /**
     * 删除技能
     *
     * @param tetromino
     */
    public String removeSkill(Tetromino tetromino) {
        tetromino.baseC();
        //获取技能uid
        Cell baseCell = tetromino.getBaseCells()[0];
        String skillUid=board[baseCell.getX()][baseCell.getY()];
        //检查是否可以删除
        if(StrUtil.isEmpty(skillUid)||skillUid.startsWith("NoDeletion:")){
            return null;
        }
        //检查是否一致
        for (Cell c : tetromino.getBaseCells()) {
            if (!skillUid.equals(board[c.getX()][c.getY()])) {
                return null;
            }
        }
        //检查是否有剩余
        int count=0;
        for (int x = 0; x < xLength; x++) {
            for (int y = 0; y < yLength; y++) {
                if (skillUid.equals(board[x][y])) {
                    count++;
                }
            }
        }
        if(count!=tetromino.getBaseCells().length){
            return null;
        }
        for (Cell c : tetromino.getBaseCells()) {
            board[c.getX()][c.getY()]=null;
            return skillUid;
        }


        return null;
    }

    /**
     * 打印出技能设置盘
     */
    public void print() {
        for (int y = yLength-1; y >= 0; y--) {
            for (int x = 0; x < xLength; x++) {
                if(StrUtil.isNotEmpty(board[x][y])){
                    Console.print(board[x][y].toCharArray()[0]);
                }else{
                    Console.print(0);
                }
                Console.print(" ");
            }
            Console.log("");
        }

    }
}
