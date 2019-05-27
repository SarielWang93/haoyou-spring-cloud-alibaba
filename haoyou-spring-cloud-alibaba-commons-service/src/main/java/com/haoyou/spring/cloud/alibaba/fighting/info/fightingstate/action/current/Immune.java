package com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.current;

import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.FightingStateAction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 免疫状态操作
 *
 */
@Service
public class Immune extends FightingStateAction {
    private static String NAME_START="免疫";
    @Override
    public void setStateType() {
        this.actionType=StateType.IMMUNE;
    }

    @Override
    public void excut(FightingState fightingState, FightingPet fightingPet) {
        try {
            if(fightingState.getActionType()==this.actionType){

                //获取免疫对象名称
                String name = fightingState.getName();
                String rname="";
                if(name.startsWith(NAME_START)){
                    rname=name.split(NAME_START)[1];
                }
                if(StrUtil.isEmpty(rname)){
                    return;
                }

                //删除，免疫对象
                List<FightingState> remove = new ArrayList<>();
                for (FightingState fightingState1 : fightingPet.getFightingStates()) {
                    if (fightingState1.getName().contains(rname)&&!fightingState1.getName().startsWith(NAME_START)) {
                        remove.add(fightingState1);
                    }
                }
                fightingPet.removeFightingState(remove);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
