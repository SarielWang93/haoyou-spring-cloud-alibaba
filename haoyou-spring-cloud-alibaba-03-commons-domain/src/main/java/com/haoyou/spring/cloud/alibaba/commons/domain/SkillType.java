package com.haoyou.spring.cloud.alibaba.commons.domain;

public class SkillType {

    /**
     * 被动
     */


    public final static int OVERALL=0;//全局被动（登录时，改变面板属性）

    public final static int OPENING=1;//开局被动（进入战斗时执行）


    /**
     * 攻击
     */

    public final static int ATTACK_PASSIVE=3;//攻击被动（消除普通攻击块时执行，其他攻击或技能可调用普攻）

    public final static int SPECIAL_ATTACK=4;//特殊攻击（消除特殊攻击块时执行）

    public final static int UNIQUE = 5;//必杀技（能量值满时，消除普通块执行）


    /**
     * 主动技能
     */

    public final static int ACTIVE = 6;//主动技能


    /**
     * attribute_type技能所属类型
     */

    public final static int PASSIVE = 99;//被动

    public final static int ATTECK = 7;//攻击

    public final static int CURE = 8;//治疗

    public final static int DEFENSE = 9;//防御

    public final static int ASSIST = 10;//辅助


    /**
     * 种植
     */
    public final static int PLANTING = 11;//
}
