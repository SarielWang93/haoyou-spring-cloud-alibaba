package com.haoyou.spring.cloud.alibaba.fighting.fightingstate.action;

import com.haoyou.spring.cloud.alibaba.fighting.Info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.fightingstate.FightingState;
import lombok.Data;

import javax.annotation.PostConstruct;

/**
 * 状态执行抽象类，所有状态执行方法都必须继承此类
 */

@Data
public abstract class FightingStateAction {

    //状态类型，注册码
    protected Integer actionType;

    @PostConstruct
    protected void init(){
        //设置状态种类
        setStateType();
        //状态action注册
        FightingState.putFightingStateAction(this.actionType,this);
    }

    public abstract void setStateType();

    /**
     * 执行方法
     */
    public abstract void excut(FightingState fightingState,FightingPet fightingPet);
}
