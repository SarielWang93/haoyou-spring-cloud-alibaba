package com.haoyou.spring.cloud.alibaba.manager.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.pojo.bean.Badge;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;
import com.haoyou.spring.cloud.alibaba.util.ScoreRankUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.apache.dubbo.config.annotation.Reference;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.manager.service.impl.ManagerServiceImpl;
import com.haoyou.spring.cloud.alibaba.service.cultivate.CultivateService;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.service.fighting.FightingService;
import com.haoyou.spring.cloud.alibaba.service.login.LoginService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.service.match.MatchService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.*;

/**
 * 消息处理器基类
 */
@Service
@Data
public abstract class ManagerHandle implements Serializable {
    private static final long serialVersionUID = 4685902869191400040L;


    /**
     * 各系统服务
     */
    @Reference(version = "${match.service.version}")
    protected MatchService matchService;
    @Reference(version = "${login.service.version}")
    protected LoginService loginService;
    @Reference(version = "${fighting.service.version}")
    protected FightingService fightingService;
    @Reference(version = "${cultivate.service.version}")
    protected CultivateService cultivateService;


    /**
     * 工具
     */
    @Autowired
    protected RedisObjectUtil redisObjectUtil;
    @Autowired
    protected SendMsgUtil sendMsgUtil;

    @Autowired
    protected ScoreRankUtil scoreRankUtil;

    @Autowired
    protected UserUtil userUtil;


    /**
     * 处理标识
     */
    protected Integer handleType;

    /**
     * 设置类型并注册到处理中心
     */
    @PostConstruct
    protected void init(){
        setHandleType();
        ManagerServiceImpl.putManagerHanderMap(this);
    }

    /**
     * 重写方法，配置标识
     */
    protected abstract void setHandleType();

    /**
     * 处理方法
     * @param req
     * @return
     */
    public abstract BaseMessage handle(MyRequest req);


    /**
     * 获取参数map
     * @param req
     * @return
     */
    protected Map<String, Object> getMsgMap(MyRequest req){
        return userUtil.getMsgMap(req);
    }



    /**
     * 获取已发放的奖励
     * @param userUid
     * @param type
     * @return
     */
    protected Award getUpAward(String userUid, String type){
        String key = RedisKeyUtil.getKey(RedisKey.USER_AWARD, userUid, type);
        return redisObjectUtil.get(key, Award.class);
    }




    protected void otherMsg(User login) {
        Map<String, Object> otherMsg = new HashMap<>();
        List<FightingPet> byUser = FightingPet.getByUser(login, redisObjectUtil);
        //宠物个数
        otherMsg.put("petsCount", byUser.size());

        //服务器名字
        Server server = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.SERVER, login.getServerId().toString()), Server.class);
        otherMsg.put("serverName", server.getServerName());
        //服务器时间
        otherMsg.put("serverDate", new Date());

        //当前服排名
        Long aLong = scoreRankUtil.find(RedisKeyUtil.getKey(RedisKey.RANKING, server.getId().toString()), login);
        otherMsg.put("serverRankNum", aLong);

        //未读邮件数量
        TreeMap<Date, Email> emails = userUtil.getEmails(login);
        int i = 0;
        for (Email email1 : emails.values()) {
            if (!email1.isHaveRead()) {
                i++;
            }
        }
        otherMsg.put("emailsCount", i);

        //好友数量
        List<Friends> friends = userUtil.getFriends(login.getUid());
        otherMsg.put("friendsCount", friends.size());

        //战斗总场次
        Long fightingCount = login.getUserNumericalMap().get("fighting_count").getValue();
        otherMsg.put("fightingCount", fightingCount);
        //战斗胜利总场次
        Long fightingWinCount = login.getUserNumericalMap().get("fighting_win_count").getValue();
        otherMsg.put("fightingWinCount", fightingWinCount);


        //天梯最高排名
        Long ladderMaxRanking = login.getUserNumericalMap().get("ladder_max_ranking").getValue();
        otherMsg.put("ladderMaxRanking", ladderMaxRanking);

        //天梯胜率
        Long ladderCount = login.getUserNumericalMap().get("ladder_count").getValue();
        if(ladderCount == 0){
            ladderCount = 1L;
        }
        Long ladderWinCount = login.getUserNumericalMap().get("ladder_win_count").getValue();
        otherMsg.put("ladderWinPercent", ladderWinCount * 100 / ladderCount);

        //进入传奇联赛次数
        Long ladderInCount = login.getUserNumericalMap().get("ladder_in_count").getValue();
        otherMsg.put("ladderInCount", ladderInCount);


        //完成任务个数
        Long taskCompletedCount = login.getUserNumericalMap().get("task_completed_count").getValue();
        otherMsg.put("taskCompletedCount", taskCompletedCount);

        //闯关模式进度
        List<Badge> badges = userUtil.getBadges(login);
        String chapterKey = RedisKeyUtil.getKey(RedisKey.CHAPTER, "DarkForest");
        Chapter chapter = redisObjectUtil.get(chapterKey, Chapter.class);
        Integer idNum = 1;
        for (Badge badge : badges) {
            if (badge.getDifficult() == 0) {
                String tchapterName = badge.getChapterName();
                String tchapterKey = RedisKeyUtil.getKey(RedisKey.CHAPTER, tchapterName);
                Chapter tchapter = redisObjectUtil.get(tchapterKey, Chapter.class);
                if (tchapter.getIdNum() < chapter.getIdNum()) {
                    continue;
                }
                if (tchapter.getIdNum() > chapter.getIdNum()) {
                    chapter = tchapter;
                    idNum = 1;
                } else {
                    if (badge.getIdNum() > idNum) {
                        idNum = badge.getIdNum();
                    }
                }
            }
        }
        otherMsg.put("pveChapter", chapter.getL10n());
        otherMsg.put("pveLevelDesignIdNum", idNum);

        //闯关徽章数
        otherMsg.put("badgesCount", badges.size());

        login.setOtherMsg(otherMsg);
    }

}
