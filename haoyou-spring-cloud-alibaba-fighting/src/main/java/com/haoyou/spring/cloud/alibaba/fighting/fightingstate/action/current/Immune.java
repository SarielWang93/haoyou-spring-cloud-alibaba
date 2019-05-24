package com.haoyou.spring.cloud.alibaba.fighting.fightingstate.action.current;

import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.fighting.Info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.fighting.fightingstate.action.FightingStateAction;
import org.springframework.stereotype.Service;

/**
 * 免疫状态操作
 *
 */
@Service
public class Immune extends FightingStateAction {

    @Override
    public void setStateType() {
        this.actionType=StateType.IMMUNE;
    }

    @Override
    public void excut(FightingState fightingState, FightingPet fightingPet) {
        try {
            if(fightingState.getActionType()==this.actionType){

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
