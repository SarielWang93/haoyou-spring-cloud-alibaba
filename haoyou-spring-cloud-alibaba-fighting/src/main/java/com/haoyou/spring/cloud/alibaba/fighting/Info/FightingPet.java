package com.haoyou.spring.cloud.alibaba.fighting.Info;


import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.domain.PunishValue;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.SkillType;
import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;

import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.action.RedisObjectUtil;
import lombok.Data;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 战斗中宠物属性
 */
@Data
@JsonIgnoreProperties(value = {"fightingCamp", "distinguish", "acceptHurtFrom", "acceptHurt", "attack","redisObjectUtil"}, ignoreUnknown = true)
public class FightingPet implements Serializable {

    private static final long serialVersionUID = 9184606707890252699L;
    private RedisObjectUtil redisObjectUtil;
    /**
     * 攻击块修正系数
     */
    private static Map<Integer, Integer> actFactors;
    /**
     * 护盾块修正系数
     */
    private static Map<Integer, Integer> ShieldFactors;

    /**
     * 暴击修正系数
     */
    private static Integer LukFactor;

    static {
        /**
         * 初始化攻击快的修正系数
         */
        actFactors = new HashMap<>();
        actFactors.put(2, -50);
        actFactors.put(3, 0);
        actFactors.put(4, 10);
        actFactors.put(5, 20);
        actFactors.put(6, 30);
        actFactors.put(7, 50);
        /**
         * 初始化护盾块修正系数
         */
        ShieldFactors = new HashMap<>();
        ShieldFactors.put(2, 5);
        ShieldFactors.put(3, 10);
        ShieldFactors.put(4, 12);
        ShieldFactors.put(5, 14);
        ShieldFactors.put(6, 17);
        ShieldFactors.put(7, 20);

        /**
         * 暴击系数
         */
        LukFactor = 150;
    }


    private FightingCamp fightingCamp;
    private Map<String, FightingCamp> distinguish;


    /**
     * 昵称
     */
    private String nickname;
    //uid
    private String uid;

    //种类
    private PetType petType;

    //状态对象
    private List<FightingState> fightingStates;

    //技能
    private List<Skill> skills;

    //出手数
    private Integer shotNum;
    //出手操作记录
    private TreeMap<Integer, FightingReq> shots;


    //操作记录与FightingRoom中的相同，只不过此处只有自己的
    private TreeMap<Integer, FightingStep> steps;

