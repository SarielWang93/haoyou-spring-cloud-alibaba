package com.haoyou.spring.cloud.alibaba.sofabolt.controller;


import com.alipay.remoting.Connection;
import com.haoyou.spring.cloud.alibaba.service.login.LoginService;
import com.haoyou.spring.cloud.alibaba.sofabolt.connection.Connections;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class ManagerController {
    private final static Logger logger = LoggerFactory.getLogger(ManagerController.class);


    @Autowired
    private Connections connections;


    @CrossOrigin
    @GetMapping(value = "heartbeat")
    public String heartbeat(String userUid){
        Connection connection = connections.get(userUid);
        connection.setAttribute(Connections.HEART_BEAT,new Date());
        return "success";
    }

}
