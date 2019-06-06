package com.haoyou.spring.cloud.alibaba.fighting.info;


import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.domain.StateType;
import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 棋盘对象
 */
@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class FightingBoard implements Serializable {
    private static final long serialVersionUID = 4716274178014184967L;
    private final static Logger logger = LoggerFactory.getLogger(FightingBoard.class);

    public final static int ATTACK_NORMAL = 1;//普通攻击块
    public final static int ATTACK_SPECIAL = 2;//特殊攻击块
    public final static int SHIELD = 3;//护盾块
    public final static int SKILL = 4;//技能块

    //步骤操作块
    public final static int FROZEN = 1;//冰冻
    public final static int UPGRADE = 2;//升级
    public final static int THAW = 3;//解冻


    private static WeightRandom.WeightObj<Integer>[] weightObjs = new WeightRandom.WeightObj[4];

    static {

        weightObjs[0] = new WeightRandom.WeightObj(ATTACK_NORMAL, 30.0);
        weightObjs[1] = new WeightRandom.WeightObj(ATTACK_SPECIAL, 20.0);
        weightObjs[2] = new WeightRandom.WeightObj(SHIELD, 30.0);
        weightObjs[3] = new WeightRandom.WeightObj(SKILL, 20.0);

    }


    private String leftPlayerUid;
    private String rightPlayerUid;

    private BlockInfo[][] board;

    public FightingBoard() {
    }


    public FightingBoard(Map<String, FightingCamp> fightingCamps) {
        int i1 = RandomUtil.randomInt(2);

        int i2 = 0;
        for (Map.Entry<String, FightingCamp> entry : fightingCamps.entrySet()) {
            if (i1 == i2++) {
                this.leftPlayerUid = entry.getKey();
                entry.getValue().setLeft(true);
            } else {
                this.rightPlayerUid = entry.getKey();
            }
        }
        this.board = new BlockInfo[7][6];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 6; j++) {
                int block = randomBlock();
                this.board[i][j] = new BlockInfo(i, j, block);
            }
        }
    }

    /**
     * 转成List
     *
     * @return
     */
    public List<BlockInfo> toListBoard() {
        List<BlockInfo> listBoard = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 6; j++) {
                listBoard.add(board[i][j]);
            }
        }
        return listBoard;
    }

    /**
     * 检查消除的块
     *
     * @param fightingPet
     * @param fightingReq
     * @return
     */
    public boolean check(FightingPet fightingPet, FightingReq fightingReq) {
        /**
         * 校验块，并且补充块的种类和数量
         */
        List<BlockInfo> destroyInfos = fightingReq.getDestroyInfos();
        if (destroyInfos != null && destroyInfos.size() > 0) {


            //星根据级校验消除块的个数
            Integer starClass = fightingPet.getPet().getStarClass();
            int maxBlockCount = 3;
            if (starClass > 2) {
                maxBlockCount = starClass + 1;
            }
            if (destroyInfos.size() > maxBlockCount) {
                return false;
            }


            int blockType = destroyInfos.get(0).getRandomID();
            //是否同一类型，以及是否与当前棋盘一致,是否冰冻
            for (BlockInfo blockInfo : destroyInfos) {
                if (blockInfo.getDisType() != 0 || blockInfo.getRandomID() != blockType || blockInfo.getRandomID() != this.board[blockInfo.getX()][blockInfo.getY()].getRandomID()) {
                    return false;
                }
            }


            //提取块的种类和数量
            fightingReq.setBlockType(blockType);
            fightingReq.setBlockCount(destroyInfos.size());

        }
        return true;
    }

    /**
     * 根据操作刷新棋盘
     *
     * @param fightingReq
     */
    public boolean refrashBoard(FightingPet fightingPet, FightingReq fightingReq) {
        // 执行操作，刷新棋盘

        List<BlockInfo> blockInfos = fightingReq.getDestroyInfos();
//        Console.log(blockInfos);
        /**
         * 三步操作，必须独立运行才能不互相影响
         */
        if (blockInfos != null && !blockInfos.isEmpty()) {
            /**
             * 消除块，置为-1
             */
            //记录消除的冰冻
            List<BlockInfo> iceBlockInfos = new ArrayList<>();
            for (BlockInfo blockInfo : blockInfos) {
                int x = blockInfo.getX();
                int y = blockInfo.getY();
                blockInfo = this.board[x][y];

                if (blockInfo.getUpType() != 0) {
                    fightingReq.setBlockCount(fightingReq.getBlockCount() + 1);
                }
                this.board[x][y] = null;
                //检查周围有没有冰冻
                List<BlockInfo> blockInfos1 = this.getBlockInfos(blockInfo,false);
                for (BlockInfo blockInfo1 : blockInfos1) {
                    if (blockInfo1 != null && blockInfo1.getDisType() > 0) {
                        blockInfo1.setDisType(0);
                        iceBlockInfos.add(blockInfo1);
                    }
                }

            }

            /**
             * 下落并获得上端缺口
             */
            //上端缺口处块对象
            List<BlockInfo> newBlockInfos = new ArrayList<>();
            for (BlockInfo blockInfo : blockInfos) {
                int x = blockInfo.getX();
                int y = blockInfo.getY();

                for (int i = y + 1, j = y; ; i++) {
                    if (i < 6) {
                        if (this.board[x][i] != null) {
                            BlockInfo e = this.board[x][i];
                            this.board[x][i] = this.board[x][j];
                            this.board[x][j] = e;
                            e.setY(j);
                            j = i;
                        }
                    } else {
                        /**
                         * 落到最后获得上端缺口位置，保存
                         */
                        BlockInfo newBlockInfo = new BlockInfo(x, j, -1);
                        newBlockInfos.add(newBlockInfo);
                        break;
                    }
                }
            }
            /**
             * 对最上端缺口新生成块
             */
            for (BlockInfo blockInfo : newBlockInfos) {
                blockInfo.setRandomID(randomBlock());
                this.board[blockInfo.getX()][blockInfo.getY()] = blockInfo;
//                if(RandomUtil.randomInt(2)==0){
//                    blockInfo.setUpType(1);
//                    fightingPet.addStep(FightingStep.BLOCK_CHANGE, blockInfo.toString());
//                }
            }


            for (BlockInfo blockInfo : iceBlockInfos) {
                //记录块解冻的步骤
                fightingPet.addStep(FightingStep.BLOCK_CHANGE, blockInfo.toString());
            }

            fightingReq.setNewInfos(newBlockInfos);
            return true;

        } else {
            return false;
        }
    }

    /**
     * 权重获取随机数
     *
     * @return
     */
    private int randomBlock() {



        WeightRandom<Integer> weightRandom = RandomUtil.weightRandom(weightObjs);

        return weightRandom.next();
    }


    /**
     * 修改块的值
     *
     * @return
     */
    public void changeBlock(int type, int count, int value, FightingPet fightingPet) {
        String userUid = fightingPet.getFightingCamp().getUser().getUid();
        //根据value的位数判断冰冻还是升级

        List<BlockInfo> blockInfos = null;
        //根据type获取不同的块
        if (type != 0) {
            blockInfos = this.ownType(type, userUid);
            if (blockInfos.size() < count) {
                count = blockInfos.size();
            }
        } else {
            blockInfos = this.ownAll(userUid);
        }


        for (int i = 0; ; ) {
            int i1 = RandomUtil.randomInt(blockInfos.size());
            BlockInfo blockInfo = blockInfos.get(i1);

            switch (value) {
                case FROZEN:
                    blockInfo.setDisType(1);
                    //记录块修改的步骤
                    fightingPet.addStep(FightingStep.BLOCK_CHANGE, blockInfo.toString());
                    i++;
                    break;
                case UPGRADE:
                    blockInfo.setUpType(1);
                    //记录块修改的步骤
                    fightingPet.addStep(FightingStep.BLOCK_CHANGE, blockInfo.toString());
                    i++;
                    break;
            }
            if (value > 3) {
                //记录块修改的步骤
                fightingPet.addStep(FightingStep.BLOCK_CHANGE, blockInfo.toString());
                value -= 3;
                blockInfo.setRandomID(value);
                i++;
            }


            blockInfos.remove(blockInfo);
            if (i >= count || blockInfos.size() == 0) {
                break;
            }
        }
    }

    /**
     * 获取己方一侧的块
     *
     * @return
     */
    private List<BlockInfo> ownAll(String userUid) {
        List<BlockInfo> all = new ArrayList<>();
        int i = 3;
        if (rightPlayerUid.equals(userUid)) {
            i = 7;
        }
        for (int x = i - 3; x < i; x++) {
            for (int y = 0; y < 6; y++) {
                all.add(this.board[x][y]);
            }
        }
        return all;
    }

    /**
     * 获取己方一侧某种快的个数
     *
     * @param userUid
     * @return
     */
    private List<BlockInfo> ownType(int type, String userUid) {
        List<BlockInfo> types = new ArrayList<>();
        int i = 3;
        if (rightPlayerUid.equals(userUid)) {
            i = 7;
        }
        for (int x = i - 3; x < i; x++) {
            for (int y = 0; y < 6; y++) {
                if (this.board[x][y].getRandomID() == type) {
                    types.add(this.board[x][y]);
                }
            }
        }
        return types;
    }


    /**
     * AI逻辑产生消除快
     *
     * @param fightingPet
     * @param userUid       下面是对应块的权重
     * @param attack
     * @param specialAttack
     * @param shield
     * @param skill
     * @return
     */
    public List<BlockInfo> doAI(FightingPet fightingPet, String userUid, Integer attack, Integer specialAttack, Integer shield, Integer skill) {

        Integer starClass = fightingPet.getPet().getStarClass();
        /**
         * 处理禁止块状态
         */
        List<FightingState> fightingStates = fightingPet.getFightingStateByType(StateType.TURN_START_BLOCK);
        for(FightingState fightingState:fightingStates){
            switch (fightingState.getFixed()){
                case ATTACK_NORMAL:
                    attack=0;
                    specialAttack=0;
                    break;
                case SHIELD:
                    shield=0;
                    break;
                case SKILL:
                    skill=0;
                    break;
            }

        }

        //权重随机
        WeightRandom.WeightObj<Integer>[] weightObjs = new WeightRandom.WeightObj[4];
        weightObjs[0] = new WeightRandom.WeightObj(ATTACK_NORMAL, attack);
        weightObjs[1] = new WeightRandom.WeightObj(ATTACK_SPECIAL, specialAttack);
        weightObjs[2] = new WeightRandom.WeightObj(SHIELD, shield);
        weightObjs[3] = new WeightRandom.WeightObj(SKILL, skill);
        WeightRandom<Integer> weightRandom = RandomUtil.weightRandom(weightObjs);
        Integer redomType = weightRandom.next();


        List<BlockInfo> blockInfo = new LinkedList<>();
        //随机选取自己可选区域内的块
        int minBlockCount = 2;
        if(redomType!=null){
            List<BlockInfo> blockInfos = ownType(redomType, userUid);
            if (blockInfos.size() > 0) {
                int i = RandomUtil.randomInt(blockInfos.size());
                BlockInfo first = blockInfos.get(i);
                if (first.getDisType() == 0) {

                    blockInfo.add(first);

                    //根据星级控制最大长度截取
                    int maxBlockCount = 3;
                    if (starClass > 2) {
                        maxBlockCount = starClass + 1;
                    }
                    //连线
                    this.link(first, blockInfo, maxBlockCount);
                }

            }
            //如果连线数小于三则重新获取连线
            if (blockInfo.size() < minBlockCount) {
                switch (redomType) {
                    case ATTACK_NORMAL:
                        attack--;
                        break;
                    case ATTACK_SPECIAL:
                        specialAttack--;
                        break;
                    case SHIELD:
                        shield--;
                        break;
                    case SKILL:
                        skill--;
                        break;
                }
                blockInfo = this.doAI(fightingPet, userUid, attack, specialAttack, shield, skill);
            }
        }

        return blockInfo;
    }

    /**
     * 连线
     *
     * @param father
     * @param blockInfo
     * @param maxBlockCount
     */
    private void link(BlockInfo father, List<BlockInfo> blockInfo, int maxBlockCount) {


        List<BlockInfo> all = this.getBlockInfos(father,true);


        for (BlockInfo son : all) {
            if (son.getDisType() == 0 && son.getRandomID() == father.getRandomID() && !blockInfo.contains(son) && son.getDisType() == 0) {
                blockInfo.add(son);
                if (blockInfo.size() >= maxBlockCount) {
                    return;
                }

                link(son, blockInfo, maxBlockCount);
                return;
            }
        }


    }

    /**
     * 获取周围块
     * @param blockInfo
     * @param xm 是否斜线
     * @return
     */
    private List<BlockInfo> getBlockInfos(BlockInfo blockInfo,boolean xm) {

        int x = blockInfo.getX();
        int y = blockInfo.getY();
        List<BlockInfo> all = new ArrayList<>();

        if (x - 1 >= 0) {
            all.add(this.board[x - 1][y]);
            if(xm){
                if (y - 1 >= 0)
                    all.add(this.board[x - 1][y - 1]);
                if (y + 1 < 6)
                    all.add(this.board[x - 1][y + 1]);
            }
        }
        if (x + 1 < 7) {
            all.add(this.board[x + 1][y]);
            if(xm){
                if (y - 1 >= 0)
                    all.add(this.board[x + 1][y - 1]);
                if (y + 1 < 6)
                    all.add(this.board[x + 1][y + 1]);
            }
        }

        if (y - 1 >= 0)
            all.add(this.board[x][y - 1]);
        if (y + 1 < 6)
            all.add(this.board[x][y + 1]);

        return all;
    }


}
