package com.haoyou.spring.cloud.alibaba.fighting.fightingstate.action.current;

import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.fighting.Info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.fighting.fightingstate.action.FightingStateAction;
import org.springframework.stereotype.Service;

/**
 * 伤害结果操作
 */
@Service
public class HurtValue extends FightingStateAction {
    @Override
    public void setStateType() {
        this.actionType= StateType.HURTVAL;
    }

    @Override
    public void excut(FightingState fightingState, FightingPet fightingPet) {
        try {
            if(fightingState.getActionType()==this.actionType){

                //最终伤害
                int acceptHurt = fightingPet.getAcceptHurt();

                //百分比
                Integer percent = fightingState.getPercent();
                //固定值
                Integer fixed = fightingState.getFixed();

                //百分比操作
                if(percent!=null&&percent!=0){
                    acceptHurt += acceptHurt*percent/100;
                }
                //固定值操作
                else if(fixed!=null&&fixed!=0){
                    acceptHurt+=fixed;
                }
                fightingPet.setAcceptHurt(acceptHurt);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
