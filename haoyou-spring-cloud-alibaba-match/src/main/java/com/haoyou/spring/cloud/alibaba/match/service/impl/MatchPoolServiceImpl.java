package com.haoyou.spring.cloud.alibaba.match.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.match.info.MatchPoolPlayerInfo;
import com.haoyou.spring.cloud.alibaba.match.info.PlayerRoomInfo;
import com.haoyou.spring.cloud.alibaba.match.service.MatchPoolService;
import com.haoyou.spring.cloud.alibaba.service.fighting.FightingService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 玩家匹配系统（基于redis）
 */
@Service
public class MatchPoolServiceImpl implements MatchPoolService {
    private final static Logger logger = LoggerFactory.getLogger(MatchPoolServiceImpl.class);

    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private SendMsgUtil sendMsgUtil;
    @Reference(version = "${fighting.service.version}")
    private FightingService fightingService;

    /**
     * 等待接受匹配时长
     */
    @Value("${matchpool.accept-max-time: 20}")
    private long acceptMastTime;


    /**
     * 最大匹配时间，过了时间直接移除
     */
    @Value("${matchpool.save-max-time: 3600}")
    public final long MAX_TIME = 60 * 60;

    /**
     * 每个玩家需要匹配到的玩家数量
     */
    private int NEED_MATCH_PLAYER_COUNT = 1;


    /**
     * 匹配方法
     *
     * @throws Exception
     */
    @Override
    @Scheduled(cron = "${matchpool.delay: 0/2 * * * * ?}")
    public void doMatch(){
        try {
            //从redis中获取匹配池
            HashMap<String, MatchPoolPlayerInfo> playerPool = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.MATCH_PLAYER_POOL), MatchPoolPlayerInfo.class);
            if (playerPool != null && playerPool.size() > 0) {
                matchProcess(playerPool);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 把玩家放入匹配池
     *
     * @param player
     * @return
     */
    @Override
    public boolean putPlayerIntoMatchPool(User player) {
        MatchPoolPlayerInfo playerInfo = new MatchPoolPlayerInfo(player);
        return playerInfo.save(RedisKey.MATCH_PLAYER_POOL, redisObjectUtil, this);
    }

    /**
     * 从匹配池查找玩家
     *
     * @param player
     * @return
     */
    @Override
    public MatchPoolPlayerInfo findPlayerFromMatchPool(User player) {
        if (player == null) {
            return null;
        }
        MatchPoolPlayerInfo playerInfo = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.MATCH_PLAYER_POOL, player.getUid()), MatchPoolPlayerInfo.class);

