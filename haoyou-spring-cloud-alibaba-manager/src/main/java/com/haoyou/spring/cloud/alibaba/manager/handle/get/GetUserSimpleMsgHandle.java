package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取排行榜数据
 */
@Service
public class GetUserSimpleMsgHandle extends ManagerHandle {


    private static final long serialVersionUID = -7233847046616375275L;
    private static final Logger logger = LoggerFactory.getLogger(GetUserSimpleMsgHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_USER_SIMPLE_MSG;
    }

    @Override
    public BaseMessage handle(MyRequest req) {

        Map<String, Object> msgMap = this.getMsgMap(req);

        String userUid = (String) msgMap.get("userUid");
        User user = userUtil.getUserByUid(userUid);
        List<Pet> userPets = userUtil.getUserPets(userUid);

        if (user == null) {
            return BaseMessage.beErr();
        }

        MapBody mapBody = new MapBody<>();
        mapBody.setState(ResponseMsg.MSG_SUCCESS);


        UserData userData = user.getUserData();

        mapBody.put("name", userData.getName());
        mapBody.put("avatar", userData.getAvatar());
        mapBody.put("level", userData.getLevel());
        mapBody.put("idNum", user.getIdNum());

        if(userPets.size()>3){
            userPets = userPets.subList(0, 3);
        }

        List<Map> petsMap = new ArrayList<>();
        for (Pet pet : userPets) {
            Map<String,Object> petMap = new HashMap<>();

            petMap.put("id",pet.getId());
            petMap.put("nickName",pet.getNickName());
            petMap.put("typeName",pet.getTypeName());
            petMap.put("level",pet.getLevel());
            petMap.put("starClass",pet.getStarClass());

            petsMap.add(petMap);
        }


        return mapBody;
    }
}
