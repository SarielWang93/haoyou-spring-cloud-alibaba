package com.haoyou.spring.cloud.alibaba.fighting.info;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class FightingStep implements Serializable {
    private static final long serialVersionUID = 2458313594609241783L;

    public final static int DO_ATTACK = 0;//普攻发动

    public final static int DO_SKILL = 1;//技能发动

    public final static int ADD_STATE = 2;//添加状态

    public final static int DO_STATE = 3;//状态执行

    public final static int REMOVE_STATE = 4;//状态移除

    public final static int HURT = 5;//受伤

    public final static int DIE = 6;//死亡

    public final static int ATTACK = 7;//输出伤害

    public final static int CURE = 8;//血量变化

    public final static int SHIELD = 9;//增加护盾值

    public final static int ROUND = 10;//回合开始

    public final static int SKIP = 11;//跳过回合

    public final static int VICTORY = 12;//胜利

    public final static int BLOCK_CHANGE = 13;//块改变

    private int stepType;

    private String stepMsg;

    private boolean left;

    private int fightingPetIsWork;

    public FightingStep() {
    }

    public FightingStep(int stepType, FightingPet fightingPet, String stepMsg) {
        this.stepType = stepType;
        this.stepMsg = stepMsg;
        this.fightingPetIsWork = fightingPet.getIswork();
        this.left = fightingPet.getFightingCamp().isLeft();
    }
}
