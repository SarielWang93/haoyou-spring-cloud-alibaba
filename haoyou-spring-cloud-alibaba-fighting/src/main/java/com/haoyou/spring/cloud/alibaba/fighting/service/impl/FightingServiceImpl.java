package com.haoyou.spring.cloud.alibaba.fighting.service.impl;


import cn.hutool.core.util.RandomUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.haoyou.spring.cloud.alibaba.commons.domain.*;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.Pet;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.Info.*;
import com.haoyou.spring.cloud.alibaba.fighting.fightingstate.FightingState;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.service.fighting.FightingService;
import com.haoyou.spring.cloud.alibaba.action.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.action.SendMsgUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


import java.util.*;

@Service(version = "${fighting.service.version}")
public class FightingServiceImpl implements FightingService {
    private final static Logger logger = LoggerFactory.getLogger(FightingServiceImpl.class);

    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private SendMsgUtil sendMsgUtil;

    @Value("${fighting.alivetime: 3600}")
    private long alivetime;


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
        for (User user : users) {
            FightingCamp fightingCamp = this.initFightingCamp(fightingRoom, user);

            fightingCamps.put(user.getUid(), fightingCamp);
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


                Integer action_time = fightingPet.getAction_time();
                for (int i = action_time; ; i++) {
                    if (fightingPets.get(i) == null) {
                        fightingPets.put(i, fightingPet);
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

        /**
         * 行动宠物回合开始
         */
        Map.Entry<Integer, FightingPet> first = fightingPets.firstEntry();
        this.startRound(first.getValue());

        /**
         * 序列化存储到redis
         */
        this.saveFightingRoom(fightingRoom);

        /**
         * 发送信息
         */
        List<String> userUids = new ArrayList<>();
        for (User user : users) {
            userUids.add(user.getUid());
        }


        fightingRoom.sendMsgInit(userUids,sendMsgUtil);


        fightingRoom = null;

        return true;
    }


    /**
     * 行动宠物回合开始触发的 状态
     *
     * @param fightingPet 当前回合行动人
     */
    public void startRound(FightingPet fightingPet) {

        //标注当前行动宠物
        FightingRoom fightingRoom = fightingPet.getFightingCamp().getFightingRoom();
        fightingRoom.startRount(fightingPet);

    }


    /**
     * 接受处理信息
     *
     * @param req
     * @return
     */
    @Override
    public MapBody receiveFightingMsg(MyRequest req) {
        FightingReq fightingReq=sendMsgUtil.deserialize(req.getMsg(),FightingReq.class);
        MapBody baseMessage = new MapBody();
        //计算
        if(!fightingReq.check()){
            baseMessage.setState(ResponseMsg.MSG_ERR);
            return baseMessage;
        }

        logger.info(String.format("接受到战斗数据：%s", fightingReq));

        /**
         * 获取对战房间对象
         */
        FightingRoom fightingRoom = this.getFightingRoomByUid(fightingReq.getFightingRoomUid());


        /**
         * 当前执行宠物与接受信息对比，如果接受信息有误，则返回错误
         */

        FightingCamp own = fightingRoom.getFightingCamps().get(fightingRoom.getCampNow());

        FightingPet fightingPet = own.getFightingPets().get(fightingRoom.getPetNow());

        if (!fightingRoom.getCampNow().equals(req.getUser().getUid()) || fightingRoom.getPetNow() != fightingReq.getCurrentPetId() + 1) {

            fightingRoom.sendMsgResp(req.getUser().getUid(),sendMsgUtil);

            baseMessage.setState(ResponseMsg.MSG_ERR);
            return baseMessage;
        }

        FightingCamp enemy = fightingPet.getDistinguish().get("enemy");
        /**
         * 棋盘刷新
         */
        fightingRoom.getFightingBoard().refrashBoard(fightingReq);

        /**
         * 发送刷新后的棋盘信息
         */
        sendMsgUtil.sendMsgList(fightingRoom.getFightingCamps().keySet(), SendType.FIGHTING_REFRESHBOARD,fightingReq);

        //出手之前记录行动权
        Integer action_time = fightingPet.getAction_time();

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
         * 胜利判定
         */
        boolean win = true;
        for (FightingPet enemyPet : enemy.getFightingPets().values()) {
            if (enemyPet.getHp() > 0) {
                win = false;
            }
        }
        /**
         * 胜利执行
         */
        if (win) {
            win(fightingPet);
            baseMessage.setState(ResponseMsg.MSG_SUCCESS);
            return baseMessage;
        }


        /**
         * 非当前宠物 行动权计算（红黑树TreeMap）
         */
        TreeMap<Integer, FightingPet> fightingPets = new TreeMap<>();


        for (FightingCamp value : fightingPet.getDistinguish().values()) {
            for (FightingPet value1 : value.getFightingPets().values()) {
                if (!value1.getUid().equals(fightingPet.getUid()) && value1.getHp() > 0) {

                    Integer action_time1 = value1.getAction_time();
                    value1.setAction_time(action_time1 - action_time);

                    fightingPets.put(value1.getAction_time(), value1);
                }
                //清理临时状态
                value1.removeState(StateType.TEMPORARY);
            }
        }


        fightingPets.put(fightingPet.getAction_time(), fightingPet);

        /**
         * 添加出手信息
         */
        fightingPet.addShot(fightingReq);
        /**
         * 以上为上一回合结束，以下为下一回合开始
         */


        /**
         * 行动宠物回合开始
         */
        Map.Entry<Integer, FightingPet> first = fightingPets.firstEntry();
        this.startRound(first.getValue());


        /**
         * 刷新redis对战房间对象
         */
        this.saveFightingRoom(fightingRoom);

        /**
         * 发送信息
         */

        fightingRoom.sendMsgResp(fightingRoom.getFightingCamps().keySet(),sendMsgUtil);


        baseMessage.setState(ResponseMsg.MSG_SUCCESS);
        return baseMessage;
    }

    /**
     * 从redis中获取对战房间对象
     *
     * @param uid
     * @return
     */

    public FightingRoom getFightingRoomByUid(String uid) {
        FightingRoom fightingRoom = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.FIGHTING_ROOM, uid), FightingRoom.class);
        fightingRoom.setNowSteps(new TreeMap<>());

        for (FightingCamp fightingCamp : fightingRoom.getFightingCamps().values()) {
            fightingCamp.setFightingRoom(fightingRoom);

            for (FightingPet fightingPet : fightingCamp.getFightingPets().values()) {
                fightingPet.setFightingCamp(fightingCamp);
                fightingPet.setDistinguish(this.distinguish(fightingPet));
            }

        }
        return fightingRoom;
    }

