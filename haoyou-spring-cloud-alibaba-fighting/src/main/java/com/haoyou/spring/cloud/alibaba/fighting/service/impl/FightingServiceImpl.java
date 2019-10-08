package com.haoyou.spring.cloud.alibaba.fighting.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.util.ScoreRankUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import com.haoyou.spring.cloud.alibaba.commons.domain.*;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.mapper.HiFightingRoomMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.*;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.service.cultivate.CultivateService;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.service.fighting.FightingService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.lang.reflect.Field;
import java.util.*;

@Service(version = "${fighting.service.version}")
@RefreshScope
public class FightingServiceImpl implements FightingService {
    private final static Logger logger = LoggerFactory.getLogger(FightingServiceImpl.class);

    public final static int INIT_MSG = -10;//索要初始化信息
    public final static int INIT_OVER = -30;//汇报初始化完成
    public final static int RECON = -20;//断线重连
    public final static int AI_DO = -40;//ai执行

    @Reference(version = "${cultivate.service.version}")
    protected CultivateService cultivateService;
    @Autowired
    private HiFightingRoomMapper hiFightingRoomMapper;
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private SendMsgUtil sendMsgUtil;
    @Autowired
    private UserUtil userUtil;
    @Autowired
    protected ScoreRankUtil scoreRankUtil;

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


    public void initFightingRoom(List<User> users, FightingRoom fightingRoom, Map<String, Boolean> allIsAi) {
        /**
         * 创建房间
         */

        logger.info(String.format("创建战斗房间：%s", fightingRoom.getUid()));

        /**
         * 初始化阵营
         */
        Map<String, FightingCamp> fightingCamps = new HashMap<>();


        for (User user : users) {
            /**
             * 删除旧的战斗
             */
            String fightingRoomOldUid = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PLAYER_FIGHTING_ROOM, user.getUid()), String.class);
            if (StrUtil.isNotEmpty(fightingRoomOldUid)) {
                FightingRoom fightingRoomOld = this.getFightingRoomByUserUid(user.getUid(), 2);
                if (fightingRoomOld != null) {
                    this.deleteFightingRoom(fightingRoomOld);
                }
            }


            FightingCamp fightingCamp = this.initFightingCamp(fightingRoom, user);

            if (allIsAi.get(user.getUid()) != null && allIsAi.get(user.getUid())) {
                fightingCamp.setAi(true);
            } else {
                fightingCamp.setAi(false);
            }

            fightingCamps.put(user.getUid(), fightingCamp);


