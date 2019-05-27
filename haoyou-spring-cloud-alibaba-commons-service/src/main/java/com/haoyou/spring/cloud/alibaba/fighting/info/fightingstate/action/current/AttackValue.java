package com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.current;

import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.FightingStateAction;
import org.springframework.stereotype.Service;

/**
 * 输出伤害值操作
 */
@Service
public class AttackValue extends FightingStateAction {
    private static final long serialVersionUID = 5825772768198076961L;

    @Override
    public void setStateType() {
        this.actionType = StateType.ATTACKVAL;
    }

    @Override
    public void excut(FightingState fightingState, FightingPet fightingPet) {
        try {
            if(fightingState.getActionType().equals(this.actionType)){

                //最终伤害
                int attack = fightingPet.getAttack();

                //百分比
                Integer percent = fightingState.getPercent();
                //固定值
                Integer fixed = fightingState.getFixed();

                //百分比操作
                if (percent != null && percent != 0) {
                    attack += attack * percent / 100;
                }
                //固定值操作
                else if (fixed != null && fixed != 0) {
                    attack += fixed;
                }
                fightingPet.setAttack(attack);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
