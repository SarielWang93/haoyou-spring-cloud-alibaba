package com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.current;

import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.FightingStateAction;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 伤害结算之后触发，溅射伤害逻辑
 */
@Service
public class SplashDamage extends FightingStateAction {
    private static final long serialVersionUID = 3601327540885432923L;

    @Override
    public void setStateType() {
        this.actionType= StateType.SPLASH_DAMAGE;
    }

    @Override
    public void excut(FightingState fightingState, FightingPet fightingPet) {
        try {
            if(fightingState.getActionType().equals(this.actionType)){


                List<FightingPet> alive = fightingPet.getFightingCamp().getAlive();
                int aliveSize = alive.size();

                //最终伤害
                int mainHurt = fightingPet.getAcceptHurt();




                //百分比
                int percent = fightingState.getPercent();

                int hurt = mainHurt*percent/100;

                //溅射范围
                int fixed = fightingState.getFixed();

                if(aliveSize<fixed){
                    fixed=aliveSize;
                }

                for(int i=0;i<fixed;i++){

                    FightingPet fightingPeti = alive.get(i);

                    if(!fightingPeti.getUid().equals(fightingPet.getUid())){
                        int acceptHurt = fightingPeti.getAcceptHurt();
                        fightingPeti.setAcceptHurtFrom(fightingPet.getAcceptHurtFrom());
                        fightingPeti.setAcceptHurt(acceptHurt+hurt);
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
