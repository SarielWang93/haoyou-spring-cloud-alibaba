package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 获取背包道具
 */
@Service
public class GetPropsHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetPropsHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_PROPS;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        User user = req.getUser();
        byte[] msg = req.getMsg();
        Map<String, Object> msgMap = null;
        try {
            msgMap =  MapperUtils.json2map(new String(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }

        MapBody mapBody = new MapBody<>();
        mapBody.setState(ResponseMsg.MSG_ERR);
        if(msgMap != null){
            mapBody.setState(ResponseMsg.MSG_SUCCESS);
            List<Prop> props = user.propList();
            for(Prop prop:props){
                if(prop.getName().equals(msgMap.get("name"))){
                    mapBody.put("prop",prop);
                    sendMsgUtil.sendMsgOneNoReturn(user.getUid(),req.getId(),mapBody);
                }
            }
        }
        mapBody.remove("prop");
        return mapBody;
    }
}
