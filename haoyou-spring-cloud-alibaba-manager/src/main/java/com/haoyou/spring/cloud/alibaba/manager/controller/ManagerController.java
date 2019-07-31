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



    /**
     * 获取用户信息
     *
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "getUsers")
    public String getUsers() {
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
        User user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, userUid), User.class);
        if (user == null) {
            user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.OUTLINE_USER, userUid), User.class);
        }
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

        HashMap<String, FightingPet> petMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, userUid)), FightingPet.class);

        try {
            return MapperUtils.obj2jsonIgnoreNull(petMap.values());
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


        FightingPet pet = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, userUid), petUid), FightingPet.class);

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
        HashMap<String, Prop> props = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.PROP), Prop.class);

        try {
            return MapperUtils.obj2jsonIgnoreNull(props.values());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
