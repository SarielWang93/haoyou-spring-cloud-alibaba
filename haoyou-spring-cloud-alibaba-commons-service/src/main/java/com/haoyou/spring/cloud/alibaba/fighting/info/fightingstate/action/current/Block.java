package com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.current;

import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingBoard;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingCamp;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.FightingStateAction;
import org.springframework.stereotype.Service;

/**
 * 块操作
 *
 */
@Service
public class Block extends FightingStateAction {

    @Override
    public void setStateType() {
        this.actionType=StateType.BOLOCK;
    }

    @Override
    public void excut(FightingState fightingState, FightingPet fightingPet) {
        try {
            if(fightingState.getActionType()==this.actionType){
                //当前阵营
                FightingCamp fightingCamp = fightingPet.getFightingCamp();
                //棋盘
                FightingBoard fightingBoard = fightingPet.getFightingCamp().getFightingRoom().getFightingBoard();

                //冻结1，升级2
                Integer percent = fightingState.getPercent();
                //数量
                Integer fixed = fightingState.getFixed();
                //块种类，0为所有快
                Integer round = fightingState.getRound();
                //修改块属性
                fightingBoard.changeBlock(round,fixed,percent,fightingPet);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
