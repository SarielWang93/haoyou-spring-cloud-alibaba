package com.haoyou.spring.cloud.alibaba.login.controller;

import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.login.UserCatch.UserDateSynchronization;
import com.haoyou.spring.cloud.alibaba.service.cultivate.CultivateService;
import com.haoyou.spring.cloud.alibaba.service.login.LoginService;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ManagerController {
    private final static Logger logger = LoggerFactory.getLogger(ManagerController.class);


    @Autowired
    protected RedisObjectUtil redisObjectUtil;
    @Autowired
    protected LoginService loginService;
    @Autowired
    protected UserDateSynchronization userDateSynchronization;
    @Autowired
    protected UserUtil userUtil;


    @CrossOrigin
    @GetMapping(value = "saveUserToSql")
    public String saveUserToSql(String userUid){

        User user = userUtil.getUserByUid(userUid);
        userUtil.saveSqlUserAndPets(user);
        return "success";

    }
    @CrossOrigin
    @GetMapping(value = "logi")
    public String login(String userUid) throws Exception {
        User user = new User();
        user.setUid(userUid);
        MyRequest myRequest = new MyRequest();
        myRequest.setUser(user);
        myRequest.setUrl("manager");
        User login = loginService.login(myRequest);
        return MapperUtils.obj2jsonIgnoreNull(login);
    }
    @CrossOrigin
    @GetMapping(value = "logou")
    public String logout(String userUid){
        User user = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.USER, userUid), User.class);
        MyRequest myRequest = new MyRequest();
        myRequest.setUser(user);
        myRequest.setUrl("manager");
        loginService.logout(myRequest);
        return "success";
    }
    @CrossOrigin
    @GetMapping(value = "deleteAllUserCatch")
    public String deleteAllUserCatch(){
        userUtil.refreshAllUserCatch();
        return "success";
    }
}
