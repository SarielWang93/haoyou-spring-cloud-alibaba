package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 获取大厅数据
 */
@Service
public class GetLevelDesignHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetLevelDesignHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_LEVEL_DESIGN;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        User user = req.getUser();

        user.setState(ResponseMsg.MSG_SUCCESS);

        return user.notTooLong();
    }
}
