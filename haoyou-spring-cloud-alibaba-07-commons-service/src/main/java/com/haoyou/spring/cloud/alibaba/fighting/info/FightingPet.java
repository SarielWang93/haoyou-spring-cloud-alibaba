package com.haoyou.spring.cloud.alibaba.fighting.info;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.domain.PunishValue;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.SkillType;
import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;

import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.SkillBoard;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 战斗中宠物属性
 */
@Data
@JsonIgnoreProperties(value = {"fightingCamp", "distinguish", "acceptHurtFrom", "acceptHurt", "attack", "redisObjectUtil"}, ignoreUnknown = true)
public class FightingPet implements Serializable {
    private static final long serialVersionUID = 8310639713725476067L;

    private transient RedisObjectUtil redisObjectUtil;

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

    //所属阵容
    private FightingCamp fightingCamp;
    //敌我分明
    private Map<String, FightingCamp> distinguish;

    /**
     * 原型pet
     */
    private Pet pet;

    /**
     * 昵称
     */
    private String nickName;
    //uid
    private String uid;


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
     * 根据用户获取宠物对象
     *
     * @param user
     * @param redisObjectUtil
     * @return
     */
    public static List<FightingPet> getByUser(User user, RedisObjectUtil redisObjectUtil) {
        return getByUser(user.getUid(),redisObjectUtil);

    }
    public static List<FightingPet> getByUser(String userUid, RedisObjectUtil redisObjectUtil) {
        List<FightingPet> fightingPets = new ArrayList<>();

        String useruidkey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, userUid);

        String key = RedisKeyUtil.getlkKey(useruidkey);

        HashMap<String, FightingPet> allFightingPetPets = redisObjectUtil.getlkMap(key, FightingPet.class);