            //PVE创建电脑对手
            if (users.size() == 1) {
                User user2 = new User();
                user2.setUid(String.format("ai-%s", user.getUid()));
                user2.setUsername(String.format("ai-%s", user.getUsername()));

                FightingCamp fightingCamp2 = this.initFightingCampAi(fightingRoom, user2);

                fightingCamps.put(user2.getUid(), fightingCamp2);
            }
        }
        fightingRoom.setFightingCamps(fightingCamps);


        /**
         * 初始化棋盘
         */
        fightingRoom.setFightingBoard(new FightingBoard(fightingCamps));
    }


    @Override
    public boolean start(User user, String chapterName, int idNum, int difficult) {
        return start(user, chapterName, idNum, difficult, false, false);
    }

    @Override
    public boolean start(User user, String chapterName, int idNum, int difficult, boolean isWin) {
        return start(user, chapterName, idNum, difficult, false, isWin);
    }

    /**
     * pve闯关模式
     *
     * @param user        玩家对象
     * @param chapterName 章节名称
     * @param idNum       关卡序号
     * @param difficult   ordinary difficulty crazy
     *                    普通       困难     疯狂
     * @param isAi        是ai操作
     * @param isWin       是否扫荡
     * @return
     */
    @Override
    public boolean start(User user, String chapterName, int idNum, int difficult, boolean isAi, boolean isWin) {

        List<User> users = new ArrayList<>();
        users.add(user);
        String levelDesignKey = RedisKeyUtil.getKey(RedisKey.LEVEL_DESIGN, chapterName, Integer.toString(idNum));
        LevelDesign levelDesign = redisObjectUtil.get(levelDesignKey, LevelDesign.class);

        FightingRoom fightingRoom = new FightingRoom(levelDesign, difficult, FightingType.PVE);

        HashMap<String, Boolean> allIsAi = new HashMap<>();
        allIsAi.put(user.getUid(), isAi);
        this.initFightingRoom(users, fightingRoom, allIsAi);

        cultivateService.numericalAdd(user, "daily_pve", 1L);
        cultivateService.numericalAdd(user, "pve_count", 1L);

        if (isWin) {
            this.win(fightingRoom.getFightingCamps().get(user.getUid()).getFightingPets().get(1), false);
            return true;
        }

        return this.start(fightingRoom);
    }

    /**
     * 天梯模式
     *
     * @param users
     * @return
     */
    @Override
    public boolean start(List<User> users) {
        return start(users, new HashMap<>());
    }

    @Override
    public boolean start(List<User> users, Map<String, Boolean> allIsAi) {
        if (users.size() < 2) {
            return false;
        }
        //从新获取user信息
        List<User> users1 = new ArrayList<>();
        for (User user : users1) {
            users1.add(userUtil.getUserByUid(user.getUid()));
        }

        FightingRoom fightingRoom = new FightingRoom(FightingType.PVP);
        this.initFightingRoom(users1, fightingRoom, allIsAi);

        if (this.start(fightingRoom)) {
            //增加一次天梯记录
            for (User user : users1) {
                cultivateService.numericalAdd(user, "daily_ladder", 1L);
            }

            return true;
        }
        return false;
    }

    /**
     * 启动战斗
     */
    public boolean start(FightingRoom fightingRoom) {

        Map<String, FightingCamp> fightingCamps = fightingRoom.getFightingCamps();

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
        logger.debug(String.format("新回合开始：%s %s", fightingPet.getFightingCamp().getUser().getUsername(), fightingPet.getPet().getNickName()));
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
        FightingReq fightingReq = sendMsgUtil.deserialize(req.getMsg(), FightingReq.class, false);

        logger.debug(String.format("接受到战斗数据：%s", fightingReq));

        /**
         * 获取对战房间对象
         */
        User user = req.getUser();
        FightingRoom fightingRoom = this.getFightingRoomByUserUid(user.getUid(), 5);
        if (fightingRoom == null) {
            logger.debug(String.format("未找到战斗房间：%s", user.getUsername()));
            rt = ResponseMsg.MSG_NOT_FIND_FIGHTING_ROOM;
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
            if (fightingReq.getCurrentPetId().equals(INIT_MSG)) {
                logger.debug(String.format("初始化信息：%s", user.getUsername()));
                /**
                 * 发送初始化信息，方便前端同步
                 */
                fightingRoom.sendMsgInit(user.getUid(), sendMsgUtil);
                rt = ResponseMsg.MSG_SUCCESS;
            }
            /**
             * 玩家动画完成跑AI
             */
            else if (fightingReq.getCurrentPetId().equals(AI_DO)) {
                if (campNow.startsWith("ai-")) {
                    this.doAI(fightingRoom);
                    rt = ResponseMsg.MSG_SUCCESS;
                    baseMessage.setState(rt);
                    return baseMessage;
                }
            }
            /**
             * 玩家初始化完成
             */
            else {
                /**
                 * 棋盘信息
                 */
                fightingReq.setState(1);
                fightingReq.setNewInfos(fightingRoom.getFightingBoard().toListBoard());
                sendMsgUtil.sendMsgList(fightingRoom.getFightingCamps().keySet(), SendType.FIGHTING_REFRESHBOARD, fightingReq);
                //全局信息
                fightingRoom.sendMsgResp(fightingRoom.getFightingCamps().keySet(), sendMsgUtil);


                if (fightingReq.getCurrentPetId().equals(INIT_OVER)) {
                    logger.debug(String.format("始化完成：%s", user.getUsername()));
                    FightingCamp thisFightingCamp = fightingRoom.getFightingCamps().get(user.getUid());
                    thisFightingCamp.setReady(true);
                    boolean allRead = true;
                    for (FightingCamp fightingCamp : fightingRoom.getFightingCamps().values()) {
                        if (!fightingCamp.isReady()) {
                            allRead = false;
                        }
                    }
                    if (allRead) {
                        //跳过或者ai启动
                        this.doAI(fightingRoom.getShotNum(), fightingRoom.getFightingCamps().get(campNow));

                        sendInitReady(fightingRoom.getFightingCamps().keySet());
                    }
                    rt = ResponseMsg.MSG_SUCCESS;
                }
                /**
                 * 断线重连信息
                 */
                else if (fightingReq.getCurrentPetId().equals(RECON)) {
                    logger.debug(String.format("断线重连：%s", user.getUsername()));

                    //跳过或者ai启动
                    this.doAI(fightingRoom.getShotNum(), fightingRoom.getFightingCamps().get(campNow));

                    rt = ResponseMsg.MSG_SUCCESS;
                }
            }

            /**
             * 刷新redis对战房间对象
             */
            this.saveFightingRoom(fightingRoom);

            baseMessage.setState(rt);
            return baseMessage;
        }

        fightingReq.setUser(user);

        this.doOperation(fightingRoom, fightingReq);

        baseMessage.setState(rt);
        return baseMessage;
    }

    /**
     * 初始化成功计时
     */
    private void doInitReady(String userUid) {
        ThreadUtil.excAsync(() -> {
            try {
                Thread.sleep(this.initReadyTime * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            FightingRoom fightingRoom = this.getFightingRoomByUserUid(userUid, 5);

            if (fightingRoom == null) {
                return;
            }

            List<String> userUids = new LinkedList<>();
            for (FightingCamp fightingCamp : fightingRoom.getFightingCamps().values()) {
                if (fightingCamp.isReady()) {
                    userUids.add(fightingCamp.getUser().getUid());
                } else {
                    fightingCamp.setReady(true);
                }
            }
            if (fightingRoom.getFightingCamps().values().size() > userUids.size()) {
                sendInitReady(userUids);
                //跳过或者ai启动
                String campNow = fightingRoom.getCampNow();
                this.doAI(fightingRoom.getShotNum(), fightingRoom.getFightingCamps().get(campNow));
            }

            this.saveFightingRoom(fightingRoom);

        }, true);
    }

    /**
     * 执行发送初始化成功
     *
     * @param userUids
     */
    private void sendInitReady(Collection<String> userUids) {
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
        if (fightingRoom.getFightingBoard().refrashBoard(fightingPet, fightingReq, cultivateService)) {
            logger.debug(String.format("刷新棋盘：%s", fightingReq));
            /**
             * 发送刷新后的棋盘信息
             */
            fightingReq.setState(ResponseMsg.MSG_SUCCESS);
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

        List<FightingPet> dieEnemy = this.suffer(enemy);
        List<FightingPet> dieOwn = this.suffer(own);
        /**
         * 击杀触发状态执行
         */
        if (!dieEnemy.isEmpty()) {
            fightingPet.kill(dieEnemy);
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
         * 非当前宠物 行动权计算（红黑树TreeMap）
         */
        TreeMap<Integer, FightingPet> fightingPets = new TreeMap<>();

        for (FightingCamp value : fightingPet.getDistinguish().values()) {
            for (FightingPet value1 : value.getFightingPets().values()) {
                if (value1.getHp() > 0 && !value1.getUid().equals(fightingPet.getUid())) {
                    Integer action_time1 = value1.getAction_time();
                    int newAction_time = action_time1 - action_time;
                    value1.setAction_time(newAction_time);
                    fightingPets.put(newAction_time, value1);

                    //清除暴击状态
                    if (value1.isLuky()) {
                        fightingPet.setLuky(false);
                    }
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
                if (fightingRoom.getShotNum() == shotNum && thisCamp.getFightingRoom().getUid().equals(fightingRoom.getUid())) {
                    boolean hasAlive = false;
                    for (FightingCamp fightingCamp : fightingRoom.getFightingCamps().values()) {
                        if (sendMsgUtil.connectionIsAlive(fightingCamp.getUser().getUid())) {
                            hasAlive = true;
                            break;
                        }
                    }
                    if (hasAlive) {
                        if (thisCamp.isAi())
                        //ai操作
                        {
                            doAI(fightingRoom);
                        }
                        //执行跳过操作
                        else {
                            FightingReq fightingReq = new FightingReq();
                            fightingReq.setDestroyInfos(new ArrayList<>());
                            fightingReq.setUser(thisCamp.getUser());
                            doOperation(fightingRoom, fightingReq);
                        }
                        return;
                    }
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
     */
    private void doAI(FightingRoom fightingRoom) {
        FightingCamp own = fightingRoom.getFightingCamps().get(fightingRoom.getCampNow());
        FightingPet fightingPet = own.getFightingPets().get(fightingRoom.getPetNow());
        //根据宠物种类获取ai权重
        String petTypeKey = RedisKeyUtil.getKey(RedisKey.PET_TYPE, fightingPet.getPet().getTypeUid());
        PetType petType = redisObjectUtil.get(petTypeKey, PetType.class);
        PetTypeAi petTypeAi = petType.getPetTypeAi();

        //执行AI操作
        FightingReq fightingReq = new FightingReq();
        fightingReq.setUser(own.getUser());
        //获取ai连的块
        fightingReq.setDestroyInfos(fightingRoom.getFightingBoard().doAI(fightingPet, own, petTypeAi));
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
        String fightingRoomUid = null;
        FightingRoom fightingRoom = null;
        for (int i = 0; i < times; i++) {
            if (StrUtil.isEmpty(fightingRoomUid)) {
                fightingRoomUid = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PLAYER_FIGHTING_ROOM, userUid), String.class);
            }
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
                redisObjectUtil.delete(RedisKeyUtil.getKey(RedisKey.FIGHTING_ROOM, fightingRoom.getUid()));
            }

            for (FightingCamp fightingCamp : fightingRoom.getFightingCamps().values()) {
//                fightingCamp.setFightingRoom(fightingRoom);
                for (FightingPet fightingPet : fightingCamp.getFightingPets().values()) {
//                    fightingPet.setFightingCamp(fightingCamp);
                    fightingPet.setRedisObjectUtil(redisObjectUtil);
//                    fightingPet.setDistinguish(this.distinguish(fightingPet));
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
     * 初始化阵营信息 player
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
            Integer iswork = fightingPet.getPet().getIswork();
            if (iswork != null && iswork != 0) {
                fightingPet.setFightingCamp(fightingCamp);
                fightingPet.initFighting();
                fightingPetMaps.put(iswork, fightingPet);
            }
        }

        // 助阵宠物设置
        String fightingType = fightingRoom.getFightingType();
        if (fightingType.equals(FightingType.PVE)) {
            String key = RedisKeyUtil.getlkKey(RedisKey.HELP_PET, user.getUid(), RedisKey.HELP);
            HashMap<String, String> stringStringHashMap = redisObjectUtil.getlkMap(key, String.class);
            if (stringStringHashMap.size() == 1) {
                for (String value : stringStringHashMap.values()) {
                    //value是助战玩家uid和助战位置的用":"拼接
                    String[] split = value.split(":");
                    int iswork = Integer.parseInt(split[1]);

                    User friendUser = userUtil.getUserByUid(split[0]);
                    String helpPetUid = friendUser.getUserData().getHelpPetUid();
                    FightingPet fightingPet = FightingPet.getByUserAndPetUid(friendUser, helpPetUid, redisObjectUtil);

                    //临时修改宠物信息
                    fightingPet.getPet().setIswork(iswork);
                    fightingPet.getPet().setUserUid(user.getUid());

                    fightingPet.setFightingCamp(fightingCamp);
                    fightingPet.initFighting();
                    fightingPetMaps.put(iswork, fightingPet);
                    //记录当日已助战好友
                    String hashKey = RedisKeyUtil.getKey(RedisKey.HELP_PET, user.getUid(), RedisKey.HAS_HELP, split[0]);
                    redisObjectUtil.save(hashKey, split[0]);

                    //增加5点亲密度
                    cultivateService.addIntimacy(user, split[0], 5L);

                }
            }
            //助战已使用清理
            String lkKey = RedisKeyUtil.getlkKey(RedisKey.HELP_PET, user.getUid(), RedisKey.HELP);
            redisObjectUtil.deleteAll(lkKey);
        }


        fightingCamp.setFightingPets(fightingPetMaps);
        return fightingCamp;
    }

    /**
     * 初始化阵营信息 AI
     *
     * @param fightingRoom
     * @param aiUser
     * @return
     */
    public FightingCamp initFightingCampAi(FightingRoom fightingRoom, User aiUser) {

        LevelDesign levelDesign = fightingRoom.getLevelDesign();
        int difficult = fightingRoom.getDifficult();

        FightingCamp fightingCamp = new FightingCamp();
        fightingCamp.setFightingRoom(fightingRoom);
        fightingCamp.setUser(aiUser);


        TreeMap<Integer, FightingPet> fightingPetMaps = new TreeMap<>();


        List<FightingPet> fightingPets = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {

            Field petTypeField = ReflectUtil.getField(LevelDesign.class, String.format("petType%s", i));
            String petTypeUid = (String) ReflectUtil.getFieldValue(levelDesign, petTypeField);

            Field petLevelField = ReflectUtil.getField(LevelDesign.class, String.format("petLevel%s", i));
            Integer petLevel = (Integer) ReflectUtil.getFieldValue(levelDesign, petLevelField);

            petLevel = ((100 + 50 * difficult)*petLevel / 100);

            PetType petType = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.PET_TYPE, petTypeUid), PetType.class);

            FightingPet fightingPet = new FightingPet(petType, aiUser, i, petLevel, redisObjectUtil);

            fightingPet.setFightingCamp(fightingCamp);
            fightingPet.initFighting();
            //ai削弱行动权
            fightingPet.setAction_time(fightingPet.getAction_time() + 10);
            fightingPetMaps.put(i, fightingPet);

            fightingPets.add(fightingPet);

        }

        fightingCamp.setFightingPets(fightingPetMaps);

        fightingCamp.setAi(true);
        fightingCamp.setReady(true);

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
                    //数值系统记录次数
                    cultivateService.numericalAdd(fightingReq.getUser(), "release_nirvana", 1L);
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

    /**
     * 胜利结算
     *
     * @param fightingPet
     */
    public void win(FightingPet fightingPet) {
        win(fightingPet, true);
    }

    public void win(FightingPet fightingPet, boolean isSave) {

        FightingRoom fightingRoom = fightingPet.getFightingCamp().getFightingRoom();
        fightingPet.addStep(FightingStep.VICTORY, "");
        /**
         * 发送信息
         */
        fightingRoom.sendMsgResp(fightingRoom.getFightingCamps().keySet(), sendMsgUtil);

        this.deleteFightingRoom(fightingRoom);

        //胜利结算逻辑
        User user = fightingPet.getFightingCamp().getUser();

        logger.debug(String.format("胜利：%s", user.getUsername()));
        //ai断线不结算
        if (!fightingPet.getUid().startsWith("ai-") && sendMsgUtil.connectionIsAlive(user.getUid())) {

            //获取内存中真实user
            user = userUtil.getUserByUid(user.getUid());
            //战斗总场次
            cultivateService.numericalAdd(user, "fighting_count", 1L);
            //战斗胜利总场次
            cultivateService.numericalAdd(user, "fighting_win_count", 1L);

            LevelDesign levelDesign = fightingRoom.getLevelDesign();
            //PVP天梯胜利结算
            if (levelDesign == null) {

                //天梯总场次
                cultivateService.numericalAdd(user, "ladder_count", 1L);
                //天梯胜利总场次
                cultivateService.numericalAdd(user, "ladder_win_count", 1L);

                Long daily_ladder_win = user.getUserNumericalMap().get("daily_ladder_win").getValue();

                if (daily_ladder_win < 10) {
                    cultivateService.rewards(user, fightingRoom.getFightingType());
                }
                //增加一次天梯胜利记录
                cultivateService.numericalAdd(user, "daily_ladder_win", 1L);
                //连胜加一
                cultivateService.numericalAdd(user, "ladder_streak", 1L);

                //升阶以及传奇积分逻辑
                Long ladder_level = user.getUserNumericalMap().get("ladder_level").getValue();
                //传奇联赛排名key
                String yyMM = DateUtil.date().toString("yyMM");
                String rankKey = RedisKeyUtil.getKey(RedisKey.LADDER_RANKING, yyMM);
                //天梯最高排名
                Long ladder_max_ranking = user.getUserNumericalMap().get("ladder_max_ranking").getValue();
                Long aLong = null;
                if (ladder_level < 15) {
                    long add = 1;
                    //连胜奖励
                    Long ladder_streak = user.getUserNumericalMap().get("ladder_streak").getValue();
                    if (ladder_streak > 2) {
                        add = 2;
                    }
                    cultivateService.numericalAdd(user, "ladder_level_star", add);

                    Long ladder_level_star = user.getUserNumericalMap().get("ladder_level_star").getValue();
                    //升阶，5星
                    if (ladder_level_star >= 5) {
                        //判断最值
                        Long ladder_level_max = user.getUserNumericalMap().get("ladder_level_max").getValue();
                        if (ladder_level + 1 > ladder_level_max) {
                            cultivateService.numericalAdd(user, "ladder_level_max", 1L);
                        }
                        //天梯最高排名(未进排名)
                        if (ladder_max_ranking < 0 && ladder_level + 1 > -ladder_max_ranking) {
                            cultivateService.numericalAdd(user, "ladder_max_ranking", -1L);
                        }


                        cultivateService.numericalAdd(user, "ladder_level", 1L);
                        cultivateService.numericalAdd(user, "ladder_level_star", -5L);
                        //14升15阶进入传奇联赛
                        if (ladder_level == 14) {
                            //传奇联赛排名
                            scoreRankUtil.add(rankKey, user, 100L);
                            cultivateService.numericalAdd(user, "ladder_integral", 100L);
                            //进入传奇联赛次数
                            cultivateService.numericalAdd(user, "ladder_in_count", 1L);
                            aLong = scoreRankUtil.find(rankKey, user);
                        }
                    }

                } else {
                    //传奇联赛，排名增加
                    scoreRankUtil.incrementScore(rankKey, user, 30L);
                    cultivateService.numericalAdd(user, "ladder_integral", 30L);
                    aLong = scoreRankUtil.find(rankKey, user);
                }
                //天梯最高排名(已有排名)
                if (aLong != null && aLong > ladder_max_ranking) {
                    cultivateService.numericalSet(user, "ladder_max_ranking", aLong);
                }


            }
            //PVE闯关胜利结算
            else {
                //闯关模式结算
                int difficult = fightingRoom.getDifficult();
                //添加徽章
                boolean isFirst = userUtil.addBadges(user.getUid(), levelDesign, difficult);
                user = userUtil.getUserByUid(user.getUid());
                String firstAwardType = null;
                String awardType = null;
                switch (difficult) {
                    case 0:
                        awardType = levelDesign.getOrdinaryAward();
                        firstAwardType = levelDesign.getOrdinaryFirstAward();
                        break;
                    case 1:
                        awardType = levelDesign.getDifficultyAward();
                        firstAwardType = levelDesign.getDifficultyFirstAward();
                        break;
                    case 2:
                        awardType = levelDesign.getCrazyAward();
                        firstAwardType = levelDesign.getCrazyFirstAward();
                        break;
                }
                //首次奖励
                if (isFirst) {
                    cultivateService.rewards(user, firstAwardType);
                }
                cultivateService.rewards(user, awardType);

                cultivateService.numericalAdd(user, "daily_pve_win", 1L);
                cultivateService.numericalAdd(user, "pve_count_win", 1L);

            }
        }

        Map<String, FightingCamp> distinguish = fightingPet.getDistinguish();
        FightingCamp enemy = distinguish.get("enemy");
        //失败结算
        this.lost(enemy);

        if (isSave) {
            //结算完毕，保存
            this.hiSave(fightingPet.getFightingCamp());
        }

    }

    /**
     * 失败结算
     *
     * @param enemy
     */
    private void lost(FightingCamp enemy) {

        FightingRoom fightingRoom = enemy.getFightingRoom();
        //ai不结算，断线不结算
        if (!enemy.getUser().getUid().startsWith("ai-") && sendMsgUtil.connectionIsAlive(enemy.getUser().getUid())) {
            LevelDesign levelDesign = fightingRoom.getLevelDesign();
            User user = userUtil.getUserByUid(enemy.getUser().getUid());
            //战斗总场次
            cultivateService.numericalAdd(user, "fighting_count", 1L);
            //天梯总场次
            cultivateService.numericalAdd(user, "ladder_count", 1L);
            //天梯失败结算
            if (levelDesign == null) {
                //传奇联赛排名key
                String yyMM = DateUtil.date().toString("yyMM");
                String rankKey = RedisKeyUtil.getKey(RedisKey.LADDER_RANKING, yyMM);

                Long ladder_level = user.getUserNumericalMap().get("ladder_level").getValue();
                if (ladder_level < 15) {
                    Long ladder_level_star = user.getUserNumericalMap().get("ladder_level_star").getValue();
                    if (ladder_level_star == 0) {
                        if (ladder_level > 0) {
                            cultivateService.numericalAdd(user, "ladder_level", -1L);
                            cultivateService.numericalAdd(user, "ladder_level_star", 4L);
                        }
                    } else {
                        cultivateService.numericalAdd(user, "ladder_level_star", -1L);
                    }

                    //清空连胜
                    Long ladder_streak = user.getUserNumericalMap().get("ladder_streak").getValue();
                    cultivateService.numericalAdd(user, "ladder_streak", -ladder_streak);
                } else {
                    scoreRankUtil.incrementScore(rankKey, user, -20L);
                    cultivateService.numericalAdd(user, "ladder_integral", -20L);
                }
            }
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

        hiFightingRoom.setFightingRoomJson(redisObjectUtil.serialize(fightingRoom));

        hiFightingRoomMapper.insertSelective(hiFightingRoom);

    }


}
