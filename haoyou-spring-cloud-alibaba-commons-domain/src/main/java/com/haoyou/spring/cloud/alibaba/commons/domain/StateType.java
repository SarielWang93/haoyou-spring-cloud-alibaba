package com.haoyou.spring.cloud.alibaba.commons.domain;

public class StateType {


    /**
     * 触发类型
     */
    public final static int REFRESH_FT =9;//每次刷新战斗属性时触发
    public final static int TURN_START =1;//回合开始触发
    public final static int ADD_STATE =2;//添加状态时触发
    public final static int FORTHWITH=3;//直接执行一次(技能效果)

    //攻击相关触发
    public final static int DAMAGE_CALCULATION_ATTACK =4;//普通攻击输出伤害时
    public final static int DAMAGE_CALCULATION_SUFFER =5;//最终伤害结算时
    public final static int DAMAGE_CALCULATION_HURT =6;//结算伤害之后触发
    //特殊触发
    public final static int KILL=7;//击杀时触发
    public final static int DEATH=8;//死亡时触发


    public final static int THRESHOLD_UP=10;//血量触发 >阈值
    public final static int THRESHOLD_DOWN=11;//血量触发 <阈值



    public final static int TURN_START_BLOCK =12;//回合开始操作块



    /**
     * 操作类型
     */
    //属性操作
    public final static int ATTR_UP =100;
    //免疫状态
    public final static int IMMUNE =101;
    //输出伤害值操作
    public final static int ATTACKVAL=102;
    //伤害结果值操作
    public final static int HURTVAL=103;
    //伤害结果值操作
    public final static int SPLASH_DAMAGE=104;

    public final static int BOLOCK=105;//块操作（升级，冰冻）

    public final static int BLOOD=106;//血操作（治疗，伤害）

    public final static int ATTACK_SERIES=107;//连续攻击


    public final static int REVIVE=108;//复活



    /**
     * 清除逻辑根据deleteType的值控制状态删除
     */
    public final static int ROUND=200;//回合制
    public final static int NEVER=201;//一直存在
    public final static int TEMPORARY=202;//当前操作执行结束

    public final static int ONECE=203;//只执行一次后

    public final static int HURT=204;//受到伤害
    public final static int OPERATION=205;//操作块后


    public final static int THRESHOLD_UP_DELETE=209;//血量删除 >阈值
    public final static int THRESHOLD_DOWN_DELETE=210;//血量删除 <阈值
}
