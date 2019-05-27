package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 获取背包道具
 */
@Service
public class GetPropsHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetPropsHandle.class);

    public final static int GET_PET_TYPE = 0;//获取宠物模型

    @Override
    protected void setHandleType() {
        this.handleType = ManagerHandle.GET_PROPS;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        User user = req.getUser();

        List<Prop> props = user.propList();

        MapBody mapBody = new MapBody<>();

        mapBody.put("props",props);

        mapBody.setState(ResponseMsg.MSG_SUCCESS);

        return mapBody;
    }
}
