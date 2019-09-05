package com.haoyou.spring.cloud.alibaba.manager.controller;

import cn.hutool.core.util.IdUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.mapper.UserMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class ManagerController {
    private final static Logger logger = LoggerFactory.getLogger(ManagerController.class);


    @Autowired
    protected RedisObjectUtil redisObjectUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    protected UserUtil userUtil;


    /**
     * 获取用户信息
     *
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "getUsers")
    public String getUsers() {
        logger.info("getUsers");
        List<User> users = userMapper.selectAll();

        for (User user : users) {
            user.notTooLong();
            user.setPassword("");
        }
        try {
            return MapperUtils.obj2jsonIgnoreNull(users);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "getUser")
    public String getUser(String userUid) {
        logger.info("userUid");
        User user = userUtil.getUserByUid(userUid);
        if (user == null) {
            return null;
        }
        user.notTooLong();
        try {
            return MapperUtils.obj2jsonIgnoreNull(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 获取用户信息
     *
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "refreshUser")
    public String refreshUser(String userUid) {
        logger.info("userUid");
        User user = userUtil.refreshCatch(userUid);
        if (user == null) {
            return null;
        }
        user.notTooLong();
        try {
            return MapperUtils.obj2jsonIgnoreNull(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 获取玩家的宠物信息
     *
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "getPets")
    public String getPets(String userUid) {
        logger.info("getPets");
        List<FightingPet> byUser = FightingPet.getByUser(userUid, redisObjectUtil);

        try {
            return MapperUtils.obj2jsonIgnoreNull(byUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取宠物信息
     *
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "getPet")
    public String getPet(String userUid, String petUid) {
        logger.info("getPet");

        FightingPet pet = FightingPet.getByUserAndPetUid(userUid,petUid,redisObjectUtil);

        try {
            return MapperUtils.obj2jsonIgnoreNull(pet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * UUID在线工具
     *
     * @param count
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "getSimpleUUID")
    public String getSimpleUUID(int count, int type) {
        logger.info("getSimpleUUID");
        List<String> uuids = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            if(type == 1){
                uuids.add(IdUtil.simpleUUID());
            }else{
                uuids.add(IdUtil.randomUUID());
            }
        }

        try {
            return MapperUtils.obj2jsonIgnoreNull(uuids);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取道具列表
     *
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "getProps")
    public String getProps() {
        logger.info("getProps");
        HashMap<String, Prop> props = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.PROP), Prop.class);

        try {
            return MapperUtils.obj2jsonIgnoreNull(props.values());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
