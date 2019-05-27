package com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.Resout;
import com.haoyou.spring.cloud.alibaba.commons.entity.State;
import com.haoyou.spring.cloud.alibaba.commons.util.SpringTool;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.action.FightingStateAction;
import lombok.Data;

import javax.persistence.Transient;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

@Data
@JsonIgnoreProperties({"fightingStateAction"})

public class FightingState implements Serializable {

    private static final long serialVersionUID = -168397358755040914L;


    /**
     * action注册机
     */
    private static Map<Integer, FightingStateAction> fightingStateActionHashMap = new HashMap<>();

    /**
     * 注册方法
     *
     * @param type
     * @param fightingStateAction
     */
    public static void putFightingStateAction(Integer type, FightingStateAction fightingStateAction) {
        fightingStateActionHashMap.put(type, fightingStateAction);
    }

    /**
     * 状态操作对象
     */
    private FightingStateAction fightingStateAction;


    private String uid;


    //来自user
    private String fromUserUid;
    //来自pet
    private String fromPetUid;


    /**
     * 消除块的数量
     */
    private Integer blockCount;

    /**
     * 技能品质
     */
    private Integer quality;


    /**
     * 作用人数类型（单人，多人（固定几人，属性影响）负数表示对敌技能，正数表示对己方技能）
     */
    private Integer numType;

    /**
     * 成功率类型（100%，不是100%（固定概率，属性影响））
     */
    private Integer rateType;


    /**
     * 状态类型
     */
    private Integer type;

    /**
     * 操作类型
     */
    private Integer actionType;

    /**
     * 删除逻辑控制（可以多条以空格分开），1永久生效，2临时生效（本操作），3直接生效一次
     */
    private ArrayList<Integer> deleteType;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String describe;


    /**
     * 血量阈值百分比
     */
    private Integer bloodThreshold;

    /**
     * 影响属性（FightingPet对象中的属性）
     */
    private String infAttr;

    /**
     * 影响百分比
     */
    private Integer percent;

    /**
     * 固定数值
     */
    private Integer fixed;

    /**
     * 影响回合数
     */
    private Integer round;

    /**
     * 操作类全名
     */
    private String actionName;

    /**
     * 状态效果uid
     */
    @Transient
    private List<Resout> resouts;



    /**
     * 并执行操作类
     *
     * @param fightingPet
     */
    public void doAction(FightingPet fightingPet) {

        try {

            if (this.fightingStateAction == null) {
                //从数据库配置取class
                if (StrUtil.isNotEmpty(this.getActionName())) {
                    //通过类名从Spring中获取对象
                    this.fightingStateAction = SpringTool.getBean(this.actionName);
                }
            }
            if (this.fightingStateAction == null) {
                //从注册机中获取class
                this.fightingStateAction = FightingState.fightingStateActionHashMap.get(this.actionType);
            }
            if(this.fightingStateAction == null){
                return;
            }
//            this.fightingStateAction.setFightingPet(fightingPet);
//            this.fightingStateAction.setFightingState(this);
            //执行操作
            this.fightingStateAction.excut(this,fightingPet);

            /**
             * 状态可以产生结果
             */
            if(this.resouts.size()>0){
                for (Resout resout : this.resouts) {
                    fightingPet.resoutDo(resout,this.quality,this.blockCount);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 技能生成战斗状态
     *
     * @param resout
     * @param fightingPet
     * @param blockCount  主动技能的消除块的数量
     * @return
     */
    public static FightingState creatFightingState(Resout resout, int quality, FightingPet fightingPet, Integer blockCount) {
        FightingState fightingState = new FightingState();

        fightingState.setBlockCount(blockCount);
        fightingState.setQuality(quality);
        fightingState.setFromUserUid(fightingPet.getPet().getUserUid());
        fightingState.setFromPetUid(fightingPet.getUid());

        fightingState.setNumType(resout.getNumType());
        fightingState.setRateType(resout.getRateType());

        State state = resout.getState();
        fightingState.setUid(state.getUid());
        fightingState.setType(state.getType());
        fightingState.setActionType(state.getActionType());

        if(StrUtil.isNotEmpty(state.getDeleteType())){

            ArrayList<String> strings = CollUtil.newArrayList(state.getDeleteType().split(" "));

            ArrayList<Integer> dts=new ArrayList<>();
            for(String dt:strings){
                dts.add(Integer.parseInt(dt));
            }

            fightingState.setDeleteType(dts);
        }


        fightingState.setName(state.getName());
        fightingState.setDescribe(state.getDescribe());
        fightingState.setBloodThreshold(state.getBloodThreshold());
        fightingState.setInfAttr(state.getInfAttr());
        fightingState.setPercent(state.getPercent());
        fightingState.setFixed(state.getFixed());
        fightingState.setRound(state.getRound());
        fightingState.setActionName(state.getActionName());

        fightingState.setResouts(state.getResouts());

        Class<? extends State> stateClass = state.getClass();
        Class<? extends FightingState> FightingStateClass = fightingState.getClass();

        /**
         * 与消除的块无关时质量利用块的字段可多操作一个参数
         */
        if(blockCount==0&&quality>0){
            blockCount=quality+1;
        }
        /**
         * 处理消除影响
         */
        if (blockCount > 1) {
            try {

                //获取处理属性以及，消除对应块数的操作值
                Field eliminateField = stateClass.getDeclaredField(String.format("eliminate%s", blockCount));

                eliminateField.setAccessible(true);
                //影响数值
                Integer eliminateAffect = (Integer) eliminateField.get(state);

                //影响属性（百分比，数值，回合数）
                String eliminateAffectAttrStr = state.getEliminateAttr();
                if(StrUtil.isNotEmpty(eliminateAffectAttrStr)) {
                    //操作处理属性，设置值为块数对应值
                    Field eliminateAffectField = FightingStateClass.getDeclaredField(eliminateAffectAttrStr);
                    eliminateAffectField.setAccessible(true);
                    eliminateAffectField.set(fightingState, eliminateAffect);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        /**
         * 处理品质影响
         */
        if (quality > 0) {
            try {
                Field qualityField = stateClass.getDeclaredField(String.format("quality%s", quality));

                qualityField.setAccessible(true);
                //影响数值
                Integer qualityAffect = (Integer) qualityField.get(state);
                //影响属性（百分比，数值，回合数）
                String qualityAffectAttrStr = state.getQualityAttr();

                if(StrUtil.isNotEmpty(qualityAffectAttrStr)){
                    //操作处理属性，设置值为块数对应值
                    Field qualityAffectField = FightingStateClass.getDeclaredField(qualityAffectAttrStr);
                    qualityAffectField.setAccessible(true);
                    qualityAffectField.set(fightingState, qualityAffect);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        return fightingState;
    }

    public static FightingState creatFightingState(Resout resout, int quality, FightingPet fightingPet) {
        return creatFightingState(resout, quality, fightingPet, 0);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FightingState that = (FightingState) o;
        return Objects.equals(uid, that.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }
}