        return playerInfo;
    }

    /**
     * 把玩家从匹配池移除
     *
     * @param player
     */
    @Override
    public boolean removePlayerFromMatchPool(User player) {
        return redisObjectUtil.delete(RedisKeyUtil.getKey(RedisKey.MATCH_PLAYER_POOL, player.getUid()));
    }

    /**
     * 匹配操作
     *
     * @param playerPool
     */
    private void matchProcess(HashMap<String, MatchPoolPlayerInfo> playerPool) {
//        long startTime = System.currentTimeMillis();
//        logger.debug("执行匹配开始|开始时间|" + startTime);
        try {
            //先把匹配池中的玩家按分数分布
            TreeMap<Integer, HashSet<MatchPoolPlayerInfo>> pointMap = new TreeMap<Integer, HashSet<MatchPoolPlayerInfo>>();
            for (MatchPoolPlayerInfo matchPlayer : playerPool.values()) {
                /**
                 * 此处由radis的存储时间控制，超过时间直接删除，此处不存在超时的
                 * MAX_TIME
                 */
//                //在匹配池中是时间太长，直接移除
//                if((System.currentTimeMillis()-matchPlayer.getStartMatchTime())>MAX_TIME){
//                    logger.warn(matchPlayer.getPlayerId()+"在匹配池中是时间超过delay秒，直接移除");
//                    removePlayerFromMatchPool(matchPlayer.getPlayer());
//                    continue;
//                }
                HashSet<MatchPoolPlayerInfo> set = pointMap.get(matchPlayer.getRank());
                if (set == null) {
                    set = new HashSet<MatchPoolPlayerInfo>();
                    set.add(matchPlayer);
                    pointMap.put(matchPlayer.getRank(), set);
                } else {
                    set.add(matchPlayer);
                }
            }

            for (HashSet<MatchPoolPlayerInfo> sameRankPlayers : pointMap.values()) {
                boolean continueMatch = true;
                while (continueMatch) {
                    //找出同一分数段里，等待时间最长的玩家，用他来匹配，因为他的区间最大
                    //如果他都不能匹配到，等待时间比他短的玩家更匹配不到
                    MatchPoolPlayerInfo oldest = null;
                    for (MatchPoolPlayerInfo playerMatchPoolInfo : sameRankPlayers) {
                        if (oldest == null) {
                            oldest = playerMatchPoolInfo;
                        } else if (playerMatchPoolInfo.getStartMatchTime() < oldest.getStartMatchTime()) {
                            oldest = playerMatchPoolInfo;
                        }
                    }
                    if (oldest == null) {
                        break;
                    }
                    logger.debug(oldest.getPlayerId() + "|为该分数上等待最久时间的玩家开始匹配|rank|" + oldest.getRank());

                    long now = System.currentTimeMillis();
                    int waitSecond = (int) ((now - oldest.getStartMatchTime()) / 1000);

                    logger.debug(oldest.getPlayerId() + "|当前时间已经等待的时间|waitSecond|" + waitSecond + "|当前系统时间|" + now + "|开始匹配时间|" + oldest.getStartMatchTime());

                    //按等待时间扩大匹配范围
                    float c2 = 1.5f;
                    int c3 = 5;
                    int c4 = 100;

                    float u = (float) Math.pow(waitSecond, c2);
                    u = u + c3;
                    u = (float) Math.round(u);
                    u = Math.min(u, c4);

                    int min = (oldest.getRank() - (int) u) < 0 ? 0 : (oldest.getRank() - (int) u);
                    int max = oldest.getRank() + (int) u;

                    logger.debug(oldest.getPlayerId() + "|本次搜索rank范围下限|" + min + "|rank范围上限|" + max);

                    int middle = oldest.getRank();

                    List<MatchPoolPlayerInfo> matchPoolPlayer = new ArrayList<>();
                    //从中位数向两边扩大范围搜索
                    for (int searchRankUp = middle, searchRankDown = middle; searchRankUp <= max || searchRankDown >= min; searchRankUp++, searchRankDown--) {
                        HashSet<MatchPoolPlayerInfo> thisRankPlayers = pointMap.getOrDefault(searchRankUp, new HashSet<>());
                        if (searchRankDown != searchRankUp && searchRankDown > 0) {
                            thisRankPlayers.addAll(pointMap.getOrDefault(searchRankDown, new HashSet<>()));
                        }
                        if (!thisRankPlayers.isEmpty()) {
                            if (matchPoolPlayer.size() < NEED_MATCH_PLAYER_COUNT) {
                                Iterator<MatchPoolPlayerInfo> it = thisRankPlayers.iterator();
                                while (it.hasNext()) {
                                    MatchPoolPlayerInfo player = it.next();
                                    if (player.getPlayerId() != oldest.getPlayerId()) {
                                        //排除玩家本身
                                        if (matchPoolPlayer.size() < NEED_MATCH_PLAYER_COUNT) {
                                            //判断redis匹配池中是否还存在
                                            if (isExist(player)) {
                                                matchPoolPlayer.add(player);
                                                logger.debug(oldest.getPlayerId() + "|匹配到玩家|" + player.getPlayerId() + "|rank|" + player.getRank());
                                            }
                                            //移除
                                            it.remove();
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            } else {
                                break;
                            }
                        }
                    }

                    if (matchPoolPlayer.size() == NEED_MATCH_PLAYER_COUNT) {
                        logger.debug(oldest.getPlayerId() + "|匹配到玩家数量够了|提交匹配成功处理");
                        //自己也匹配池移除
                        sameRankPlayers.remove(oldest);
                        //判断redis匹配池中是否还存在
                        if (isExist(oldest)) {
                            //匹配成功处理
                            matchPoolPlayer.add(oldest);
                            //把配对的人提交匹配成功处理
                            matchSuccessProcess(matchPoolPlayer);
                            //跳过归还
                            continue;
                        }
                    } else {
                        //本分数段等待时间最长的玩家都匹配不到，其他更不用尝试了
                        continueMatch = false;
                        logger.debug(oldest.getPlayerId() + "|匹配到玩家数量不够，取消本次匹配");
                    }
                    //归还取出来的玩家
                    for (MatchPoolPlayerInfo player : matchPoolPlayer) {
                        HashSet<MatchPoolPlayerInfo> sameRankPlayer = pointMap.get(player.getRank());
                        sameRankPlayer.add(player);
                        //取出时 isExist 方法从redis中移除了匹配对象，此处归还
                        player.save(RedisKey.MATCH_PLAYER_POOL, redisObjectUtil, this);
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("service|error", t);
        }
//        long endTime = System.currentTimeMillis();
//        logger.debug("执行匹配结束|结束时间|" + endTime + "|耗时|" + (endTime - startTime) + "ms");
    }

    /**
     * 查询是否存在（防止负载均衡时多线程问题）
     *
     * @param playerInfo
     * @return
     */
    private boolean isExist(MatchPoolPlayerInfo playerInfo) {
        MatchPoolPlayerInfo playerFromMatchPool = findPlayerFromMatchPool(playerInfo.getPlayer());
        if (playerFromMatchPool == null) {
            return false;
        } else {
            //从匹配池中移除玩家
            removePlayerFromMatchPool(playerInfo.getPlayer());
            return true;
        }
    }

    /**
     * 处理配对成功的玩家
     *
     * @param matchPoolPlayer
     */
    private void matchSuccessProcess(List<MatchPoolPlayerInfo> matchPoolPlayer) {
        //创建房间，并存入redis
        PlayerRoomInfo playerRoomInfo = new PlayerRoomInfo(matchPoolPlayer);
        logger.info(String.format("配对成功： %s", playerRoomInfo.getUid()));

        List<String> userUids = new ArrayList<>();
        for (MatchPoolPlayerInfo playerInfo : matchPoolPlayer) {
            userUids.add(playerInfo.getPlayerId());
        }

        playerRoomInfo.save(redisObjectUtil, this);

        //给玩家发送房间信息

        sendMsgUtil.sendMsgList(userUids, SendType.MATCH_READY, playerRoomInfo);


        final String playerRoomUid = playerRoomInfo.getUid();
        playerRoomInfo = null;


        /**
         * 接受，拒绝检查线程（此处启动一个线程，个人认为是最优解）
         */
        ThreadUtil.excAsync(() -> {
            //启动线程检查是否全部接受，或者有人拒绝
            PlayerRoomInfo playerRoomInfoT = null;
            A:
            for (int i = 0; ; i++) {
                playerRoomInfoT = new PlayerRoomInfo(playerRoomUid, redisObjectUtil);
                if (playerRoomInfoT.getMatchPoolPlayer().size() == 0) {
                    break;
                }

                List<String> userUidsT = new ArrayList<>();
                List<String> userDDs = new ArrayList<>();
                List<User> users = new ArrayList<>();

                Integer sendType = null;
                boolean has0 = false;
                boolean has2 = false;
                B:
                for (Map.Entry<String, MatchPoolPlayerInfo> entry : playerRoomInfoT.getMatchPoolPlayer().entrySet()) {
                    String s = entry.getKey();
                    MatchPoolPlayerInfo playerInfoT = entry.getValue();
                    users.add(playerInfoT.getPlayer());
                    //playerInfoT.setPlayer(null);
                    int isAccept = playerInfoT.getIsAccept();
                    userUidsT.add(s);
                    switch (isAccept) {
                        case 0://待定
                            userDDs.add(s);
                            has0 = true;
                            break;
                        case 1://接受
                            break;
                        default://拒绝
                            userUidsT.remove(s);
                            has2 = true;
                            break;
                    }
                }
                //超过20次直接拒绝
                if (i >= acceptMastTime) {
                    userUidsT.removeAll(userDDs);
                    has2 = true;
                }
                //存在拒绝的玩家
                if (has2) {
                    sendType = SendType.MATCH_FILD;
                    //发送有人拒绝信息

                    sendMsgUtil.sendMsgList(userUids, sendType, new BaseMessage());

                    //删除临时房间
                    playerRoomInfoT.delete(redisObjectUtil);
                    //将接受的玩家重新放入匹配池
                    for (String playerInfoId : userUidsT) {
                        MatchPoolPlayerInfo playerInfo = playerRoomInfoT.getMatchPoolPlayer().get(playerInfoId);
                        playerInfo.setIsAccept(0);
                        playerInfo.save(RedisKey.MATCH_PLAYER_POOL, redisObjectUtil, this);
                    }
                    break A;
                }
                //存在待定玩家
                if (has0) {
                    //等待1秒继续判定
                    ThreadUtil.sleep(1000);
                    continue A;
                }

                //全部接受
                sendType = SendType.MATCH_SUCCESE;
                //给玩家发送所有玩家都接受了的信息
                sendMsgUtil.sendMsgList(userUids, sendType, new BaseMessage());
                //删除临时房间
                playerRoomInfoT.delete(redisObjectUtil);
                logger.info(String.format("匹配成功进入对战：%s", CollUtil.join(users, ",")));
                //创建战斗房间，开启战斗
                fightingService.start(users);
                break A;
            }

        }, true);

    }


    /**
     * 处理玩家接受匹配的信息
     *
     * @param player      玩家
     * @param accept      1.接受   2.拒绝
     * @param playerRoomrInfo playerRoom对象的json
     * @return
     */
    @Override
    public boolean playerAccept(User player, int accept, PlayerRoomInfo playerRoomrInfo) {
        if (accept == 1) {
            logger.info(String.format("玩家接受匹配信息: $s", player.getName()));
        } else {
            logger.info(String.format("玩家拒绝匹配信息: $s", player.getName()));
        }

        if (StrUtil.isEmpty(playerRoomrInfo.getUid())) {
            return false;
        }
        String playerRoomUidKey = RedisKeyUtil.getKey(RedisKey.MATCH_PLAYER_ROOM, playerRoomrInfo.getUid());

        MatchPoolPlayerInfo playerInfo = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKeyUtil.getKey(playerRoomUidKey, "playerInfo"), player.getUid()), MatchPoolPlayerInfo.class);


        playerInfo.setIsAccept(accept);

        playerInfo.save(RedisKeyUtil.getKey(playerRoomUidKey, "playerInfo"), redisObjectUtil, this);
        playerInfo = null;
        return true;
    }
}
