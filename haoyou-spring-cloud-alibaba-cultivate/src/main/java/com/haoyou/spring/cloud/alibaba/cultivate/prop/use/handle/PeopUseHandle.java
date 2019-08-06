package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.cultivate.service.RewardService;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.cultivate.service.PropUseService;
import com.haoyou.spring.cloud.alibaba.service.cultivate.CultivateService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * 道具使用类
 */
@Service
@Data
public abstract class PeopUseHandle {

    //获取宠物类型失败
    final static public int NO_PETTYPE = 1001;
    //容量不足
    final static public int NO_SPACE = 1002;
    //数量不对
    final static public int WRONG_COUNT = 1003;
    //已拥有
    final static public int ALREADY_HAVE = 1004;
    //超出上限
    final static public int LIMIT = 1005;
    //参数有误
    final static public int WRONG_PRO = 1006;


    @Autowired
    protected RedisObjectUtil redisObjectUtil;
    @Autowired
    protected SendMsgUtil sendMsgUtil;
    @Autowired
    private PetMapper petMapper;

    @Autowired
    protected UserMapper userMapper;

    @Autowired
    protected RewardService rewardService;
    @Autowired
    protected CultivateService cultivateService;


    @Autowired
    protected UserUtil userUtil;

    /**
     * 处理标识
     */
    protected String handleType;

    /**
     * 设置类型并注册到处理中心
     */
    @PostConstruct
    protected void init() {
        setHandleType();
        PropUseService.register(this);
    }

    /**
     * 重写方法，配置标识
     */
    protected abstract void setHandleType();


    /**
     * 道具效果
     *
     * @param propUseMsg
     * @return
     */
    public abstract MapBody handle(PropUseMsg propUseMsg);

    /**
     * 道具使用
     *
     * @param propUseMsg
     * @return
     */
    public MapBody useProp(PropUseMsg propUseMsg) {
        MapBody rt = new MapBody();
        if (deleteProp(propUseMsg)) {
            rt = handle(propUseMsg);
            if (rt.getState() != ResponseMsg.MSG_SUCCESS) {
                User user = propUseMsg.getUser();
                Prop prop = propUseMsg.getProp();
                prop.setCount(propUseMsg.getPropCount());
                userUtil.addProp(user,prop);
            }
            return rt;
        }
        rt.setState(ResponseMsg.MSG_ERR);
        return rt;
    }

    /**
     * 删除使用掉的道具
     *
     * @param propUseMsg
     * @return
     */
    protected boolean deleteProp(PropUseMsg propUseMsg) {
        User user = propUseMsg.getUser();
        Prop prop = propUseMsg.getProp();
        //删除道具并修改玩家信息
        if (user.deleteProp(prop, propUseMsg.getPropCount())) {
            user.setLastUpdateDate(new Date());
//            return redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.USER, user.getUid()), user);
            return true;
        }
        return false;

    }

}
