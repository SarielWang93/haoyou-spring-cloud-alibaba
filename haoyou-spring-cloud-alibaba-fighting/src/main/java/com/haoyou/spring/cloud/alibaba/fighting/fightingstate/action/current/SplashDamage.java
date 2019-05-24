package com.haoyou.spring.cloud.alibaba.fighting.fightingstate.action.current;

import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.fighting.Info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.fighting.fightingstate.action.FightingStateAction;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 伤害结算之后触发，溅射伤害逻辑
 */
@Service
public class SplashDamage extends FightingStateAction {
    @Override
    public void setStateType() {
        this.actionType= StateType.SPLASH_DAMAGE;
    }

    @Override
    public void excut(FightingState fightingState, FightingPet fightingPet) {
        try {
            if(fightingState.getActionType()==this.actionType){


                List<FightingPet> alive = fightingPet.getFightingCamp().getAlive();
                int aliveSize = alive.size();

                //最终伤害
                int mainHurt = fightingPet.getAcceptHurt();




                //百分比
                int percent = fightingState.getPercent();

                int hurt = mainHurt*percent/100;

                //溅射范围
                int fixed = fightingState.getFixed();

                if(aliveSize<fixed+1){
                    fixed=aliveSize;
                }

                for(int i=0;i<fixed;i++){

                    FightingPet fightingPeti = alive.get(i + 1);

                    int acceptHurt = fightingPeti.getAcceptHurt();
                    fightingPeti.setAcceptHurtFrom(fightingPet.getAcceptHurtFrom());
                    fightingPeti.setAcceptHurt(acceptHurt+hurt);

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
