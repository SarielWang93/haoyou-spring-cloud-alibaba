package com.haoyou.spring.cloud.alibaba.manager.init;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.mapper.*;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
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

    @Autowired
    private UserDataMapper userDataMapper;
    @Autowired
    private CurrencyMapper currencyMapper;
    @Autowired
    private ServerMapper serverMapper;
    @Autowired
    private AwardMapper awardMapper;
    @Autowired
    private PetEggMapper petEggMapper;
    @Autowired
    private PetEggPoolMapper petEggPoolMapper;


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
            //加载卡池信息
            initEggPool();
            //加载奖励信息
            initAward();
            //加载服务器信息
            initServer();
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
    //卡池信息
    private void initEggPool() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.PET_EGG));
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.PET_EGG_POOL));

        List<PetEgg> petEggs = petEggMapper.selectAll();
        for(PetEgg petEgg : petEggs){
            String petEggKey = RedisKeyUtil.getKey(RedisKey.PET_EGG, petEgg.getId().toString());
            redisObjectUtil.save(petEggKey,petEgg,-1);
        }

        List<PetEggPool> petEggPools = petEggPoolMapper.selectAll();
        for(PetEggPool petEggPool : petEggPools){
            String petEggPoolKey = RedisKeyUtil.getKey(RedisKey.PET_EGG_POOL, petEggPool.getEggId().toString(),petEggPool.getId().toString());
            redisObjectUtil.save(petEggPoolKey,petEggPool,-1);
        }

    }


    //奖励信息
    private void initAward() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.AWARD));
        List<Award> awards = awardMapper.selectAll();
        for (Award award : awards) {
            String awardKey = RedisKeyUtil.getKey(RedisKey.AWARD, award.getType());
            redisObjectUtil.save(awardKey,award,-1);
        }
    }

    //加载服务器信息
    private void initServer() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.SERVER));
        List<Server> servers = serverMapper.selectAll();
        for (Server server : servers) {
            String levelUpExpKey = RedisKeyUtil.getKey(RedisKey.SERVER, server.getId().toString());
            redisObjectUtil.save(levelUpExpKey,server,-1);
        }

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
     */
    public void initRanking() {

        //初始化缓存排行榜

        //总榜
        String rankKey = RedisKey.RANKING;
        List<User> users = userMapper.selectAll();
        this.ranking(users,rankKey);

        //分服
        List<Server> servers = serverMapper.selectAll();
        for(Server server : servers){
            User user = new User();
            user.setServerId(server.getId());
            List<User> serverUsers = userMapper.select(user);
            String serverRankKey = RedisKeyUtil.getKey(RedisKey.RANKING,server.getServerNum().toString());
            this.ranking(serverUsers,serverRankKey);

        }


    }


    private void ranking(List<User> users,String rankKey) {
        redisObjectUtil.delete(rankKey);
        Map<String, Long> msgs = new HashMap<>();
        for (User user : users) {
            Currency currency = new Currency();
            currency.setUserUid(user.getUid());
            currency = currencyMapper.selectOne(currency);

            UserData userData = new UserData();
            userData.setUserUid(user.getUid());
            userData = userDataMapper.selectOne(userData);

            Map<String, Object> player = new HashMap<>();

            player.put("useruid", user.getUid());
            player.put("name", userData.getName());
            player.put("avatar", userData.getAvatar());
            player.put("integral", currency.getRank());

            String plj="";
            try {
                plj = MapperUtils.obj2json(player);
            } catch (Exception e) {
                e.printStackTrace();
            }

            msgs.put(plj, currency.getRank().longValue());
        }
        scoreRankService.batchAdd(rankKey, msgs);
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
            String propKey = RedisKeyUtil.getKey(RedisKey.PROP, prop.getName());
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
