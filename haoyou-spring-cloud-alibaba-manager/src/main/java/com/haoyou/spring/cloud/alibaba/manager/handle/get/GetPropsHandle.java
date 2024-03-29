package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import cn.hutool.core.util.StrUtil;
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

import java.util.ArrayList;
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

        Map<String, Object> msgMap = getMsgMap(req);

        List<Prop>  sdprops = new ArrayList<>();

        MapBody mapBody = new MapBody<>();
        mapBody.setState(ResponseMsg.MSG_ERR);

        String reqUid = "";

        if (msgMap != null) {
            reqUid = (String)msgMap.get("reqUid");
            mapBody.setState(ResponseMsg.MSG_SUCCESS);
            List<Prop> props = user.propList();

            for (Prop prop : props) {
                if (prop.getName().equals(msgMap.get("name")) || StrUtil.isEmpty((String) msgMap.get("name"))) {
                    sdprops.add(prop);
                }
            }

        }


        for(Prop prop : sdprops){
            mapBody.put("reqUid", reqUid);
            mapBody.put("prop", prop);
            mapBody.put("count", sdprops.size());
            sendMsgUtil.sendMsgOneNoReturn(user.getUid(), req.getId(), mapBody);
        }

        mapBody.remove("prop");
        mapBody.remove("count");
        mapBody.remove("reqUid");

        Integer propMax = user.getCurrency().getPropMax();
        mapBody.put("name",msgMap.get("name"));
        mapBody.put("propMaxUpDiamond",propMax / 10 * 10 * propMax + (propMax % 10)*10);

        return mapBody;
    }
}
