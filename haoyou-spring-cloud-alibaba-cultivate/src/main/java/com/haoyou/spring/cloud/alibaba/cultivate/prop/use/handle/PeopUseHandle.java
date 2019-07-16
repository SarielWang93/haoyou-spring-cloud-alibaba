package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
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
    public abstract boolean handle(PropUseMsg propUseMsg);

    /**
     * 道具使用
     * @param propUseMsg
     * @return
     */
    public boolean useProp(PropUseMsg propUseMsg){

        if(deleteProp(propUseMsg)){
            return handle(propUseMsg);
        }
        return false;
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