    /**
     * redis存储
     */

    public boolean saveFightingRoom(FightingRoom fightingRoom) {
        return redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.FIGHTING_ROOM, fightingRoom.getUid()), fightingRoom, this.alivetime);
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
        TreeMap<Integer, FightingPet> fightingPets = new TreeMap<>();
        String useruidkey = RedisKeyUtil.getKey(RedisKey.PETS, user.getUid());

        String key = RedisKeyUtil.getlkKey(useruidkey);

        HashMap<String, Pet> allPets = redisObjectUtil.getlkMap(key, Pet.class);

        for (Pet pet : allPets.values()) {
            if (pet.getIswork() != null && pet.getIswork() != 0) {
                FightingPet fightingPet = FightingPet.getFightingPet(pet,redisObjectUtil);
                fightingPet.setFightingCamp(fightingCamp);
                fightingPet.initFighting();

                fightingPets.put(pet.getIswork(), fightingPet);
            }
        }
        fightingCamp.setFightingPets(fightingPets);
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
        final int MAX_ENERGY = 20;

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
        Integer punishValue = 5 * blockCount;


        /**
         * 根据块的种类执行相应的操作
         */
        switch (blockType) {
            // 普通攻击
            case FightingBoard.ATTACK_NORMAL:
                //必杀技
                if (energy == MAX_ENERGY) {
                    fightingPet.skillsDo(SkillType.UNIQUE, blockCount);
                    energy = -1;
                    punishValue += PunishValue.UNIQUE;
                } else {
                    /**
                     * 记录步骤
                     */
                    fightingPet.addStep(FightingStep.DO_ATTACK,"");

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
        }

        //是否跳过
        if (punishValue == 0) {
            //跳过的惩罚值
            punishValue = PunishValue.SKIP;
        } else {
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
        //TODO 胜利结算逻辑



        //删除战斗信息缓存的
        FightingRoom fightingRoom = fightingPet.getFightingCamp().getFightingRoom();
        redisObjectUtil.delete(RedisKeyUtil.getKey(RedisKey.FIGHTING_ROOM, fightingRoom.getUid()));
    }


    /**
     * 初始化FightingPet
     * @param pet
     * @return
     */
    @Override
    public boolean newFightingPet(Pet pet) {
        logger.info(String.format("newFightingPet: %s ",pet));
        new FightingPet(pet,redisObjectUtil).save();
        return true;

    }

    /**
     * 删除FightingPet
     * @param pet
     * @return
     */
    @Override
    public boolean deleteFightingPet(Pet pet) {
        logger.info(String.format("newFightingPet: %s ",pet));
        FightingPet.getFightingPet(pet,redisObjectUtil).delete();
        return true;

    }

    /**
     * 获取FightingPet信息
     * @param pet
     * @return
     */
    @Override
    public MapBody getFightingPet(Pet pet) {
        logger.info(String.format("newFightingPet: %s ",pet));
        return FightingPet.getFightingPet(pet,redisObjectUtil).toMsg();

    }



}