        for (FightingPet fightingPet : allFightingPetPets.values()) {
            fightingPet.setRedisObjectUtil(redisObjectUtil);
            fightingPets.add(fightingPet);
        }
        return fightingPets;
    }
    /**
     * 根据用户和宠物uid查找
     *
     * @param user
     * @param petUid
     * @param redisObjectUtil
     * @return
     */
    public static FightingPet getByUserAndPetUid(User user, String petUid, RedisObjectUtil redisObjectUtil) {
        return getByUserAndPetUid(user.getUid(),petUid,redisObjectUtil);
    }
    public static FightingPet getByUserAndPetUid(String userUid, String petUid, RedisObjectUtil redisObjectUtil) {
        List<FightingPet> fightingPets = new ArrayList<>();

        String userUidKey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, userUid);

        String key = RedisKeyUtil.getKey(userUidKey, petUid);

        FightingPet fightingPet = redisObjectUtil.get(key, FightingPet.class);

        if (fightingPet != null) {
            fightingPet.setRedisObjectUtil(redisObjectUtil);
        }

        return fightingPet;
    }


    public FightingPet() {
    }

    /**
     * pet初始化
     *
     * @param pet
     */
    public FightingPet(Pet pet, RedisObjectUtil redisObjectUtil) {
        this.redisObjectUtil = redisObjectUtil;
        this.init(pet);
    }

    /**
     * petType初始化
     * @param petType
     * @param aiUser
     * @param isWork
     * @param level
     * @param redisObjectUtil
     */
    public FightingPet(PetType petType, User aiUser, Integer isWork, Integer level, RedisObjectUtil redisObjectUtil) {
        Pet pet = new Pet(aiUser, petType, isWork);
        pet.setLevel(level);
        this.redisObjectUtil = redisObjectUtil;
        this.init(pet);

        //
        this.setUid(String.format("ai-%s", this.getUid()));
        this.setNickName(String.format("ai-%s", this.getNickName()));
    }

    /**
     * 根据宠物初始化
     */
    public FightingPet init() {
        init(this.pet);
        return this;
    }

    public void init(Pet pet) {


        pet.initIngredientsPieces();
        this.pet = pet;

        //基础属性
        this.nickName = pet.getNickName();
        this.uid = pet.getUid();


        //面板暴击与速度与等级无关
        this.mb_luk = this.pet.getLuk();
        this.mb_spd = this.pet.getSpd();
        this.mb_tpc = 100;


        //养成相关
        this.refreshMbByLevel();

        /**
         * 初始化宠物技能
         */
        initSkill();
    }

    /**
     * 初始化宠物技能
     */
    public FightingPet initSkill() {
        List<Skill> skills = new ArrayList<>();
        List<PetSkill> skillUids = this.pet.getOtherSkill();

        if (StrUtil.isNotEmpty(this.pet.getInhSkill()))
            skills.add(this.redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.SKILL, this.pet.getInhSkill()), Skill.class));
        if (StrUtil.isNotEmpty(this.pet.getTalentSkill()))
            skills.add(this.redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.SKILL, this.pet.getTalentSkill()), Skill.class));
        if (StrUtil.isNotEmpty(this.pet.getUniqueSkill()))
            skills.add(this.redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.SKILL, this.pet.getUniqueSkill()), Skill.class));
        if (StrUtil.isNotEmpty(this.pet.getSpecialAttack()))
            skills.add(this.redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.SKILL, this.pet.getSpecialAttack()), Skill.class));

        //技能盘满的时候加成技能
        if (this.pet.getSkillBoard() != null) {
            SkillBoard skillBoard = redisObjectUtil.deserialize(this.pet.getSkillBoard(), SkillBoard.class);
            if (skillBoard.isFull()) {
                if (StrUtil.isNotEmpty(pet.getFullSkillBoard()))
                    skills.add(this.redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.SKILL, this.pet.getFullSkillBoard()), Skill.class));
            }
        }

        if (skillUids != null) {
            for (PetSkill petSkill : skillUids) {
                String skillUid = petSkill.getSkillUid();
                if (!StrUtil.isEmpty(skillUid)) {
                    String skillKey = RedisKeyUtil.getKey(RedisKey.SKILL, skillUid);
                    Skill skill = this.redisObjectUtil.get(skillKey, Skill.class);

                    //相同名称的只保留最高品质
                    boolean has = false;
                    Skill skillLess = null;
                    for (Skill skillh : skills) {
                        if (skillh.getName().equals(skill.getName())) {
                            if (skill.getQuality() > skillh.getQuality()) {
                                skillLess = skillh;
                            } else {
                                has = true;
                            }
                        }
                    }
                    if (skillLess != null) {
                        skills.remove(skillLess);
                    }
                    if (!has) {
                        skills.add(skill);
                    }
                }
            }
        }

        this.skills = skills;

        //执行全局技能
        this.overAll();
        return this;
    }


    /**
     * 缓存
     */
    public void save(String key) {
        //redis存储
        this.pet.setLastUpdateDate(new Date());
        this.ridesKey = key;
        this.redisObjectUtil.save(key, this);
    }

    public void save() {
        //redis存储
        if (this.ridesKey == null) {
            this.ridesKey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, this.pet.getUserUid(), this.pet.getUid());
        }
        this.pet.setLastUpdateDate(new Date());
        this.redisObjectUtil.save(this.ridesKey, this);
    }

    /**
     * 删除缓存
     */
    public void delete() {
        //redis删除
        this.redisObjectUtil.delete(this.ridesKey);
    }

    /**
     * redis存储时用的key
     */
    private String ridesKey;
    /**
     * 接收到的伤害（临时存储）
     */
    private int acceptHurt;
    /**
     * 伤害结果（临时存储）
     */
    private int acceptHurtR;

    /**
     * 接收到的伤害来源（临时存储）
     */
    private FightingPet acceptHurtFrom;

    /**
     * 输出伤害值（临时存储）
     */
    private int attack;


    /**
     * 治疗系数（执行治疗操作时需要系数，部分状态会修改系数，如：增加治疗量）
     */
    private Integer mb_tpc;
    private Integer ft_tpc;

    /**
     * 物攻
     */

    private Integer mb_atn;
    private Integer ft_atn;
    /**
     * 物防
     */

    private Integer mb_def;
    private Integer ft_def;
    /**
     * 速度
     */
    private Integer punishValue;//惩罚值
    private Integer action_time;//行动权

    private Integer mb_spd;
    private Integer ft_spd;
    /**
     * 最大血量
     */

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

    private Integer mb_luk;
    private Integer ft_luk;
    private boolean luky;


