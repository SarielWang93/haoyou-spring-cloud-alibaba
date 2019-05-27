package com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.current;

import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.FightingStateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;

/**
 * 属性值操作类状态，通用类
 * 通过数据库配置实现对不同属性的增减
 */
@Service
public class AttrUp extends FightingStateAction {
    private final static Logger logger = LoggerFactory.getLogger(AttrUp.class);
    @Override
    public void setStateType() {
        this.actionType=StateType.ATTR_UP;
    }

    @Override
    public void excut(FightingState fightingState, FightingPet fightingPet) {
        try {
            if(fightingState.getActionType()==this.actionType){
                /**
                 * 反射处理fightingPet属性
                 */
                Class<? extends FightingPet> fightingPetClass = fightingPet.getClass();

                String infAttr = fightingState.getInfAttr();
                int bloodThreshold = fightingState.getBloodThreshold();

                //百分比
                Integer percent = fightingState.getPercent();
                //固定值
                Integer fixed = fightingState.getFixed();

                //行动权操作
                if("action_time".equals(infAttr)){
                    fightingPet.setAction_time(fixed);
                    return;
                }



                //根据影响属性获取属性字段
                Field mbField = fightingPetClass.getDeclaredField(String.format("mb_%s", infAttr));
                Field ftField = fightingPetClass.getDeclaredField(String.format("ft_%s", infAttr));

                mbField.setAccessible(true);
                ftField.setAccessible(true);

                //获取面板以及战斗属性值
                Integer mbAttr = (Integer)mbField.get(fightingPet);
                Integer ftAttr = (Integer)ftField.get(fightingPet);


                int up = 0;
                //百分比操作
                if(percent!=null&&percent!=0){
                    up = mbAttr*percent/100;
                }
                //固定值操作
                else if(fixed!=null&&fixed!=0){
                    up=fixed;
                }
                //根据血量阈值正负分辨改变值与血量的正相关还是负相关
                if(bloodThreshold!=0){
                    Integer hp = fightingPet.getHp();
                    Integer ft_max_hp = fightingPet.getFt_max_hp();
                    Integer bl=hp*100/ft_max_hp;
                    if(bloodThreshold>0){
                        up*=bl/100;
                    }else{
                        up-=up*bl/100;
                    }
                }
                ftAttr+=up;

                //修改属性值
                ftField.set(fightingPet,ftAttr);
                //logger.debug(String.format("宠物属性：%s %s %s %s %s",fightingPet.getPet().getNickname(),infAttr,mbAttr,up,ftField.get(fightingPet)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
