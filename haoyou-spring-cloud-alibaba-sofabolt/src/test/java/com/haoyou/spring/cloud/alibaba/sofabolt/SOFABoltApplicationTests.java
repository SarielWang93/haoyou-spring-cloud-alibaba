package com.haoyou.spring.cloud.alibaba.sofabolt;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.lang.Console;
import com.alipay.remoting.Connection;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.mapper.*;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SOFABoltApplicationTests {
    @Autowired
    private UserUtil userUtil;
    @Autowired
    private RedisObjectUtil redisObjectUtil;
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

    @Before
    public void befor() {

    }

    @Test
    public void contextLoads() throws RemotingException, InterruptedException {
        Skill skill = skillMapper.selectByPrimaryKey(9);
        getSkillResout(skill);
        byte[] serialize = redisObjectUtil.serialize(skill);
        redisObjectUtil.save("www",skill);

        Skill deserialize = redisObjectUtil.deserialize(serialize, Skill.class);
        Console.log(deserialize);
        Skill skill1 = redisObjectUtil.get("skill:c4ff4a96c2785ddfb10ea694db083d12", Skill.class);
        Console.log(skill1);

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


    @Test
    public void contextLoads2() throws RemotingException, InterruptedException {

    }

    public static void main(String[] args) {

    }
}
