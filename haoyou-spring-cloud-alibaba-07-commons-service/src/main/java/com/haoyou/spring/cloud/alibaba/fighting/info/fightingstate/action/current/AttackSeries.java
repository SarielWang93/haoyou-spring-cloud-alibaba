package com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.current;

import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.FightingStateAction;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 连续遭受伤害
 */
@Service
public class AttackSeries extends FightingStateAction {
    private static final long serialVersionUID = 7539084425231054166L;

    @Override
    public void setStateType() {
        this.actionType= StateType.ATTACK_SERIES;
    }

    private int count=0;

    @Override
    public void excut(FightingState fightingState, FightingPet fightingPet) {
        try {
            if(fightingState.getActionType().equals(this.actionType)){
                count++;
                //连击次数
                int fixed = fightingState.getFixed();

                if(fixed>count){

                    //百分比
                    int percent = fightingState.getPercent();

                    //最终伤害
                    int mainHurtR = fightingPet.getAcceptHurtR();
                    //伤害
                    int mainHurt = fightingPet.getAcceptHurt();

                    int hurt = mainHurt*percent/100;

                    //下次攻击
                    fightingPet.setAcceptHurt(hurt);
                    fightingPet.hurt();



                    fightingPet.setAcceptHurt(mainHurt);
                    fightingPet.setAcceptHurtR(mainHurtR);

                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