    /**
     * redis中获取宠物战斗对象
     * @param pet
     * @return
     */
    public static FightingPet getFightingPet(Pet pet,RedisObjectUtil redisObjectUtil) {
        String key = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, pet.getUid());
        FightingPet fightingPet = redisObjectUtil.get(key, FightingPet.class);
        fightingPet.setRedisObjectUtil(redisObjectUtil);
        return fightingPet;
    }

    public FightingPet() {
    }

    /**
     * pet初始化
     *
     * @param pet
     */
    public FightingPet(Pet pet,RedisObjectUtil redisObjectUtil) {

        this.redisObjectUtil=redisObjectUtil;

        this.atnGr = pet.getAtnGr();
        this.defGr = pet.getDefGr();
        this.hpGr = pet.getHpGr();

        this.nickname = pet.getNickname();
        this.petType = pet.getPetType();
        this.uid = pet.getUid();
        this.fightingStates = new ArrayList<>();
        this.steps = new TreeMap<>();

        this.iswork = pet.getIswork();
        this.atn = pet.getAtn();


        this.def = pet.getDef();


        this.spd = pet.getSpd();


        this.max_hp = pet.getHp();


        this.ft_shield = 0;

        this.luk = pet.getLuk();

        this.mb_atn = this.atn;
        this.mb_def = this.def;
        this.mb_max_hp = this.max_hp;

        //面板暴击与速度与等级无关
        this.mb_spd = this.spd;
        this.mb_luk = this.luk;

        this.mb_tpc = 100;
        this.ft_tpc = this.mb_tpc;

        refreshMbByLevel(pet.getLevel(), pet.getExp());


        /**
         * 初始化宠物技能
         */

        List<Skill> skills = new ArrayList<>();
        List<String> skillUids = pet.getOtherSkill();
        skillUids.add(pet.getInhSkill());
        skillUids.add(pet.getTalentSkill());
        skillUids.add(pet.getUniqueSkill());
        skillUids.add(pet.getSpecialAttack());
        for (String skillUid : skillUids) {
            if (!StrUtil.isEmpty(skillUid)) {
                String skillKey = RedisKeyUtil.getKey(RedisKey.SKILL, skillUid);
                Skill skill = this.redisObjectUtil.get(skillKey, Skill.class);
                skills.add(skill);
            }
        }

        this.skills=skills;

        //执行全局技能
        this.overAll();
    }

    /**
     * petType初始化
     *
     * @param petType
     * @param isWork  阵型位置
     */
    public FightingPet(PetType petType, Integer isWork, Integer level,RedisObjectUtil redisObjectUtil) {

        this.redisObjectUtil=redisObjectUtil;

        this.atnGr = petType.getAtnGr();
        this.defGr = petType.getDefGr();
        this.hpGr = petType.getHpGr();


        this.nickname = petType.getName();
        this.petType = petType;
        this.uid = IdUtil.simpleUUID();
        this.fightingStates = new ArrayList<>();
        this.steps = new TreeMap<>();

        this.iswork = isWork;
        this.atn = petType.getAtn();


        this.def = petType.getDef();


        this.spd = petType.getSpd();


        this.max_hp = petType.getHp();


        this.ft_shield = 0;

        this.luk = petType.getLuk();

        this.mb_atn = this.atn;
        this.mb_def = this.def;
        this.mb_max_hp = this.max_hp;

        //暴击与速度与等级无关
        this.mb_spd = this.spd;
        this.mb_luk = this.luk;

        this.mb_tpc = 100;
        this.ft_tpc = this.mb_tpc;


        refreshMbByLevel(level, 0);


        List<Skill> skills = new ArrayList<>();
        List<String> skillUids = new ArrayList<>();
        skillUids.add(petType.getInhSkill());
        skillUids.add(petType.getTalentSkill());
        skillUids.add(petType.getUniqueSkill());
        for (String skillUid : skillUids) {
            if (StrUtil.isEmpty(skillUid)) {
                continue;
            }
            String skillKey = RedisKeyUtil.getKey(RedisKey.SKILL, skillUid);
            Skill skill = this.redisObjectUtil.get(skillKey, Skill.class);
            skills.add(skill);
        }
        this.skills=skills;
        //执行全局技能
        this.overAll();
    }

    /**
     * 缓存
     */
    public void save(){
        String key = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, this.uid);
        //redis存储
        this.redisObjectUtil.save(key, this);
    }

    /**
     * 删除缓存
     */
    public void delete(){
        String key = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, this.uid);
        //redis删除
        this.redisObjectUtil.delete(key);
    }


    /**
     * 接收到的伤害（临时存储）
     */
    private int acceptHurt;

    /**
     * 接收到的伤害来源（临时存储）
     */
    private FightingPet acceptHurtFrom;

    /**
     * 输出伤害值（临时存储）
     */
    private int attack;


    /**
     * 经验值
     */
    private Integer exp;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 攻击成长率
     */
    private Integer atnGr;

    /**
     * 防御成长率
     */
    private Integer defGr;

    /**
     * 血量成长率
     */
    private Integer hpGr;


    /**
     * 阵型位置
     */
    private Integer iswork;

    /**
     * 治疗系数（执行治疗操作时需要系数，部分状态会修改系数，如：增加治疗量）
     */
    private Integer mb_tpc;
    private Integer ft_tpc;

    /**
     * 物攻
     */
    private Integer atn;
    private Integer mb_atn;
    private Integer ft_atn;
    /**
     * 物防
     */
    private Integer def;
    private Integer mb_def;
    private Integer ft_def;
    /**
     * 速度
     */
    private Integer punishValue;//惩罚值
    private Integer action_time;//行动权
    private Integer spd;
    private Integer mb_spd;
    private Integer ft_spd;
    /**
     * 最大血量
     */
    private Integer max_hp;
    private Integer mb_max_hp;
    private Integer ft_max_hp;

    /**
     * 护盾值
     */
    private Integer ft_shield;
    /**
     * 当前血量
     */
    private Integer hp;

    /**
     * 暴击率
     */
    private Integer luk;
    private Integer mb_luk;
    private Integer ft_luk;
    private boolean luky;


    /**
     * 根据等级刷新面板属性（经验值主要是用于显示）
     *
     * @param level
     */
    public void refreshMbByLevel(Integer level, Integer exp) {
        //刷新等级和经验
        this.level = level;
        this.exp = exp;


        for (int i = 0; i < level; i++) {
            this.mb_atn += this.atnGr;
            this.mb_def += this.defGr;
            this.mb_max_hp += this.hpGr;
        }


    }


    /**
     * 初始化战斗属性
     */
    public void initFighting() {
        //根据面板数据对战斗数据进行初始化
        this.ft_atn = mb_atn;
        this.ft_def = mb_def;
        this.ft_spd = mb_spd;
        this.ft_max_hp = mb_max_hp;
        this.ft_luk = mb_luk;
        //满血
        this.hp = this.ft_max_hp;
        //护盾归0
        this.ft_shield = 0;
        //刷新状态为空
        this.fightingStates = new ArrayList<>();


        this.shotNum = 0;
        this.shots = new TreeMap<>();

        /**
         * 初始惩罚值为100
         */
        this.reflashActionTime(PunishValue.INITIAL);


    }

    /**
     * 受到治疗,或者掉血
     */
    public void cure(int cureValue) {

        if (cureValue > 0) {
            cureValue *= this.ft_tpc / 100;
        }

        this.hp += cureValue;

        /**
         * 添加步骤
         */

        this.addStep(FightingStep.CURE, String.valueOf(cureValue));


        /**
         * 血量阈值控制
         */
        this.doFightingStateByType(StateType.THRESHOLD_UP);
        this.removeState(StateType.THRESHOLD_UP_DELETE);
    }

    /**
     * 受到伤害
     */
    public boolean hurt() {


        //TODO 伤害逻辑，HP计算
        Integer realHurt = (this.acceptHurt - this.ft_def) < 0 ? 0 : (this.acceptHurt - this.ft_def);

        this.acceptHurt = realHurt;
        /**
         * 承受伤害计算，有关状态（处理伤害值）
         */
        this.doFightingStateByType(StateType.DAMAGE_CALCULATION_SUFFER);

        this.ft_shield -= this.acceptHurt;

        if (this.ft_shield < 0) {
            this.hp += this.ft_shield;
            this.ft_shield = 0;
        }

        if (this.hp < 0) {
            this.hp = 0;
        }


        /**
         * 添加步骤信息
         */
        this.addStep(FightingStep.HURT, String.valueOf(this.acceptHurt));

        //是否死亡
        boolean isDie = false;
        /**
         * 死亡触发状态
         */

        if (this.getHp() == 0) {
            this.doFightingStateByType(StateType.DEATH);

        }
        /**
         * 仍然死亡去除光环（所有自己产生的属性类状态）
         */

        if (this.getHp() < 1) {
            isDie = true;
            for (Map.Entry<String, FightingCamp> entry : fightingCamp.getFightingRoom().getFightingCamps().entrySet()) {
                for (Map.Entry<Integer, FightingPet> e : entry.getValue().getFightingPets().entrySet()) {
                    List<FightingState> attrUps = e.getValue().getFightingStateByType(StateType.REFRESH_FT);
                    List<FightingState> remove = new ArrayList<>();
                    for (FightingState attrUp : attrUps) {
                        String fromPetUid = attrUp.getFromPetUid();
                        if (fromPetUid.equals(this.getUid())) {
                            remove.add(attrUp);
                        }
                    }
                    e.getValue().removeFightingState(remove);
                }
            }
        }


        if (!isDie) {
            //伤害结算之后触发
            this.doFightingStateByType(StateType.DAMAGE_CALCULATION_HURT);
            this.removeState(StateType.HURT);
            /**
             * 血量阈值，触发状态
             */
            this.doFightingStateByType(StateType.THRESHOLD_DOWN);
            this.removeState(StateType.THRESHOLD_DOWN_DELETE);

        }

        //伤害归0
        this.acceptHurt = 0;

        /**
         * 添加步骤信息
         */
        if (isDie) {
            this.addStep(FightingStep.DIE, "");
        }
        return isDie;

    }

    /**
     * 普通攻击
     */
    public void attackAction(int blockCount) {
        //伤害值(初始化)
        this.attack = this.getFt_atn();
        /**
         * 计算块对攻击力影响
         */

        //系数
        Integer factor = this.actFactors.get(blockCount);
        //攻击值
        this.attack += this.mb_atn * factor / 100;

        FightingCamp enemy = this.getDistinguish().get("enemy");
        //执行普攻伤害计算有关状态（状态中处理伤害值）
        this.doFightingStateByType(StateType.DAMAGE_CALCULATION_ATTACK);

        //普攻触发被动执行
        this.skillsDo(SkillType.ATTACK_PASSIVE, blockCount);

        //输出伤害
        if (this.attack > 0) {
            enemy.setAcceptHurtFrom(this);
            // 计算是否暴击(暴击系数待定)
            int random = RandomUtil.randomInt(100);
            if (random < this.ft_luk) {
                this.luky = true;
                this.attack *= LukFactor / 100;
            }else{
                this.luky = false;
            }
            /**
             * 记录步骤
             */
            this.addStep(FightingStep.ATTACK, String.valueOf(this.attack));

            enemy.setAcceptHurt(enemy.getAcceptHurt() + this.attack);
            this.attack = 0;
        }

    }


    /**
     * 增加护盾操作
     */
    public void shieldAction(int blockCount) {
        //护盾系数
        Integer shieldFactor = ShieldFactors.get(blockCount);

        this.ft_shield += this.ft_max_hp * shieldFactor / 100;


        /**
         * 记录步骤
         */
        this.addStep(FightingStep.SHIELD, String.valueOf(this.ft_shield));

    }


    /**
     * 增加护盾操作
     */
    public void kill(List<FightingPet> die) {
        this.doFightingStateByType(StateType.KILL);
    }


    /**
     * 添加状态，判断是否是直接执行的
     *
     * @param fightingState
     */
    public void addFightingState(FightingState fightingState) {
        //判断是否立即执行
        if (fightingState.getType() == StateType.FORTHWITH) {
            this.StateDoAction(fightingState);
        } else {
            /**
             * 检查覆盖逻辑
             */
            boolean hasSame = false;
            List<FightingState> covers= new ArrayList<>();
            if (fightingState.getActionType() == StateType.ATTR_UP) {
                /**
                 * 属性操作覆盖逻辑
                 */
                for (FightingState fightingState1 : this.getFightingStateByType(StateType.REFRESH_FT)) {
                    if (fightingState1.getInfAttr().equals(fightingState.getInfAttr())) {
                        hasSame = true;
                        if (fightingState.getQuality() >= fightingState1.getQuality()) {
                            covers.add(fightingState1);
                            this.fightingStates.add(fightingState);
                        }
                    }
                }
            } else {
                for (FightingState fightingState1 : this.fightingStates) {
                    if (fightingState1.equals(fightingState)) {
                        hasSame = true;
                        if (fightingState.getQuality() >= fightingState1.getQuality()) {
                            covers.add(fightingState1);
                            this.fightingStates.add(fightingState);
                        }
                    }
                }
            }

            this.removeFightingState(covers);

            if (!hasSame) {
                this.fightingStates.add(fightingState);
            }

            //执行免疫
            this.doFightingStateByType(StateType.ADD_STATE);
            /**
             * 添加步骤信息
             */
            this.addStep(FightingStep.ADD_STATE, fightingState.getName()+":"+fightingState.getRound());
        }
    }


    /**
     * 刷新行动权
     *
     * @param punishValue 惩罚值
     */
    public void reflashActionTime(Integer punishValue) {
        this.punishValue = punishValue;
        this.action_time = this.punishValue * 100 / this.ft_spd;
    }


    /**
     * 根据状态加成刷新战斗过程中的属性值
     */
    public void refreshFt() {

        int hurt = this.ft_max_hp - this.hp;

        this.ft_atn = this.mb_atn;
        this.ft_def = this.mb_def;
        this.ft_spd = this.mb_spd;
        this.ft_max_hp = this.mb_max_hp;
        this.ft_luk = this.mb_luk;


        this.doFightingStateByType(StateType.REFRESH_FT);
        this.hp = this.ft_max_hp - hurt;

    }


    /**
     * 刷新回合
     */
    public void reflashRound() {

        /**
         * 添加步骤记录回合开始
         */
        this.addStep(FightingStep.ROUND, "");
        /**
         * 执行回合开始状态
         */
        this.doFightingStateByType(StateType.TURN_START);

        //增加出手数
        this.shotNum++;

        //管理状态回合

        for (FightingState fightingState : this.fightingStates) {
            Integer round = fightingState.getRound();
            if (round > 0) {
                fightingState.setRound(--round);
            }
        }
        this.removeState(StateType.ROUND);
    }


    /**
     * 清理状态，根据round参数清除状态
     */
    public void removeState(int deleteType) {
        List<FightingState> remove = new ArrayList<>();
        for (FightingState fightingState : this.fightingStates) {
            if (deleteType == StateType.ROUND) {
                if (fightingState.getRound() == 0) {
                    remove.add(fightingState);
                }
            } else {
                //实现多处触发删除
                ArrayList<Integer> deleteTypes = fightingState.getDeleteType();
                if (deleteTypes.contains(deleteType)) {
                    remove.add(fightingState);
                }
            }

        }
        this.removeFightingState(remove);
    }


    /**
     * 全局技能生效
     */
    public void overAll() {

        //全局技能生效
        List<Skill> skills = this.getSkillsByType(SkillType.OVERALL);


        for (Skill skill : skills) {
            for (Resout resout : skill.getResouts()) {
                State state = resout.getState();
                FightingState fightingState = FightingState.creatFightingState(resout, skill.getQuality(), this);
                //反射修改面板数值，只有全局技能可以修改面板属性
                try {
                    String infAttr = fightingState.getInfAttr();
                    Field field = this.getClass().getField(infAttr);
                    Integer o = (Integer) field.get(fightingState);
                    Integer ch = 0;
                    Integer fixed = fightingState.getFixed();
                    Integer percent = fightingState.getPercent();
                    if (fixed != null && fixed != 0) {
                        ch = fixed;
                    } else {
                        ch = o * percent / 100;
                    }
                    Field fieldmb = this.getClass().getField(String.format("mb_%s", infAttr));
                    fieldmb.setAccessible(true);
                    Integer mb_o = (Integer) fieldmb.get(this);
                    mb_o += ch;
                    fieldmb.set(this, mb_o);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 技能产生状态（抛出状态给己方或者对方）
     *
     * @param skillType
     * @param blockCount
     */
    public void skillsDo(Integer skillType, Integer blockCount) {

        List<Skill> skills = this.getSkillsByType(skillType);

        for (Skill skill : skills) {
            this.addStep(FightingStep.DO_SKILL, skill.getId().toString());
            for (Resout resout : skill.getResouts()) {
                this.resoutDo(resout, skill.getQuality(), blockCount);
            }
            //技能如果为进攻技能，要执行进攻操作
            if (skill.getAttributeType() == null) {
                skill.setAttributeType(SkillType.PASSIVE);
            }
            if (SkillType.ATTECK == skill.getAttributeType()) {
                this.attackAction(blockCount);
            }
        }
    }

    public void skillsDo(Integer skillType) {
        this.skillsDo(skillType, 0);
    }

    /**
     * 产生状态地结果
     *
     * @param resout
     * @param quality
     * @param blockCount
     */
    public void resoutDo(Resout resout, int quality, Integer blockCount) {

        FightingCamp own = this.getDistinguish().get("own");
        FightingCamp enemy = this.getDistinguish().get("enemy");
        //作用人数
        Integer numType = resout.getNumType();

        //对己方或者对方抛出技能结果
        FightingState fightingState = FightingState.creatFightingState(resout, quality, this, blockCount);


        if (numType > 0) {
            /**
             * 单独对自己产生状态
             */
            if (numType == 1) {
                this.addFightingState(fightingState);
            }
            //对己方所有人产生状态
            else {
                own.getAcceptStates().add(fightingState);
            }
        } else {
            //对其他阵营产生状态
            enemy.getAcceptStates().add(fightingState);
        }
    }


    /**
     * 删除状态
     *
     * @param removeFightingStates
     */
    private void removeFightingState(List<FightingState> removeFightingStates) {

        for (FightingState fightingState : removeFightingStates) {

            this.fightingStates.remove(fightingState);
            /**
             * 添加步骤信息
             */
            this.addStep(FightingStep.REMOVE_STATE, fightingState.getName());

        }
    }

    /**
     * 根据状态类型获取状态
     *
     * @param type
     * @return
     */
    private List<FightingState> getFightingStateByType(Integer type) {
        List<FightingState> rl = new ArrayList<>();
        for (FightingState fightingState : this.fightingStates) {

            if (type >= 100) {
                if (fightingState.getType() > type && fightingState.getType() < type + 100) {
                    rl.add(fightingState);
                }

            } else {
                if (fightingState.getType() == type) {
                    rl.add(fightingState);
                }
            }
        }
        return rl;
    }


    /**
     * 根据type获取技能
     *
     * @param type
     * @return
     */
    private List<Skill> getSkillsByType(Integer type) {
        List<Skill> tSkills = new ArrayList<>();
        for (Skill skill : skills) {
            if (type == skill.getType()) {
                tSkills.add(skill);
            }
        }
        return tSkills;
    }


    /**
     * 根据状态类型执行状态
     *
     * @param type
     * @return
     */
    public void doFightingStateByType(Integer type) {
        for (FightingState fightingState : this.getFightingStateByType(type)) {
            this.StateDoAction(fightingState);
        }
    }

    /**
     * 添加步骤
     *
     * @param stepType
     * @param msg
     */
    public void addStep(int stepType, String msg) {

        FightingStep fightingStep = new FightingStep(stepType, this, msg);


        FightingRoom fightingRoom = this.fightingCamp.getFightingRoom();
        int step = fightingRoom.getStep();
        int newStep = ++step;

        fightingRoom.setStep(newStep);
        fightingRoom.getSteps().put(newStep, fightingStep);
        fightingRoom.getNowSteps().put(newStep, fightingStep);

        this.steps.put(newStep, fightingStep);

    }
    /**
     * 添加出手
     *
     * @param fightingReq
     */
    public void addShot(FightingReq fightingReq) {
        FightingRoom fightingRoom = this.fightingCamp.getFightingRoom();
        int roomShotNum = fightingRoom.getShotNum()+1;
        this.shotNum++;
        fightingRoom.setShotNum(roomShotNum);
        fightingRoom.getShots().put(roomShotNum,fightingReq);
        this.shots.put(this.shotNum,fightingReq);
    }

    /**
     * 执行状态
     *
     * @param fightingState
     */
    private void StateDoAction(FightingState fightingState) {
        if (fightingState.getActionType() != StateType.ATTR_UP) {
            /**
             * 添加步骤信息,排除属性操作状态
             */
            this.addStep(FightingStep.DO_STATE, fightingState.getName());
        }

        fightingState.doAction(this);
        /**
         * 只执行一次
         */
        if (fightingState.getDeleteType().contains(StateType.ONECE)) {
            this.fightingStates.remove(fightingState);
        }

    }

    /**
     * 发送信息
     * @return
     */
    public MapBody toMsg() {

        MapBody<String, Object> msg = new MapBody<>();

        return msg;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FightingPet that = (FightingPet) o;
        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }
}
