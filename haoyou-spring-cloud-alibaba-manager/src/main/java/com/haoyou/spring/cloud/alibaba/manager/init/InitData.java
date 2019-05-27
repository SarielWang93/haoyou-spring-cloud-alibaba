package com.haoyou.spring.cloud.alibaba.manager.init;

import com.alibaba.dubbo.config.annotation.Reference;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.mapper.*;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.service.redis.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从mysql初始化公共数据到redis
 */
@Component
@Order(value = 1)
public class InitData implements ApplicationRunner {


    @Autowired
    private RedisObjectUtil redisObjectUtil;

    @Reference(version = "${score-rank.service.version}")
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
    protected VersionControlMapper versionControlMapper;

    @Override
    public void run(ApplicationArguments args){
        //TODO 数据库中的公共数据缓存到内存或redis中
        //初始化道具
        initVersion();
        //初始化排行榜
        initScoreRank();
        //初始化技能
        initSkill();
        //初始化宠物类型
        initPetType();
        //初始化道具
        initProp();
    }

    /**
     * 初始化排行榜
     *
     * @throws Exception
     */
    public void initScoreRank(){
        //初始化缓存排行榜
        final String scoreRank = RedisKey.SCORE_RANK;
        List<User> users = userMapper.selectAll();
        Map<String, Long> msgs = new HashMap<>();
        for (User user : users) {
            msgs.put(user.getUid(), user.getRank().longValue());
        }
        scoreRankService.batchAdd(scoreRank, msgs);
    }

    /**
     * 初始化技能
     *
     * @throws Exception
     */
    public void initSkill() {
        List<Skill> skills = skillMapper.selectAll();

        for(Skill skill:skills){
            this.getSkillResout(skill);
            String skillKey = RedisKeyUtil.getKey(RedisKey.SKILL, skill.getUid());
            redisObjectUtil.save(skillKey,skill,-1);
        }
    }

    private void getSkillResout(Skill skill){
        List<Resout> resouts=new ArrayList<>();
        /**
         * 查询技能效果
         */
        SkillResout skillfResout =new SkillResout();
        skillfResout.setSkillUid(skill.getUid());
        List<SkillResout> skillResouts = skillResoutMapper.select(skillfResout);

        for(SkillResout skillResout :skillResouts){
            String skillResoutUid = skillResout.getResoutUid();
            Resout resout =new Resout();
            resout.setUid(skillResoutUid);
            resout = resoutMapper.selectOne(resout);

            this.getResoutState(resout);
            resouts.add(resout);
        }
        skill.setResouts(resouts);
    }


    /**
     * 获取结果状态
     * @param resout
     */

    private void getResoutState(Resout resout){
        State state = new State();
        state.setUid(resout.getStateUid());
        state = stareMapper.selectOne(state);

        this.getStateResout(state);
        resout.setState(state);
    }

    /**
     * 获取状态产生的结果（会有递归问题，不过取决于数据库的数据）
     * @param state
     */
    private void getStateResout(State state){

        List<Resout> resouts=new ArrayList<>();
        StateResout statefResout = new StateResout();
        statefResout.setStateUid(state.getUid());
        List<StateResout> stateResouts = stateResoutMapper.select(statefResout);

        for(StateResout stateResout:stateResouts){
            Resout resout =new Resout();
            resout.setUid(stateResout.getResoutUid());
            resout = resoutMapper.selectOne(resout);
            this.getResoutState(resout);
            resouts.add(resout);
        }
        state.setResouts(resouts);

    }


    /**
     * 初始化宠物类型
     * @throws Exception
     */
    public void initPetType(){
        List<PetType> petTypes = petTypeMapper.selectAll();
        for(PetType petType:petTypes){
            //获取ai权重信息
            PetTypeAi petTypeAi = new PetTypeAi();
            petTypeAi.setPetTypeUid(petType.getUid());
            PetTypeAi petTypeAi1 = petTypeAiMapper.selectOne(petTypeAi);
            petType.setPetTypeAi(petTypeAi1);

            String petTypeKey = RedisKeyUtil.getKey(RedisKey.PET_TYPE, petType.getUid());
            redisObjectUtil.save(petTypeKey,petType,-1);
        }
    }

    /**
     * 初始化道具表
     */
    public void initProp(){

        List<Prop> props = propMapper.selectAll();

        for(Prop prop:props){
            String propKey = RedisKeyUtil.getKey(RedisKey.PROP, prop.getUid());
            redisObjectUtil.save(propKey,prop,-1);
        }
    }

    /**
     * 初始化道具表
     */
    public void initVersion(){
        List<VersionControl> versions = versionControlMapper.selectAll();
        for(VersionControl version:versions){
            String propKey = RedisKeyUtil.getKey(RedisKey.VERSION, version.getVersion());
            redisObjectUtil.save(propKey,version,-1);
        }

    }
}
