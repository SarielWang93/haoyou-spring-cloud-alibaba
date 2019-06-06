package com.haoyou.spring.cloud.alibaba.fighting.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.haoyou.spring.cloud.alibaba.commons.domain.*;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.mapper.HiFightingRoomMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.*;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.serialization.JsonSerializer;
import com.haoyou.spring.cloud.alibaba.service.cultivate.CultivateService;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.service.fighting.FightingService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.commons.util.ZIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.*;

@Service(version = "${fighting.service.version}")
@RefreshScope
public class FightingServiceImpl implements FightingService {
    private final static Logger logger = LoggerFactory.getLogger(FightingServiceImpl.class);

    @Reference(version = "${cultivate.service.version}")
    protected CultivateService cultivateService;
    @Autowired
    private HiFightingRoomMapper hiFightingRoomMapper;
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private SendMsgUtil sendMsgUtil;

    @Value("${fighting.alivetime: 300}")
    private long aliveTime;
    @Value("${fighting.skiptime: 50}")
    private long skipTime;
    @Value("${fighting.outlineskiptime: 3}")
    private long outlineSkipTime;
    @Value("${fighting.aitime: 5}")
    private long aiTime;
    @Value("${fighting.initreadytime: 15}")
    private long initReadyTime;
    /**
     * 创建房间，并启动战斗
     *
     * @param users
     * @return
     */
    @Override
    public boolean start(List<User> users) {

        /**
         * 创建房间
         */
        FightingRoom fightingRoom = new FightingRoom(users);
        logger.info(String.format("创建战斗房间：%s", fightingRoom.getUid()));

        /**
         * 初始化阵营
         */
        Map<String, FightingCamp> fightingCamps = new HashMap<>();

        if (users.size() > 1) {
            for (User user : users) {
                FightingCamp fightingCamp = this.initFightingCamp(fightingRoom, user);

                fightingCamps.put(user.getUid(), fightingCamp);
            }
        } else if (users.size() == 1) {
            /**
             * 单人开启战斗，自己的影子
             */
            User user = users.get(0);
            FightingCamp fightingCamp1 = this.initFightingCamp(fightingRoom, user);
            fightingCamps.put(user.getUid(), fightingCamp1);
            FightingCamp fightingCamp2 = this.initFightingCamp(fightingRoom, user);
            User user2 = new User();
            user2.setUid(String.format("ai-%s", user.getUid()));
            user2.setName(String.format("ai-%s", user.getName()));
            user2.setUsername(String.format("ai-%s", user.getUsername()));
            fightingCamp2.setUser(user2);
            fightingCamp2.setAi(true);
            fightingCamp2.setReady(true);
            for (FightingPet fightingPet : fightingCamp2.getFightingPets().values()) {
                fightingPet.setUid(String.format("ai-%s", fightingPet.getUid()));
                fightingPet.setNickname(String.format("ai-%s", fightingPet.getNickname()));
                fightingPet.setAction_time(fightingPet.getAction_time() + 10);
                //fightingPet.setHp(1);
            }

            fightingCamps.put(user2.getUid(), fightingCamp2);
        }
        fightingRoom.setFightingCamps(fightingCamps);

        /**
         * 初始化棋盘
         */
        fightingRoom.setFightingBoard(new FightingBoard(fightingCamps));


        /**
         * 开始战斗,被动技能启动
         */
        //根据行动权排序，按顺序执行被动(红黑树排序)
        TreeMap<Integer, FightingPet> fightingPets = new TreeMap<>();

        for (FightingCamp fightingCamp : fightingCamps.values()) {
            for (FightingPet fightingPet : fightingCamp.getFightingPets().values()) {
                //存放敌我阵营对象
                fightingPet.setDistinguish(this.distinguish(fightingPet));


                /**
                 * 根据action_time排序，防止action_time重复
                 */
                Integer action_time = fightingPet.getAction_time();
                for (int i = action_time; ; i++) {
                    if (fightingPets.get(i) == null) {
                        fightingPets.put(i, fightingPet);
                        fightingPet.setAction_time(i);
                        break;
                    }
                }
            }
        }


        //按行动权顺序执行被动
        for (FightingPet fightingPet : fightingPets.values()) {
            fightingPet.skillsDo(SkillType.OPENING);
            //阵营接受状态以及伤害
            for (FightingCamp fightingCamp : fightingCamps.values()) {
                this.suffer(fightingCamp);
            }
        }
        Map.Entry<Integer, FightingPet> first = fightingPets.firstEntry();
        fightingRoom.startRount(first.getValue());
        /**
         * 序列化存储到redis
         */
        this.saveFightingRoom(fightingRoom);

        doInitReady(fightingRoom.getCampNow());

        return true;
    }


