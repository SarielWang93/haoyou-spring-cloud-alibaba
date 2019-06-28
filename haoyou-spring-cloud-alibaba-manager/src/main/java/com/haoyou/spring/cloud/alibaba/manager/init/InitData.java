package com.haoyou.spring.cloud.alibaba.manager.init;

import com.alibaba.dubbo.config.annotation.Reference;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.mapper.*;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 从mysql初始化公共数据到redis
 */
@Component
@Order(value = 1)
public class InitData implements ApplicationRunner {


    @Autowired
    private RedisObjectUtil redisObjectUtil;

    @Autowired
    private ScoreRankService scoreRankService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SkillMapper skillMapper;
    @Autowired
    private ResoutMapper resoutMapper;
    @Autowired
    private SkillResoutMapper skillResoutMapper;

    @Autowired
    private StateMapper stareMapper;
    @Autowired
    private StateResoutMapper stateResoutMapper;
    @Autowired
    private PetTypeMapper petTypeMapper;
    @Autowired
    private PetTypeAiMapper petTypeAiMapper;
    @Autowired
    private PropMapper propMapper;
    @Autowired
    private VersionControlMapper versionControlMapper;
    @Autowired
    private LevLoyaltyMapper levLoyaltyMapper;

    @Autowired
    private LevelUpExpMapper levelUpExpMapper;

    private Date lastDo;

    @Override
    public void run(ApplicationArguments args) {
        //TODO 数据库中的公共数据缓存到内存或redis中
        doInit();
    }

    public boolean doInit() {
        Date now = new Date();
        /**
         * 每次加载必须间隔一分钟以上，防止攻击
         */
        if (lastDo == null || now.getTime() - lastDo.getTime() > 60 * 1000) {
            //加载版本信息
            initVersion();
            //加载排行榜
            initRanking();
            //加载技能
            initSkill();
            //加载宠物类型
            initPetType();
            //加载道具
            initProp();
            //加载忠诚度/等级关系列表
            initLevLoyalty();
            //加载宠物等级提升所需经验表
            initLevelUpExp();
            lastDo = now;
            return true;
        }
        return false;
    }

    /**
     * 宠物等级提升所需经验表
     */
    private void initLevelUpExp() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.LEVEL_UP_EXP));
        List<LevelUpExp> levelUpExps = levelUpExpMapper.selectAll();
        for (LevelUpExp levelUpExp : levelUpExps) {
            String levelUpExpKey = RedisKeyUtil.getKey(RedisKey.LEVEL_UP_EXP, levelUpExp.getLevel().toString());
            redisObjectUtil.save(levelUpExpKey,levelUpExp,-1);
        }

    }

    /**
     * 加载忠诚度/等级关系列表
     */
    private void initLevLoyalty() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.LEV_LOYALTY));
        List<LevLoyalty> levLoyalties = levLoyaltyMapper.selectAll();
        for (LevLoyalty levLoyalty : levLoyalties) {
            String levLoyaltyKey = RedisKeyUtil.getKey(RedisKey.LEV_LOYALTY, levLoyalty.getLoyaltyLev().toString());
            redisObjectUtil.save(levLoyaltyKey,levLoyalty,-1);
        }

    }


    /**
     * 初始化排行榜
     *
     * @throws Exception
     */
    public void initRanking() {

        //初始化缓存排行榜
        final String ranking = RedisKey.RANKING;
        List<User> users = userMapper.selectAll();
        Map<String, Long> msgs = new HashMap<>();
        for (User user : users) {
            msgs.put(user.getUid(), user.getRank().longValue());
        }
        scoreRankService.batchAdd(ranking, msgs);
    }

    /**
     * 初始化技能
     *
     * @throws Exception
     */
    public void initSkill() {

        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.SKILL));

        List<Skill> skills = skillMapper.selectAll();

        for (Skill skill : skills) {
            this.getSkillResout(skill);
            String skillKey = RedisKeyUtil.getKey(RedisKey.SKILL, skill.getUid());
            redisObjectUtil.save(skillKey, skill, -1);
        }
    }

    private void getSkillResout(Skill skill) {
        List<Resout> resouts = new ArrayList<>();
        /**
         * 查询技能效果
         */
        SkillResout skillfResout = new SkillResout();
        skillfResout.setSkillUid(skill.getUid());
        List<SkillResout> skillResouts = skillResoutMapper.select(skillfResout);

        for (SkillResout skillResout : skillResouts) {
            String skillResoutUid = skillResout.getResoutUid();
            Resout resout = new Resout();
            resout.setUid(skillResoutUid);
            resout = resoutMapper.selectOne(resout);

            this.getResoutState(resout);
            resouts.add(resout);
        }
        skill.setResouts(resouts);
    }


    /**
     * 获取结果状态
     *
     * @param resout
     */

    private void getResoutState(Resout resout) {
        State state = new State();
        state.setUid(resout.getStateUid());
        state = stareMapper.selectOne(state);

        this.getStateResout(state);
        resout.setState(state);
    }

    /**
     * 获取状态产生的结果（会有递归问题，不过取决于数据库的数据）
     *
     * @param state
     */
    private void getStateResout(State state) {

        List<Resout> resouts = new ArrayList<>();
        StateResout statefResout = new StateResout();
        statefResout.setStateUid(state.getUid());
        List<StateResout> stateResouts = stateResoutMapper.select(statefResout);

        for (StateResout stateResout : stateResouts) {
            Resout resout = new Resout();
            resout.setUid(stateResout.getResoutUid());
            resout = resoutMapper.selectOne(resout);
            this.getResoutState(resout);
            resouts.add(resout);
        }
        state.setResouts(resouts);

    }


    /**
     * 初始化宠物类型
     *
     * @throws Exception
     */
    public void initPetType() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.PET_TYPE));


        List<PetType> petTypes = petTypeMapper.selectAll();
        for (PetType petType : petTypes) {
            //获取ai权重信息
            PetTypeAi petTypeAi = new PetTypeAi();
            petTypeAi.setPetTypeUid(petType.getUid());
            PetTypeAi petTypeAi1 = petTypeAiMapper.selectOne(petTypeAi);
            petType.setPetTypeAi(petTypeAi1);

            String petTypeKey = RedisKeyUtil.getKey(RedisKey.PET_TYPE, petType.getUid());
            redisObjectUtil.save(petTypeKey, petType, -1);
        }
    }

    /**
     * 初始化道具表
     */
    public void initProp() {

        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.PROP));

        List<Prop> props = propMapper.selectAll();
        for (Prop prop : props) {
            String propKey = RedisKeyUtil.getKey(RedisKey.PROP, prop.getUid());
            redisObjectUtil.save(propKey, prop, -1);
        }
    }

    /**
     * 初始化版本表
     */
    public void initVersion() {

        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.VERSION));

        List<VersionControl> versions = versionControlMapper.selectAll();
        for (VersionControl version : versions) {
            String propKey = RedisKeyUtil.getKey(RedisKey.VERSION, version.getUid());
            redisObjectUtil.save(propKey, version, -1);
        }

    }
}
