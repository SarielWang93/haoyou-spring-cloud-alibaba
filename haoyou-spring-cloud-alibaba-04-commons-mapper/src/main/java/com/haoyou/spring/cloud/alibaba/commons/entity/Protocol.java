package com.haoyou.spring.cloud.alibaba.commons.entity;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@JsonIgnoreProperties(value = {"userUid"}, ignoreUnknown = true)
public class Protocol implements Serializable {
    private static final long serialVersionUID = 9021066524016535166L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 设备idNum
     */
    @Column(name = "device_id_num")
    private String deviceIdNum;

    /**
     * 设备类型
     */
    @Column(name = "device_type")
    private Integer deviceType;

    /**
     * 信息发送时间
     */
    @Column(name = "send_date")
    private Date sendDate;

    /**
     * 维度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 南北半球
     */
    private String nshemisphere;

    /**
     * 东西半球
     */
    private String ewhemisphere;

    /**
     * 协议内容
     */
    private String text;


    //$B,2,190821072404,3908.1792,N,11706.6825,E,*
    //117.066825,39.081792
    //117.11117139353,39.13698300371961
    //0.04434639353,0.05519100371961



    public void analysis(String msg) {
        try {
            String[] split = msg.split(",");

            this.deviceType = Integer.valueOf(split[1]);
            this.latitude = gpsToWGS84(Double.valueOf(split[3]));
            this.nshemisphere = split[4];

            this.longitude = gpsToWGS84(Double.valueOf(split[5]));
            this.ewhemisphere = split[6];
            //子午线时间
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");
            Date parse = null;

            parse = simpleDateFormat.parse(split[2]);

            //北京时间
            DateTime dateTime = DateUtil.offsetHour(parse, 8);
            this.sendDate = dateTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Double gpsToWGS84(Double gps){
        return  (int)(gps/100) + (gps/100.0 - (int)(gps/100)) *100.0 / 60.0;
    }

}