    /**
     * 行动宠物回合开始触发的 状态
     *
     * @param fightingPet 当前回合行动人
     */
    public void startRound(FightingPet fightingPet) {
        logger.debug(String.format("新回合开始：%s %s", fightingPet.getFightingCamp().getUser().getUsername(), fightingPet.getPet().getNickname()));
        //标注当前行动宠物
        FightingRoom fightingRoom = fightingPet.getFightingCamp().getFightingRoom();
        fightingRoom.startRount(fightingPet);
        //启动回合计时，准备跳过或者AI执行
        this.doAI(fightingRoom.getShotNum(), fightingPet.getFightingCamp());
    }

    /**
     * 接受处理信息
     *
     * @param req
     * @return
     */
    @Override
    public MapBody receiveFightingMsg(MyRequest req) {
        MapBody baseMessage = new MapBody();
        int rt = ResponseMsg.MSG_SUCCESS;
        //传递信息
        FightingReq fightingReq = sendMsgUtil.deserialize(req.getMsg(), FightingReq.class);

        logger.debug(String.format("接受到战斗数据：%s", fightingReq));

        /**
         * 获取对战房间对象
         */
        User user = req.getUser();
        FightingRoom fightingRoom = this.getFightingRoomByUserUid(user.getUid(), 10);
        if (fightingRoom == null) {
            logger.debug(String.format("未找到战斗房间：%s", user.getUsername()));
            rt = ResponseMsg.MSG_ERR;
            baseMessage.setState(rt);
            return baseMessage;
        }
        //设置本轮起始步骤
        fightingReq.setStep(fightingRoom.getStep());

        /**
         * 当前执行宠物与接受信息对比，非操作信息处理
         */
        String campNow = fightingRoom.getCampNow();

        if (fightingRoom.getPetNow() != fightingReq.getCurrentPetId() + 1 || !campNow.equals(user.getUid())) {
            rt = ResponseMsg.MSG_ERR;
            /**
             * 玩家索要初始化信息
             */
            if (fightingReq.getCurrentPetId().equals(-10)) {
                logger.debug(String.format("初始化信息：%s", user.getUsername()));
                /**
                 * 发送初始化信息，方便前端同步
                 */
                fightingRoom.sendMsgInit(user.getUid(), sendMsgUtil);
                rt = ResponseMsg.MSG_SUCCESS;
            }
            /**
             * 玩家初始化完成
             */
            else if (fightingReq.getCurrentPetId().equals(-30)) {
                logger.debug(String.format("始化完成：%s", user.getUsername()));
                FightingCamp thisFightingCamp = fightingRoom.getFightingCamps().get(user.getUid());
                thisFightingCamp.setReady(true);

                /**
                 * 棋盘信息
                 */
                fightingReq.setState(1);
                fightingReq.setNewInfos(fightingRoom.getFightingBoard().toListBoard());
                sendMsgUtil.sendMsgList(fightingRoom.getFightingCamps().keySet(), SendType.FIGHTING_REFRESHBOARD, fightingReq);

                //全局信息
                fightingRoom.sendMsgResp(fightingRoom.getFightingCamps().keySet(), sendMsgUtil);


                boolean allRead = true;
                for (FightingCamp fightingCamp : fightingRoom.getFightingCamps().values()) {
                    if (!fightingCamp.isReady()) {
                        allRead = false;
                    }
                }
                if(allRead){
                    //跳过或者ai启动
                    this.doAI(fightingRoom.getShotNum(), fightingRoom.getFightingCamps().get(campNow));

                    sendInitReady(fightingRoom.getFightingCamps().keySet());
                }
            }
            /**
             * 断线重连信息
             */
            else if (fightingReq.getCurrentPetId().equals(-20)) {
                logger.debug(String.format("断线重连：%s", user.getUsername()));
                /**
                 * 棋盘信息
                 */
                fightingReq.setState(1);
                fightingReq.setNewInfos(fightingRoom.getFightingBoard().toListBoard());
                sendMsgUtil.sendMsgList(fightingRoom.getFightingCamps().keySet(), SendType.FIGHTING_REFRESHBOARD, fightingReq);

                //全局信息
                fightingRoom.sendMsgResp(fightingRoom.getFightingCamps().keySet(), sendMsgUtil);

                rt = ResponseMsg.MSG_SUCCESS;
            }

            /**
             * 刷新redis对战房间对象
             */
            this.saveFightingRoom(fightingRoom);

            baseMessage.setState(rt);
            return baseMessage;
        }


        this.doOperation(fightingRoom, fightingReq);

        baseMessage.setState(rt);
        return baseMessage;
    }

