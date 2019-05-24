package com.haoyou.spring.cloud.alibaba.manager.controller;

import com.alibaba.dubbo.config.annotation.Service;
import com.haoyou.spring.cloud.alibaba.manager.init.InitData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;


@RestController
public class ManagerController {
    private final static Logger logger = LoggerFactory.getLogger(ManagerController.class);

    @Autowired
    private InitData initData;


    /**
     * 对外接口，用于刷新缓存
     * @param response
     * @return
     */
    @GetMapping(value = "refreshCatch")
    public String refreshCatch(HttpServletResponse response){

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods","GET,POST");


        initData.run(null);

        return "succese";
    }
}
