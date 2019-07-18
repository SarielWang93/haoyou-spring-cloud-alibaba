package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.cultivate.service.RewardService;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import com.haoyou.spring.cloud.alibaba.cultivate.service.PropUseService;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 道具使用类
 */
@Service
@Data
public abstract class PeopUseHandle {

    //获取宠物失败
    final static public int NO_PETTYPE = 1001;
    //容量不足
    final static public int NO_SPACE = 1002;
    //数量不对
    final static public int WRONG_COUNT = 1003;
    //已拥有
    final static public int ALREADY_HAVE = 1004;
    //参数有误
    final static public int WRONG_PRO = 1005;



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


    /**
     * 处理标识
     */
    protected String handleType;

    /**
     * 设置类型并注册到处理中心
     */
    @PostConstruct
    protected void init(){
        setHandleType();
        PropUseService.register(this);
    }

    /**
     * 重写方法，配置标识
     */
    protected abstract void setHandleType();


    /**
     *
     * 道具效果
     * @param propUseMsg
     * @return
     */
    public abstract int handle(PropUseMsg propUseMsg);

    /**
     * 道具使用
     * @param propUseMsg
     * @return
     */
    public int useProp(PropUseMsg propUseMsg){

        if(deleteProp(propUseMsg)){
            return handle(propUseMsg);
        }
        return ResponseMsg.MSG_ERR;
    }

    /**
     * 删除使用掉的道具
     * @param propUseMsg
     * @return
     */
    protected boolean deleteProp(PropUseMsg propUseMsg){
        User user=propUseMsg.getUser();
        Prop prop=propUseMsg.getProp();
        //删除道具并修改玩家信息
        return user.deleteProp(prop,propUseMsg.getPropCount());
    }

}