    /**
     * 初始化成功计时
     */
    private void doInitReady(String userUid){
        ThreadUtil.excAsync(() -> {
            try {
                Thread.sleep(this.initReadyTime*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            FightingRoom fightingRoom = this.getFightingRoomByUserUid(userUid, 5);

            List<String> userUids = new LinkedList<>();
            for (FightingCamp fightingCamp : fightingRoom.getFightingCamps().values()) {
                if (fightingCamp.isReady()) {
                    userUids.add(fightingCamp.getUser().getUid());
                }
            }
            if(fightingRoom.getFightingCamps().values().size()>userUids.size()){
                sendInitReady(userUids);
            }

            this.saveFightingRoom(fightingRoom);

        },true);
    }

    /**
     * 执行发送初始化成功
     * @param userUids
     */
    private void sendInitReady(Collection<String> userUids){
        MapBody baseMessage = new MapBody();
        int rt = ResponseMsg.MSG_SUCCESS;
        baseMessage.setState(rt);
        sendMsgUtil.sendMsgList(userUids, SendType.FIGHTING_INIT, baseMessage);
    }

    /**
     * 执行操作
     *
     * @param fightingRoom
     * @param fightingReq
     */
    private void doOperation(FightingRoom fightingRoom, FightingReq fightingReq) {
        FightingCamp own = fightingRoom.getFightingCamps().get(fightingRoom.getCampNow());
        FightingPet fightingPet = own.getFightingPets().get(fightingRoom.getPetNow());
        FightingCamp enemy = fightingPet.getDistinguish().get("enemy");
        logger.debug(String.format("进入操作方法开始操作！"));
        //校验并计算操作块
        if (!fightingRoom.getFightingBoard().check(fightingPet, fightingReq)) {
            logger.debug(String.format("块操作校验失败：%s", fightingReq));
            this.saveFightingRoom(fightingRoom);
            /**
             * 发送信息
             */
            fightingRoom.sendMsgResp(fightingRoom.getFightingCamps().keySet(), sendMsgUtil);
            return;
        }

        /**
         * 棋盘刷新
         */
        if (fightingRoom.getFightingBoard().refrashBoard(fightingPet, fightingReq)) {
            logger.debug(String.format("刷新棋盘：%s", fightingReq));
            /**
             * 发送刷新后的棋盘信息
             */
            sendMsgUtil.sendMsgList(fightingRoom.getFightingCamps().keySet(), SendType.FIGHTING_REFRESHBOARD, fightingReq);
        }
        /**
         * 添加出手信息
         */
        fightingPet.addShot(fightingReq);

        //出手之前记录行动权
        int action_time = fightingPet.getAction_time();

        /**
         * 出手宠物行动
         */
        this.operation(fightingReq, fightingPet);


        /**
         * 阵营接受状态以及伤害（包括己方和对方）
         */
        for (Map.Entry<String, FightingCamp> entry : fightingPet.getDistinguish().entrySet()) {
            List<FightingPet> die = this.suffer(entry.getValue());
            /**
             * 击杀触发状态执行
             */
            if (entry.getKey().equals("enemy") && die.size() > 0) {
                fightingPet.kill(die);
            }
        }


        /**
         * 非当前宠物 行动权计算（红黑树TreeMap）
         */
        TreeMap<Integer, FightingPet> fightingPets = new TreeMap<>();

        for (FightingCamp value : fightingPet.getDistinguish().values()) {
            for (FightingPet value1 : value.getFightingPets().values()) {
                if (!value1.getUid().equals(fightingPet.getUid()) && value1.getHp() > 0) {

                    Integer action_time1 = value1.getAction_time();
                    int newAction_time = action_time1 - action_time;
                    value1.setAction_time(newAction_time);

                    fightingPets.put(newAction_time, value1);
                }
                //清理临时状态
                value1.removeState(StateType.TEMPORARY);
            }
        }

        for (int i = fightingPet.getAction_time(); ; i++) {
            if (fightingPets.get(i) == null) {
                fightingPets.put(i, fightingPet);
                fightingPet.setAction_time(i);
                break;
            }
        }


        /**
         * 胜利
         */
        boolean win = true;
        for (FightingPet enemyPet : enemy.getFightingPets().values()) {
            if (enemyPet.getHp() > 0) {
                win = false;
            }
        }
        if (win) {
            win(fightingPet);
            return;
        }


        /**
         * 以上为上一回合结束，以下为下一回合开始
         */


        /**
         * 行动宠物回合开始
         */
        Map.Entry<Integer, FightingPet> first = fightingPets.firstEntry();
        this.startRound(first.getValue());
        /**
         * 发送信息
         */
        fightingRoom.sendMsgResp(fightingRoom.getFightingCamps().keySet(), sendMsgUtil);

        /**
         * 刷新redis对战房间对象
         */
        this.saveFightingRoom(fightingRoom);


    }

    /**
     * 计时跳过/AI操作
     *
     * @param shotNum
     * @param thisCamp
     */
    private void doAI(Integer shotNum, FightingCamp thisCamp) {

        ThreadUtil.excAsync(() -> {
            String userUid = thisCamp.getUser().getUid();

            try {
                long sleepTime = skipTime * 1000;
                if (thisCamp.isAi()) {
                    sleepTime = aiTime * 1000;
                } else if (!sendMsgUtil.connectionIsAlive(userUid)) {
                    sleepTime = outlineSkipTime * 1000;
                }
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            FightingRoom fightingRoom = this.getFightingRoomByUserUid(userUid, 5);

            if (fightingRoom != null) {
                if (fightingRoom.getShotNum() == shotNum) {
                    if (thisCamp.isAi())
                    //ai操作
                    {
                        doAI(fightingRoom, userUid);
                    } else {
                        boolean hasAlive = false;
                        for (FightingCamp fightingCamp : fightingRoom.getFightingCamps().values()) {
                            if (sendMsgUtil.connectionIsAlive(fightingCamp.getUser().getUid())) {
                                hasAlive = true;
                                break;
                            }
                        }
                        if (hasAlive) {
                            //执行跳过操作
                            FightingReq fightingReq = new FightingReq();
                            fightingReq.setDestroyInfos(new ArrayList<>());
                            //fightingReq.setFightingRoomUid(fightingRoom.getUid());
                            doOperation(fightingRoom, fightingReq);
                        }else{
                            this.saveFightingRoom(fightingRoom);
                        }
                    }
                    return;
                }
                /**
                 * 刷新redis对战房间对象
                 */
                this.saveFightingRoom(fightingRoom);
            }

        }, true);
    }

    /**
     * AI操作
     *
     * @param fightingRoom
     * @param userUid
     */
    private void doAI(FightingRoom fightingRoom, String userUid) {
        FightingCamp own = fightingRoom.getFightingCamps().get(fightingRoom.getCampNow());
        FightingPet fightingPet = own.getFightingPets().get(fightingRoom.getPetNow());
        //根据宠物种类获取ai权重
        String petTypeKey = RedisKeyUtil.getKey(RedisKey.PET_TYPE, fightingPet.getPet().getTypeUid());
        PetType petType = redisObjectUtil.get(petTypeKey, PetType.class);
        PetTypeAi petTypeAi = petType.getPetTypeAi();

        //执行AI操作
        FightingReq fightingReq = new FightingReq();

        //权重
        Integer attack = petTypeAi.getAttack();
        Integer specialAttack = petTypeAi.getSpecialAttack();
        Integer shield = petTypeAi.getShield();
        Integer skill = petTypeAi.getSkill();
        //能量值满了必杀
        if (own.getEnergy() == FightingCamp.MAX_ENERGY && fightingPet.getSkillsByType(SkillType.UNIQUE).size() > 0) {
            attack += FightingCamp.MAX_ENERGY * 10;
        }

        //获取ai连的块
        fightingReq.setDestroyInfos(fightingRoom.getFightingBoard().doAI(fightingPet, userUid, attack, specialAttack, shield, skill));
        //fightingReq.setFightingRoomUid(fightingRoom.getUid());
        doOperation(fightingRoom, fightingReq);
    }


    /**
     * 从redis中获取对战房间对象
     *
     * @param userUid
     * @param times   尝试次数
     * @return
     */
    public FightingRoom getFightingRoomByUserUid(String userUid, int times) {

        for (int i = 0; i < times; i++) {
            String fightingRoomUid = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PLAYER_FIGHTING_ROOM, userUid), String.class);
            FightingRoom fightingRoom = null;
            if (StrUtil.isEmpty(fightingRoomUid)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            fightingRoom = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.FIGHTING_ROOM, fightingRoomUid), FightingRoom.class);
            if (fightingRoom == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            } else {
                this.deleteFightingRoom(fightingRoom);
            }

            for (FightingCamp fightingCamp : fightingRoom.getFightingCamps().values()) {
                fightingCamp.setFightingRoom(fightingRoom);

                for (FightingPet fightingPet : fightingCamp.getFightingPets().values()) {
                    fightingPet.setFightingCamp(fightingCamp);
                    fightingPet.setRedisObjectUtil(redisObjectUtil);
                    fightingPet.setDistinguish(this.distinguish(fightingPet));
                }

            }

            //刷新当前步骤
            fightingRoom.setNowSteps(new TreeMap<>());
            return fightingRoom;
        }
        return null;
    }