//    /**
//     * 获得经验
//     *
//     * @param upExp
//     */
//    public void upExp(Integer upExp) {
//        Integer exp = this.pet.getExp();
//        Integer levUpExp = this.pet.getLevUpExp();
//
//        //升级判断
//        Integer expl = exp + upExp - levUpExp;
//        if (expl >= 0) {
//            upLevel();
//            this.pet.setExp(expl);
//        } else {
//            this.pet.setExp(exp + upExp);
//        }
//
//    }

    /**
     * 升级操作
     */
    public void upLevel() {
        Integer level = this.pet.getLevel() + 1;
        this.pet.setLevel(level);
        this.refreshMbByLevel();
    }

    /**
     * 培养操作
     */
    public void upCulture(double r) {
        this.pet.setCultureResoult(this.pet.getCultureResoult() + r);
        this.pet.setCulture(this.pet.getCulture() + 1);
        this.refreshMbByLevel();
    }

    /**
     * 根据等级刷新面板属性（经验值主要是用于显示）
     */
    public void refreshMbByLevel() {
        this.mb_atn = this.pet.getAtn();
        this.mb_def = this.pet.getDef();
        this.mb_max_hp = this.pet.getHp();
        //this.mb_spd = this.pet.getSpd();

        int atn = 0, def = 0, max_hp = 0;
        //等级相加
        for (int i = 0; i < this.pet.getLevel(); i++) {
            atn += this.pet.getAtnGr();
            def += this.pet.getDefGr();
            max_hp += this.pet.getHpGr();
        }
        //培养结果
        Double cultureResoult = this.getPet().getCultureResoult();
        cultureResoult = 1 + cultureResoult / 100;

        atn *= cultureResoult;
        def *= cultureResoult;
        max_hp *= cultureResoult;

        //喂食，果实效果

        this.ingredients();

        this.mb_atn += atn;
        this.mb_def += def;
        this.mb_max_hp += max_hp;

        //this.mb_spd += this.pet.getLevel();

    }

    /**
     * 喂食，果实效果
     */
    private void ingredients() {
        for (int i = 1; i < 5; i++) {
            String attr = (String) ReflectUtil.getFieldValue(this.pet, String.format("ingredientsAttr%s", i));
            Integer pieces = (Integer) ReflectUtil.getFieldValue(this.pet, String.format("ingredientsPieces%s", i));

            Integer starClass = this.getPet().getStarClass();

            //提升基数
            int up = 0;
            if (starClass < 3) {
                up = starClass;
            } else {
                up = (starClass - 2) * 5;
            }

            //提升对应属性
            if (attr.equals("hp")) {
                this.mb_max_hp += up * 5 * (pieces - 1);
            } else {
                Integer mb_attr = (Integer) ReflectUtil.getFieldValue(this, String.format("mb_%s", attr));
                ReflectUtil.setFieldValue(this, String.format("mb_%s", attr), mb_attr + up * (pieces - 1));
            }
        }
    }

    /**
     * 初始化战斗属性
     */
    public void initFighting() {
        //拥有宠物等级和影响属性
        String userUid = this.pet.getUserUid();
        List<FightingPet> byUser = FightingPet.getByUser(userUid, this.redisObjectUtil);
        int levSum = 0;
        for(FightingPet fightingPet:byUser){
            levSum+=fightingPet.getPet().getLevel();
        }
        int up = (levSum/100 * 2)+1000;
        //根据面板数据对战斗数据进行初始化
        this.ft_atn = mb_atn*up/1000;
        this.ft_def = mb_def*up/1000;
        this.ft_spd = mb_spd*up/1000;
        this.ft_max_hp = mb_max_hp;
        this.ft_luk = mb_luk;
        //满血
        this.hp = this.ft_max_hp;
        //护盾归0
        this.ft_shield = 0;
        //治疗系数
        this.ft_tpc = this.mb_tpc;
        //刷新状态为空
        this.fightingStates = new ArrayList<>();
        //刷新战斗记录
        this.steps = new TreeMap<>();
        this.shotNum = 0;
        this.shots = new TreeMap<>();

        /**
         * 初始惩罚值为100
         */
        this.reflashActionTime(PunishValue.INITIAL);


    }

    /**
     * 根据状态加成刷新战斗过程中的属性值
     */
    public void refreshFt() {
        int mhp = this.ft_max_hp;
        int hurt = this.ft_max_hp - this.hp;

        this.ft_atn = this.mb_atn;
        this.ft_def = this.mb_def;
        this.ft_spd = this.mb_spd;
        this.ft_max_hp = this.mb_max_hp;
        this.ft_luk = this.mb_luk;
        //状态影响战斗属性
        this.doFightingStateByType(StateType.REFRESH_FT);
        //最大血量增减对当前血量影响
        if (this.ft_max_hp - mhp > 0) {
            this.hp = this.ft_max_hp - hurt;
        } else {
            if (this.hp > this.ft_max_hp) {
                this.hp = this.ft_max_hp;
            }
        }

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
        this.acceptHurtR = (this.acceptHurt - this.ft_def) < 0 ? 0 : (this.acceptHurt - this.ft_def);
        /**
         * 承受伤害计算，有关状态（处理伤害值）
         */
        this.doFightingStateByType(StateType.DAMAGE_CALCULATION_SUFFER);

        /**
         * 限制真实伤害不能小于接受伤害的10%
         */
        this.acceptHurtR = this.acceptHurtR < this.acceptHurt * 10 / 100 ? this.acceptHurt * 10 / 100 : this.acceptHurtR;

        /**
         * 真实伤害减血与护盾的逻辑
         */
        this.ft_shield -= this.acceptHurtR;

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
        this.addStep(FightingStep.HURT, String.valueOf(this.acceptHurtR));

        //伤害结算之后触发
        this.doFightingStateByType(StateType.DAMAGE_CALCULATION_HURT);

        /**
         * 死亡触发状态
         */

        if (this.getHp() < 1) {
            this.doFightingStateByType(StateType.DEATH);
        }
        /**
         * 仍然死亡去除光环（所有自己产生的属性类状态）
         */
        if (this.getHp() < 1) {
            /**
             * 添加步骤信息
             */
            this.addStep(FightingStep.DIE, "");
            //清空自身状态
            this.fightingStates = new ArrayList<>();
            /**
             * 清空光环
             */
            List<Skill> skillsOpening = this.getSkillsByType(SkillType.OPENING);
            //获取光环状态
            List<FightingState> ghFightingStates = new ArrayList<>();
            for (Skill skill : skillsOpening) {
                for (Resout resout : skill.getResouts()) {
                    if (resout.getNumType().equals(3)) {
                        ghFightingStates.add(FightingState.creatFightingState(resout, skill.getQuality(), this, 0));
                    }
                }
            }
            //清除光环状态
            for (Map.Entry<String, FightingCamp> entry : fightingCamp.getFightingRoom().getFightingCamps().entrySet()) {
                for (Map.Entry<Integer, FightingPet> e : entry.getValue().getFightingPets().entrySet()) {
                    List<FightingState> attrUps = e.getValue().getFightingStateByType(StateType.REFRESH_FT);
                    List<FightingState> remove = new ArrayList<>();
                    for (FightingState attrUp : attrUps) {
                        if (ghFightingStates.contains(attrUp)) {
                            remove.add(attrUp);
                        }
                    }
                    e.getValue().removeFightingState(remove);
                }
            }

        } else {
            this.removeState(StateType.HURT);
            /**
             * 血量阈值，触发状态
             */
            this.doFightingStateByType(StateType.THRESHOLD_DOWN);
            this.removeState(StateType.THRESHOLD_DOWN_DELETE);

        }

        //伤害归0
        this.acceptHurt = 0;
        this.acceptHurtR = 0;

        return this.getHp() < 1;

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
                this.attack = this.attack * LukFactor / 100;
            } else {
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
     * 击杀操作
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
        if (fightingState.getType().equals(StateType.FORTHWITH)) {
            this.StateDoAction(fightingState);
        } else {
            /**
             * 检查覆盖逻辑
             */

            List<FightingState> covers = new ArrayList<>();

            for (FightingState fightingState1 : this.fightingStates) {
                if (fightingState1.equals(fightingState)) {
                    if (fightingState.getQuality() >= fightingState1.getQuality()) {
                        covers.add(fightingState1);
                    }
                }
            }
            this.removeFightingState(covers);

            //减速改变行动权
            if (fightingState.getActionType().equals(StateType.ATTR_UP) && "spd".equals(fightingState.getInfAttr()) && fightingState.getPercent() < 0) {
                this.action_time += this.action_time * 100 / (-fightingState.getPercent());
            }


            this.fightingStates.add(fightingState);

            /**
             * 添加步骤信息
             */
            this.addStep(FightingStep.ADD_STATE, fightingState.getName() + ":" + fightingState.getRound());

            //执行免疫
            this.doFightingStateByType(StateType.ADD_STATE);
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
        //回合开始操作块
        this.doFightingStateByType(StateType.TURN_START_BLOCK);


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
            if (deleteType == StateType.ROUND)
            //回合制删除状态
            {
                if (fightingState.getRound().equals(0)) {
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
                    Field field = Pet.class.getDeclaredField(infAttr);
                    field.setAccessible(true);
                    Integer o = (Integer) field.get(this.pet);
                    Integer ch = 0;
                    Integer fixed = fightingState.getFixed();
                    Integer percent = fightingState.getPercent();
                    if (fixed != null && fixed != 0) {
                        ch = fixed;
                    } else {
                        ch = o * percent / 100;
                    }
                    Field fieldmb = this.getClass().getDeclaredField(String.format("mb_%s", infAttr));
                    fieldmb.setAccessible(true);
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
            this.addStep(FightingStep.DO_SKILL, skill.getType().toString());
            for (Resout resout : skill.getResouts()) {
                this.resoutDo(resout, skill.getQuality(), blockCount);
            }
            //技能如果为进攻技能，要执行进攻操作
            if (skill.getAttributeType() == null) {
                skill.setAttributeType(SkillType.PASSIVE);
            }
            if (skill.getAttributeType().equals(SkillType.ATTECK)) {
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
            if (numType.equals(1)) {
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
     * @param removeFightingState
     */
    public void removeFightingState(FightingState removeFightingState) {
        List<FightingState> fightingStates = new ArrayList<>();
        fightingStates.add(removeFightingState);
        this.removeFightingState(fightingStates);
    }

    public void removeFightingState(List<FightingState> removeFightingStates) {

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
    public List<FightingState> getFightingStateByType(Integer type) {
        List<FightingState> rl = new ArrayList<>();
        for (FightingState fightingState : this.fightingStates) {

            if (fightingState.getType().equals(type)) {
                rl.add(fightingState);
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
    public List<Skill> getSkillsByType(Integer type) {
        List<Skill> tSkills = new ArrayList<>();
        for (Skill skill : this.skills) {
            if (type.equals(skill.getType())) {
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

        //全局增加步骤
        FightingRoom fightingRoom = this.fightingCamp.getFightingRoom();
        fightingRoom.addStep(fightingStep);
        //本宠物步骤本地保存（暂时没有用）
        //this.steps.put(fightingRoom.getStep(), fightingStep);

    }

    /**
     * 添加出手
     *
     * @param fightingReq
     */
    public void addShot(FightingReq fightingReq) {
        //全局添加出手
        FightingRoom fightingRoom = this.fightingCamp.getFightingRoom();
        fightingRoom.addShot(fightingReq);
        //本宠物添加出手
        this.shotNum++;
        this.shots.put(this.shotNum, fightingReq);
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
            this.removeFightingState(fightingState);
        }

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


    @Override
    public String toString() {
        return "FightingPet{" +
                ", pet=" + pet +
                ", nickName='" + nickName + '\'' +
                ", uid='" + uid + '\'' +
                ", fightingStates=" + fightingStates +
                ", skills=" + skills +
                ", shotNum=" + shotNum +
                ", shots=" + shots +
                ", steps=" + steps +
                ", ridesKey='" + ridesKey + '\'' +
                ", acceptHurt=" + acceptHurt +
                ", acceptHurtR=" + acceptHurtR +
                ", acceptHurtFrom=" + acceptHurtFrom +
                ", attack=" + attack +
                ", mb_tpc=" + mb_tpc +
                ", ft_tpc=" + ft_tpc +
                ", mb_atn=" + mb_atn +
                ", ft_atn=" + ft_atn +
                ", mb_def=" + mb_def +
                ", ft_def=" + ft_def +
                ", punishValue=" + punishValue +
                ", action_time=" + action_time +
                ", mb_spd=" + mb_spd +
                ", ft_spd=" + ft_spd +
                ", mb_max_hp=" + mb_max_hp +
                ", ft_max_hp=" + ft_max_hp +
                ", ft_shield=" + ft_shield +
                ", hp=" + hp +
                ", mb_luk=" + mb_luk +
                ", ft_luk=" + ft_luk +
                ", luky=" + luky +
                '}';
    }
}
