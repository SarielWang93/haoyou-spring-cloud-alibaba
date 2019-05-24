package com.haoyou.spring.cloud.alibaba.fighting.Info;


import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 棋盘对象
 */
@Data
@JsonIgnoreProperties({"ATTACK_NORMAL", "ATTACK_SPECIAL", "SHIELD", "SKILL"})
public class FightingBoard implements Serializable {
    private static final long serialVersionUID = 4716274178014184967L;

    public final static int ATTACK_NORMAL = 1;//普通攻击块
    public final static int ATTACK_SPECIAL = 2;//特殊攻击块
    public final static int SHIELD = 3;//护盾块
    public final static int SKILL = 4;//技能块

    private static WeightRandom.WeightObj<Integer>[] weightObjs = new WeightRandom.WeightObj[4];

    static {

        weightObjs[0] = new WeightRandom.WeightObj(ATTACK_NORMAL, 100.0);
        weightObjs[1] = new WeightRandom.WeightObj(ATTACK_SPECIAL, 100.0);
        weightObjs[2] = new WeightRandom.WeightObj(SHIELD, 100.0);
        weightObjs[3] = new WeightRandom.WeightObj(SKILL, 100.0);

    }


    private String leftPlayerUid;
    private String rightPlayerUid;

    private int[][] board;

    public FightingBoard() {
    }

    public FightingBoard(Map<String, FightingCamp> fightingCamps) {
        for (Map.Entry<String, FightingCamp> entry : fightingCamps.entrySet()) {
            if (this.leftPlayerUid == null) {
                this.leftPlayerUid = entry.getKey();
                entry.getValue().setLeft(true);
            } else {
                this.rightPlayerUid = entry.getKey();
            }
        }
        this.board = new int[7][6];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 6; j++) {
                int block = randomBlock();
                this.board[i][j] = block;
            }
        }
    }

    /**
     * 根据操作刷新棋盘
     *
     * @param fightingReq
     */
    public void refrashBoard(FightingReq fightingReq) {
        // 执行操作，刷新棋盘

        List<BlockInfo> blockInfos = fightingReq.getDestroyInfos();
//        Console.log(blockInfos);
        if (blockInfos != null) {
            /**
             * 消除块，置为-1
             */
            for (BlockInfo blockInfo : blockInfos) {
                int x = blockInfo.getX();
                int y = blockInfo.getY();

                this.board[x][y] = -1;
            }
            /**
             * 下落并获得上端缺口
             */
            List<BlockInfo> newBlockInfos = new ArrayList<>();
            for (BlockInfo blockInfo : blockInfos) {
                int x = blockInfo.getX();
                int y = blockInfo.getY();

                for (int i = y + 1, j = y;; i++) {
                    if (i < 6) {
                        if (this.board[x][i] > 0) {
                            int e = this.board[x][i];
                            this.board[x][i] = this.board[x][j];
                            this.board[x][j] = e;
                            j = i;
                        }
                    } else {
                        /**
                         * 落到后获得上端缺口位置，保存
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
            for(BlockInfo blockInfo:newBlockInfos){
                int x = blockInfo.getX();
                int y = blockInfo.getY();
                int newBlock=randomBlock();
                this.board[x][y] = newBlock;
                blockInfo.setRandomID(newBlock);

            }
//            Console.log(newBlockInfos);

            fightingReq.setNewInfos(newBlockInfos);



//            for (int i = 5; i >=0 ; i--) {
//                for (int j = 0; j < 7; j++) {
//                    Console.print(this.board[j][i]+"  ");
//                }
//                Console.log();
//            }


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


}
