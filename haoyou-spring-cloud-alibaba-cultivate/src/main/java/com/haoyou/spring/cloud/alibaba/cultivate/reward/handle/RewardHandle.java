package com.haoyou.spring.cloud.alibaba.cultivate.reward.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.reward.Award;
import com.haoyou.spring.cloud.alibaba.cultivate.service.RewardService;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

@Service
@Data
public abstract class RewardHandle {

    @Autowired
    protected RedisObjectUtil redisObjectUtil;
    @Autowired
    protected SendMsgUtil sendMsgUtil;
    @Autowired
    private PetMapper petMapper;

    @Autowired
    protected UserMapper userMapper;


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
        RewardService.register(this);
    }

    /**
     * 重写方法，配置标识
     */
    protected abstract void setHandleType();


    /**
     * 奖励生成
     * @param user
     * @return
     */
    public abstract boolean handle(User user);

    /**
     * 奖励存储给玩家
     * @param user
     */
    public boolean save(User user){
        if(redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.USER, user.getUid()), user)){
            return userMapper.updateByPrimaryKeySelective(user)==1;
        }
        return false;
    }

    /**
     * 发送奖励信息给玩家
     * @param user
     * @param award
     */
    public boolean send(User user, Award award){
        if(sendMsgUtil.connectionIsAlive(user.getUid())){
            return sendMsgUtil.sendMsgOneNoReturn(user.getUid(), SendType.AWARD, award);
        }else{
            return false;
        }
    }

    /**
     * 奖励处理
     * @param user
     * @param award
     */
    public void doAward(User user, Award award){
        boolean mail=true;
        if(this.send(user,award)){
            //增加货币
            user.setCoin(user.getCoin() + award.getCoin());
            user.setDiamond(user.getDiamond() + award.getDiamond());
            this.save(user);

            //增加经验
            List<FightingPet> fightingPets = FightingPet.getByUser(user, redisObjectUtil);
            for (FightingPet fightingPet : fightingPets) {
                Integer iswork = fightingPet.getIswork();
                if (iswork != null && iswork != 0) {
                    fightingPet.upExp(award.getExp());
                    fightingPet.save();
                    petMapper.updateByPrimaryKeySelective(fightingPet.getPet());
                }
            }

            //增加道具
            if(user.addProps(award.getProps())){
                if(this.save(user)){
                    mail=false;
                }
            }
        }

        if(mail){
            //TODO 发送邮件给玩家
        }
    }
}
