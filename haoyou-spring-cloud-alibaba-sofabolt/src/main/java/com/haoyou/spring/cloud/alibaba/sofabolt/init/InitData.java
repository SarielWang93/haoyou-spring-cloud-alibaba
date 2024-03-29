package com.haoyou.spring.cloud.alibaba.sofabolt.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.mapper.*;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.service.sofabolt.SendMsgService;
import com.haoyou.spring.cloud.alibaba.sofabolt.connection.Connections;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.ScoreRankUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
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
    private ScoreRankUtil scoreRankUtil;
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
    @Autowired
    private NumericalMapper numericalMapper;
    @Autowired
    private AchievementMapper achievementMapper;
    @Autowired
    private AchievementAimsMapper achievementAimsMapper;
    @Autowired
    private DailyTaskMapper dailyTaskMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private CommodityMapper commodityMapper;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityAwardMapper activityAwardMapper;
    @Autowired
    private FriendsMapper friendsMapper;
    @Autowired
    private LevelDesignMapper levelDesignMapper;
    @Autowired
    private ChapterMapper chapterMapper;
    @Autowired
    private HuntingAssociationMapper huntingAssociationMapper;


    @Autowired
    private UserUtil userUtil;
    @Autowired
    private SendMsgService sendMsgService;
    @Autowired
    private SendMsgUtil sendMsgUtil;

    private Date lastDo;

    @Override
    public void run(ApplicationArguments args) {
        //TODO 数据库中的公共数据缓存到内存或redis中
        //sofabolt服务绕过调用自己
        sendMsgUtil.setSendMsgService(sendMsgService);
        doInit();
    }

    public boolean doInit() {

        Date now = new Date();
        /**
         * 每次加载必须间隔一分钟以上，防止攻击
         */
        if (lastDo == null || now.getTime() - lastDo.getTime() > 60 * 1000) {

            //数值系统静态信息
            initNumerical();
            //用户信息
            initUsers();
            //加载狩猎协会
            initHuntingAssociation();
            //加载关卡信息
            initLevelDesign();
            //加载屏蔽词汇
            initShieldVoca();
            //好友信息
            initFriends();
            //活动信息
            initActivity();
            //商品信息
            initCommodity();
            //基金列表
            initFunds();
            //每日任务系统
            initDailyTask();
            //成就系统静态信息
            initAchievement();
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




//            redisObjectUtil.backup(RedisKey.USER_AWARD);
//            redisObjectUtil.inputBackup();

            lastDo = now;
            return true;
        }
        return false;
    }

    /**
     *
     */
    private void initUsers(){
        List<User> users = userMapper.selectAll();
        redisObjectUtil.save(RedisKey.USER_COUNT, Integer.valueOf(users.size()), -1);

        userUtil.refreshAllUserCatch();
    }
    /**
     * 加载狩猎协会
     */
    private void initHuntingAssociation() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.HUNTING_ASSOCIATION));

        List<HuntingAssociation> huntingAssociations = huntingAssociationMapper.selectAll();
        for (HuntingAssociation huntingAssociation : huntingAssociations) {
            String huntingAssociationKey = RedisKeyUtil.getKey(RedisKey.HUNTING_ASSOCIATION, huntingAssociation.getIdNum().toString());
            redisObjectUtil.save(huntingAssociationKey, huntingAssociation, -1);
        }

    }


    /**
     * 加载关卡信息
     */
    private void initLevelDesign() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.CHAPTER));
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.LEVEL_DESIGN));

        List<Chapter> chapters = chapterMapper.selectAll();

        for (Chapter chapter : chapters) {

            String chapterName = chapter.getName();
            String chapterKey = RedisKeyUtil.getKey(RedisKey.CHAPTER, chapterName);
            redisObjectUtil.save(chapterKey, chapter, -1);

            LevelDesign levelDesignSelect = new LevelDesign();
            levelDesignSelect.setChapterName(chapterName);
            List<LevelDesign> levelDesigns = levelDesignMapper.select(levelDesignSelect);

            for (LevelDesign levelDesign : levelDesigns) {
                String levelDesignKey = RedisKeyUtil.getKey(RedisKey.LEVEL_DESIGN, chapterName, levelDesign.getIdNum().toString());
                redisObjectUtil.save(levelDesignKey, levelDesign, -1);
            }

        }


    }

    /**
     * 加载屏蔽词汇
     */
    private void initShieldVoca() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.SHIELD_VOCA));
        File file = FileUtil.file("ShieldVoca.txt");
        FileReader fileReader = FileReader.create(file);
        List<String> shieldVocas = fileReader.readLines();

        List<String> all = new ArrayList<>();

        for (String shieldVoca : shieldVocas) {
            if (StrUtil.isNotEmpty(shieldVoca.trim())) {
                all.add(shieldVoca);
            }
        }
        String shieldVocaKey = RedisKeyUtil.getKey(RedisKey.SHIELD_VOCA);
        redisObjectUtil.save(shieldVocaKey, all, -1);

    }


    /**
     * 好友信息
     */
    private void initFriends() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.FRIENDS));

        List<Friends> friends = friendsMapper.selectAll();
        for (Friends friend : friends) {
            userUtil.saveFriend(friend);
        }


    }

    /**
     * 活动信息
     */
    private void initActivity() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.ACTIVITY));

        List<Activity> activities = activityMapper.selectAll();

        for (Activity activity : activities) {
            ActivityAward activityAwardSelect = new ActivityAward();
            activityAwardSelect.setActivitiId(activity.getId());

            //获取活动奖励列表
            List<ActivityAward> select = activityAwardMapper.select(activityAwardSelect);


            //按aim排序，
            TreeMap<Long, ActivityAward> activityAwardsTreeMap = new TreeMap<>();
            for (ActivityAward activityAward : select) {
                Long key = activityAward.getAim();
                //天天充值按进度排序
                if (activity.getActivityType().equals("DailyRecharge")) {
                    key = activityAward.getSchedule().longValue();
                }
                activityAwardsTreeMap.put(key, activityAward);

                select = CollUtil.newArrayList(activityAwardsTreeMap.values());
            }


            activity.setActivityAwards(select);


            if (activity.getPresetEnabled() == 1) {
                activity.setCurrent(true);
            }


            String activityKey = RedisKeyUtil.getKey(RedisKey.ACTIVITY, activity.getActivityType(), activity.getName());

            redisObjectUtil.save(activityKey, activity, -1);

        }

    }

    /**
     * 商品信息
     */
    private void initCommodity() {

        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.COMMODITY));

        List<Commodity> commodities = commodityMapper.selectAll();

        for (Commodity commodity : commodities) {


            String numericalMapperName = String.format("commodity_%s", commodity.getName());
            Numerical numerical = new Numerical();
            numerical.setName(numericalMapperName);
            List<Numerical> select = numericalMapper.select(numerical);
            if (select == null || select.isEmpty()) {
                numerical.setDescription(commodity.getDescription());
                numerical.setL10n(commodity.getL10n());
                numerical.setRefresh(commodity.getRefresh());
                numericalMapper.insertSelective(numerical);
            }

            String numericalMapperNameAll = String.format("commodity_all_%s", commodity.getName());
            Numerical numericalAll = new Numerical();
            numericalAll.setName(numericalMapperNameAll);
            List<Numerical> selectAll = numericalMapper.select(numericalAll);
            if (selectAll == null || selectAll.isEmpty()) {
                numericalAll.setDescription(commodity.getDescription());
                numericalAll.setL10n(commodity.getL10n());
                numericalAll.setRefresh(-1);
                numericalMapper.insertSelective(numericalAll);
            }


            String commodityKey = RedisKeyUtil.getKey(RedisKey.COMMODITY, commodity.getStoreName(), commodity.getName());
            redisObjectUtil.save(commodityKey, commodity, -1);
        }

    }

    /**
     * 基金列表
     */
    private void initFunds() {

        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.FUNDS));

        List<Fund> funds = fundMapper.selectAll();
        for (Fund fund : funds) {
            String fundKey = RedisKeyUtil.getKey(RedisKey.FUNDS, fund.getName());
            redisObjectUtil.save(fundKey, fund, -1);
        }
    }

    /**
     * 每日任务系统
     */
    private void initDailyTask() {

        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.DAILY_TASK));

        List<DailyTask> dailyTasks = dailyTaskMapper.selectAll();
        for (DailyTask dailyTask : dailyTasks) {

            String dailyTaskKey = RedisKeyUtil.getKey(RedisKey.DAILY_TASK, dailyTask.getName());
            redisObjectUtil.save(dailyTaskKey, dailyTask, -1);

        }


    }

    /**
     * 成就系统静态信息
     */
    private void initAchievement() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.ACHIEVEMENT));

        List<Achievement> achievements = achievementMapper.selectAll();
        for (Achievement achievement : achievements) {

            Example example = new Example(AchievementAims.class);
            example.createCriteria().andEqualTo("achievementId", achievement.getId());
            example.orderBy("priorityOrder");
            achievement.setAchievementAims(achievementAimsMapper.selectByExample(example));

            String achievementKey = RedisKeyUtil.getKey(RedisKey.ACHIEVEMENT, achievement.getName());
            redisObjectUtil.save(achievementKey, achievement, -1);
        }


    }

    /**
     * 数值系统静态信息
     */
    private void initNumerical() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.NUMERICAL));

        List<Numerical> numericals = numericalMapper.selectAll();

        for (Numerical numerical : numericals) {
            String numericalKey = RedisKeyUtil.getKey(RedisKey.NUMERICAL, numerical.getName());
            redisObjectUtil.save(numericalKey, numerical, -1);
        }


    }


    //卡池信息
    private void initEggPool() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.PET_EGG));
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.PET_EGG_POOL));

        List<PetEgg> petEggs = petEggMapper.selectAll();
        for (PetEgg petEgg : petEggs) {
            String petEggKey = RedisKeyUtil.getKey(RedisKey.PET_EGG, petEgg.getId().toString());
            redisObjectUtil.save(petEggKey, petEgg, -1);
        }

        List<PetEggPool> petEggPools = petEggPoolMapper.selectAll();
        for (PetEggPool petEggPool : petEggPools) {
            String petEggPoolKey = RedisKeyUtil.getKey(RedisKey.PET_EGG_POOL, petEggPool.getEggId().toString(), petEggPool.getId().toString());
            redisObjectUtil.save(petEggPoolKey, petEggPool, -1);
        }

    }


    //奖励信息
    private void initAward() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.AWARD));
        List<Award> awards = awardMapper.selectAll();
        for (Award award : awards) {
            award.notToLong();
            String awardKey = RedisKeyUtil.getKey(RedisKey.AWARD, award.getType());
            redisObjectUtil.save(awardKey, award, -1);
        }
    }

    //加载服务器信息
    private void initServer() {
        redisObjectUtil.deleteAll(RedisKeyUtil.getlkKey(RedisKey.SERVER));
        List<Server> servers = serverMapper.selectAll();
        for (Server server : servers) {
            String levelUpExpKey = RedisKeyUtil.getKey(RedisKey.SERVER, server.getId().toString());
            redisObjectUtil.save(levelUpExpKey, server, -1);
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
            redisObjectUtil.save(levelUpExpKey, levelUpExp, -1);
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
            redisObjectUtil.save(levLoyaltyKey, levLoyalty, -1);
        }

    }


    /**
     * 初始化排行榜
     */
    public void initRanking() {

        //初始化缓存排行榜

        //分服
        String rankKey = RedisKey.RANKING;
        String numericalName = "daily_ladder_integral";

        List<User> users = userMapper.selectAll();
        //分服
        List<Server> servers = serverMapper.selectAll();
        for (Server server : servers) {
            User user = new User();
            user.setServerId(server.getId());
            List<User> serverUsers = userMapper.select(user);
            String serverRankKey = RedisKeyUtil.getKey(rankKey, server.getServerNum().toString());
            this.ranking(serverUsers, serverRankKey,numericalName);
        }

        //传奇排名
        String yyMM = DateUtil.date().toString("yyMM");
        rankKey = RedisKeyUtil.getKey(RedisKey.LADDER_RANKING, yyMM);
        numericalName = "ladder_integral";
        this.ranking(users, rankKey,numericalName);

        //上个月传奇排名
        DateTime dateTime = DateUtil.offsetMonth(DateUtil.date(), -1);
        yyMM = dateTime.toString("yyMM");
        rankKey = RedisKeyUtil.getKey(RedisKey.LADDER_RANKING, yyMM);
        numericalName = "ladder_integral_last_month";
        this.ranking(users, rankKey,numericalName);

    }


    private void ranking(List<User> users, String rankKey,String numericalName) {
        redisObjectUtil.delete(rankKey);
        Map<String, Long> msgs = new HashMap<>();
        for (User user : users) {

            User userByUid = userUtil.getUserByUid(user.getUid());

            msgs.put(user.getUid(),userByUid.getUserNumericalMap().get(numericalName).getValue());
        }
        scoreRankUtil.batchAdd(rankKey, msgs);
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
