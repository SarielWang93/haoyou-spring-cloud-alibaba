package com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.current;

import cn.hutool.core.util.RandomUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.FightingStateAction;
import org.springframework.stereotype.Service;

/**
 * 复活
 */
@Service
public class Revive extends FightingStateAction {
    @Override
    public void setStateType() {
        this.actionType= StateType.REVIVE;
    }

    @Override
    public void excut(FightingState fightingState, FightingPet fightingPet) {
        try {
            if(fightingState.getActionType()==this.actionType){
                //复活概率
                int percent = fightingState.getPercent();
                //复活血量
                int fixed = fightingState.getFixed();

                if(RandomUtil.randomInt(100)<percent){

                    int ft_max_hp = fightingPet.getFt_max_hp();

                    int hp=ft_max_hp*fixed/100;

                    fightingPet.setHp(hp);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
