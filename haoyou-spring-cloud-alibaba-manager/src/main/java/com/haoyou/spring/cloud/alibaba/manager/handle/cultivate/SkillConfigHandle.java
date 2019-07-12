package com.haoyou.spring.cloud.alibaba.manager.handle.cultivate;


import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 技能配置处理
 */
@Service
public class SkillConfigHandle extends ManagerHandle {


    private static final long serialVersionUID = -5962430415616625581L;
    private static final Logger logger = LoggerFactory.getLogger(SkillConfigHandle.class);
    @Override
    protected void setHandleType() {
        this.handleType = SendType.SKILL_CONFIG;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        BaseMessage baseMessage = new BaseMessage();
        if(cultivateService.skillConfig(req)){
            baseMessage.setState(ResponseMsg.MSG_SUCCESS);
        }else{
            baseMessage.setState(ResponseMsg.MSG_ERR);
        }
        return baseMessage;
    }
}
