package com.haoyou.spring.cloud.alibaba.controller;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alipay.remoting.Connection;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Protocol;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
public class BeaconController {
    private final static Logger logger = LoggerFactory.getLogger(BeaconController.class);


    @Autowired
    private RedisObjectUtil redisObjectUtil;


    @CrossOrigin
    @GetMapping(value = "getMsg")
    public String getMsg(String deviceIdNum, Integer count) {
        String lkKey = RedisKeyUtil.getlkKey(RedisKey.BEACON, deviceIdNum);
        HashMap<String, Protocol> stringProtocolHashMap = redisObjectUtil.getlkMap(lkKey, Protocol.class);

        List<Protocol> protocols = CollUtil.newArrayList(stringProtocolHashMap.values());

        //toBaiDuTooLong(protocols);

        //分设备，按时间排序
        Map<String, TreeMap<Date, Protocol>> msg = new HashMap<>();
        for (Protocol protocol : protocols) {
            protocol.setText(null);
            String deviceIdNum1 = protocol.getDeviceIdNum();
            TreeMap<Date, Protocol> treeMap = msg.get(deviceIdNum1);
            if (treeMap == null) {
                treeMap = new TreeMap<>();
                treeMap.put(protocol.getSendDate(), protocol);
                msg.put(deviceIdNum1, treeMap);
            } else {
                treeMap.put(protocol.getSendDate(), protocol);
            }
        }

        Map<String, Collection<Protocol>> rtMsg = new HashMap<>();

        for (Map.Entry<String, TreeMap<Date, Protocol>> entry : msg.entrySet()) {
            List<Protocol> protocols1 = CollUtil.newArrayList(entry.getValue().values());
            if (count != null && count > 0 && count < protocols1.size()) {
                protocols1 = protocols1.subList(protocols1.size() - count, protocols1.size());
            }

            rtMsg.put(entry.getKey(), protocols1);
        }


        try {
            return MapperUtils.obj2json(rtMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    //坐标转换api单次最大转换个数
    private static int maxCount = 100;
    //百度地图密匙
    private static String ak = "PhGsQImzfSjThNanj5jV6u9Ot5OljMpG";

    /**
     * 根据单次最大转换个数截取，分批转换
     *
     * @param protocols
     */
    private void toBaiDuTooLong(List<Protocol> protocols) {
        int size = protocols.size();
        if (size > maxCount) {
            int count = size / maxCount;

            for (int i = 0; i <= count; i++) {
                int begin = i * maxCount;
                int end = (i + 1) * maxCount;
                if (end > size) {
                    end = size;
                }
                List<Protocol> protocols1 = protocols.subList(begin, end);
                toBaiDu(protocols1);
            }
        } else {
            toBaiDu(protocols);
        }

    }

    private void toBaiDu(List<Protocol> protocols) {
        if (protocols.isEmpty()) {
            return;
        }
        List<Protocol> bad = new ArrayList<>();
        for (Protocol protocol : protocols) {
            Date sendDate = protocol.getSendDate();
            Double latitude = protocol.getLatitude();
            Double longitude = protocol.getLongitude();
            if (sendDate == null || latitude == null || longitude == null) {
                bad.add(protocol);
            }
        }
        protocols.removeAll(bad);

        StringBuilder builder = StrUtil.builder();
        for (Protocol protocol : protocols) {
            Double longitude = protocol.getLongitude();
            //longitude+=0.04434639353;
            protocol.setLongitude(longitude);
            builder.append(longitude);
            builder.append(",");
            Double latitude = protocol.getLatitude();
            //latitude+=0.05519100371961;
            protocol.setLatitude(latitude);
            builder.append(latitude);
            builder.append(";");
        }
        //转百度地图坐标
        String url = "http://api.map.baidu.com/geoconv/v1/?coords=" + builder.substring(0, builder.toString().length() - 1) + "&from=1&to=5&ak=" + ak;
        String rt = HttpUtil.get(url);

        Map<String, Object> stringObjectMap = null;
        try {
            stringObjectMap = MapperUtils.json2map(rt);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Console.log(stringObjectMap.get("status"));
//        Console.log(stringObjectMap.get("result"));
        List<Map> result = (List) stringObjectMap.get("result");
        for (int i = 0; i < protocols.size(); i++) {
            Protocol protocol = protocols.get(i);
            protocol.setLongitude((Double) result.get(i).get("x"));
            protocol.setLatitude((Double) result.get(i).get("y"));
        }
    }


}
