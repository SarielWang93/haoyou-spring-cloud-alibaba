package com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.current;

import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.FightingStateAction;
import org.springframework.stereotype.Service;

/**
 * 血量状态操作
 *
 */
@Service
public class Blood extends FightingStateAction {

    @Override
    public void setStateType() {
        this.actionType=StateType.BLOOD;
    }

    @Override
    public void excut(FightingState fightingState, FightingPet fightingPet) {
        try {
            if(fightingState.getActionType()==this.actionType){

                int percent = fightingState.getPercent();

                if(percent>0)
                //回血
                {
                    Integer ft_max_hp = fightingPet.getFt_max_hp();

                    int cureValue= ft_max_hp*percent/100;

                    fightingPet.cure(cureValue);
                }
                //掉血
                else if(percent<0){
                    Integer hp = fightingPet.getHp();

                    int cureValue= hp*percent/100;

                    fightingPet.cure(cureValue);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
