package com.haoyou.spring.cloud.alibaba.fighting.info;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;

import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * 战斗阵营信息
 */
@Data
@JsonIgnoreProperties(value = {"fightingRoom", "acceptStates", "acceptHurt1", "acceptHurt2", "acceptHurtFrom"}, ignoreUnknown = true)
public class FightingCamp implements Serializable {

    private static final long serialVersionUID = 1848450392442510724L;
    public static final int MAX_ENERGY=20;

    private FightingRoom fightingRoom;


    private User user;

    //位置：是否左边
    private boolean left = false;

    //是否自动
    private boolean ai = false;

    // key:位置
    private TreeMap<Integer, FightingPet> fightingPets;

    //能量值
    private int energy;

    //接收到的状态
    private List<FightingState> acceptStates;

    //接收到的伤害来源
    private FightingPet acceptHurtFrom;

    //接收到的伤害1
    private int acceptHurt;


    public FightingCamp() {
        this.acceptStates = new ArrayList<>();
        this.acceptHurt = 0;
        this.energy = 0;
    }

    /**
     * 获取活着的宠物，按照顺序
     *
     * @return
     */
    public List<FightingPet> getAlive() {
        List<FightingPet> sufferFightingPets = new ArrayList<>();
        for (FightingPet fightingPet : fightingPets.values()) {
            if (fightingPet.getHp() > 0) {
                sufferFightingPets.add(fightingPet);
            }
        }
        return sufferFightingPets;
    }

}