    /**
     * redis存储
     */
    public boolean saveFightingRoom(FightingRoom fightingRoom) {
        //刷新当前步骤
        fightingRoom.setNowSteps(new TreeMap<>());
        for (FightingCamp fightingCamp : fightingRoom.getFightingCamps().values()) {
            redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.PLAYER_FIGHTING_ROOM, fightingCamp.getUser().getUid()), fightingRoom.getUid(), this.aliveTime);
        }
        return redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.FIGHTING_ROOM, fightingRoom.getUid()), fightingRoom, this.aliveTime);
    }

    /**
     * redis删除
     */
    public boolean deleteFightingRoom(FightingRoom fightingRoom) {
        for (FightingCamp fightingCamp : fightingRoom.getFightingCamps().values()) {
            redisObjectUtil.delete(RedisKeyUtil.getKey(RedisKey.PLAYER_FIGHTING_ROOM, fightingCamp.getUser().getUid()));
        }
        return redisObjectUtil.delete(RedisKeyUtil.getKey(RedisKey.FIGHTING_ROOM, fightingRoom.getUid()));
    }

    /**
     * 初始化阵营信息
     *
     * @param user
     * @return
     */

    public FightingCamp initFightingCamp(FightingRoom fightingRoom, User user) {
        FightingCamp fightingCamp = new FightingCamp();
        fightingCamp.setFightingRoom(fightingRoom);
        fightingCamp.setUser(user);
        TreeMap<Integer, FightingPet> fightingPetMaps = new TreeMap<>();

        List<FightingPet> fightingPets = FightingPet.getByUser(user, redisObjectUtil);

        for (FightingPet fightingPet : fightingPets) {
            Integer iswork = fightingPet.getIswork();
            if (iswork != null && iswork != 0) {
                fightingPet.setFightingCamp(fightingCamp);
                fightingPet.initFighting();
                fightingPetMaps.put(iswork, fightingPet);
            }
        }
        fightingCamp.setFightingPets(fightingPetMaps);
        return fightingCamp;
    }


    /**
     * 区分敌我
     *
     * @param fightingPet
     * @return
     */

    public Map<String, FightingCamp> distinguish(FightingPet fightingPet) {
        Map<String, FightingCamp> rm = new HashMap<>();
        //区分己方与对方
        Map<String, FightingCamp> fightingCamps = fightingPet.getFightingCamp().getFightingRoom().getFightingCamps();

        for (Map.Entry<String, FightingCamp> entry : fightingCamps.entrySet()) {
            String key = "";
            if (entry.getKey().equals(fightingPet.getFightingCamp().getUser().getUid())) {
                key = "own";
            } else {
                key = "enemy";
            }
            rm.put(key, entry.getValue());
        }

        return rm;

    }


    /**
     * 阵营接收处理到的状态以及伤害（受）
     *
     * @param fightingCamp
     * @return 击杀的FightingPet
     */

    public List<FightingPet> suffer(FightingCamp fightingCamp) {
        List<FightingPet> die = new ArrayList<>();

        List<FightingState> acceptStates = fightingCamp.getAcceptStates();

        FightingPet acceptHurtFrom = fightingCamp.getAcceptHurtFrom();
        //主伤害
        Integer acceptHurt = fightingCamp.getAcceptHurt();


        /**
         * 获取活着的承受对象（按顺位）,传递伤害值
         */
        List<FightingPet> sufferFightingPets = fightingCamp.getAlive();

        sufferFightingPets.get(0).setAcceptHurtFrom(acceptHurtFrom);
        sufferFightingPets.get(0).setAcceptHurt(acceptHurt);


        /**
         * 接受状态
         */
        if (acceptStates.size() > 0) {
            for (FightingState fightingState : acceptStates) {
                //作用人数
                Integer numType = fightingState.getNumType();
                //成功率
                Integer rateType = fightingState.getRateType();

                if (numType < 0) {
                    numType = -numType;
                }
                //向宠物传递状态
                int petNum = sufferFightingPets.size();
                if (petNum < numType) {
                    numType = petNum;
                }
                for (int i = 0; i < numType; i++) {
                    //概率计算
                    int random = RandomUtil.randomInt(100);
                    if (random < rateType) {
                        sufferFightingPets.get(i).addFightingState(fightingState);
                    }
                }
            }
            //刷新接受状态队列
            fightingCamp.setAcceptStates(new ArrayList<>());
        }


        /**
         * 接受伤害
         */
        for (FightingPet fightingPet : sufferFightingPets) {
            /**
             * 刷新战斗属性
             */
            fightingPet.refreshFt();

            //判断是否受到伤害
            if (fightingPet.getAcceptHurt() > 0) {
                /**
                 * 造成伤害,返回是否死亡
                 */
                if (fightingPet.hurt()) {
                    die.add(fightingPet);
                }

            }


        }


        //归0伤害
        fightingCamp.setAcceptHurt(0);
        fightingCamp.setAcceptHurtFrom(null);


        return die;
    }


    /**
     * 操作玩家执行
     *
     * @param fightingReq
     * @param fightingPet
     */

    public void operation(FightingReq fightingReq, FightingPet fightingPet) {


        FightingCamp own = fightingPet.getDistinguish().get("own");
        //能量值（必杀技逻辑）
        int energy = own.getEnergy();
        //能量最大值
        final int MAX_ENERGY = FightingCamp.MAX_ENERGY;

        //根据状态刷新战斗属性
        fightingPet.refreshFt();

        //块的种类
        Integer blockType = fightingReq.getBlockType();
        //块的个数
        Integer blockCount = fightingReq.getBlockCount();

        //存储实际消除个数
        int all = blockCount;
        if (blockCount > 7) {
            blockCount = 7;
        }
        //惩罚值
        int punishValue = 5 * blockCount;

        boolean skip = false;
        /**
         * 根据块的种类执行相应的操作
         */
        switch (blockType) {
            // 普通攻击
            case FightingBoard.ATTACK_NORMAL:
                //必杀技
                if (energy == MAX_ENERGY && fightingPet.getSkillsByType(SkillType.UNIQUE).size() > 0) {
                    fightingPet.skillsDo(SkillType.UNIQUE, blockCount);
                    energy = -1;
                    punishValue += PunishValue.UNIQUE;
                } else {
                    /**
                     * 记录步骤
                     */
                    fightingPet.addStep(FightingStep.DO_ATTACK, "");

                    fightingPet.attackAction(blockCount);
                    punishValue += PunishValue.ATTACK_NORMAL;
                }
                break;
            // 特殊攻击
            case FightingBoard.ATTACK_SPECIAL:
                //执行特殊攻击技能
                fightingPet.skillsDo(SkillType.SPECIAL_ATTACK, blockCount);
                punishValue += PunishValue.ATTACK_SPECIAL;
                break;
            //增加护盾值操作
            case FightingBoard.SHIELD:
                fightingPet.shieldAction(blockCount);
                punishValue += PunishValue.SHIELD;
                break;
            //发动技能
            case FightingBoard.SKILL:
                //查找主动技能，并发动
                fightingPet.skillsDo(SkillType.ACTIVE, blockCount);
                punishValue += PunishValue.SKILL;
                break;
            default:
                //跳过
                skip = true;
                //记录步骤
                fightingPet.addStep(FightingStep.SKIP, "");
                //跳过的惩罚值
                punishValue = PunishValue.SKIP;
        }


        if (!skip) {
            //不跳过执行
            fightingPet.removeState(StateType.OPERATION);
        }

        /**
         *  能量值增加逻辑（暂定）
         */
        //是否必杀回合
        if (energy < 0) {
            energy = 0;
        }
        //非必杀回合
        else {
            energy += all;
            //不超过上限
            if (energy > MAX_ENERGY) {
                energy = MAX_ENERGY;
            }
        }
        own.setEnergy(energy);


        /**
         * 刷新行动权
         */
        fightingPet.reflashActionTime(punishValue);

    }


    public void win(FightingPet fightingPet) {

        FightingRoom fightingRoom = fightingPet.getFightingCamp().getFightingRoom();
        fightingPet.addStep(FightingStep.VICTORY, "");
        /**
         * 发送信息
         */
        fightingRoom.sendMsgResp(fightingRoom.getFightingCamps().keySet(), sendMsgUtil);


        User user = fightingPet.getFightingCamp().getUser();
        logger.debug(String.format("胜利：%s", user.getUsername()));
        //TODO 胜利结算逻辑（临时）获取“技能道具”
        if (!fightingPet.getUid().startsWith("ai-") && sendMsgUtil.connectionIsAlive(user.getUid())) {
            //PVE胜利结算
            cultivateService.rewards(user, RewardType.PVE);

            //结算完毕，保存
            this.hiSave(fightingPet.getFightingCamp());

        }

    }

    /**
     * 结算历史信息保存到数据库
     */
    public void hiSave(FightingCamp fightingCamp) {

        HiFightingRoom hiFightingRoom = new HiFightingRoom();
        hiFightingRoom.setUid(IdUtil.simpleUUID());


        FightingRoom fightingRoom = fightingCamp.getFightingRoom();
        hiFightingRoom.setPlayer1(fightingRoom.getFightingBoard().getLeftPlayerUid());
        hiFightingRoom.setPlayer2(fightingRoom.getFightingBoard().getRightPlayerUid());

        fightingRoom.setOverTime(new Date());
        hiFightingRoom.setCreatTime(fightingRoom.getCreatTime());
        hiFightingRoom.setOverTime(fightingRoom.getOverTime());

        hiFightingRoom.setFightingRoomJson(JsonSerializer.serializes(fightingRoom));

        hiFightingRoomMapper.insertSelective(hiFightingRoom);

    }


}