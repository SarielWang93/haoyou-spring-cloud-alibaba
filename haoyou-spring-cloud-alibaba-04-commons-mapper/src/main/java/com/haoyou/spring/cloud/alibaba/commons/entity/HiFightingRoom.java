package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "hi_fighting_room")
@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class HiFightingRoom implements Serializable {
    private static final long serialVersionUID = -6293065366466874529L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uid;

    private String player1;

    private String player2;

    @Column(name = "creat_time")
    private Date creatTime;

    @Column(name = "over_time")
    private Date overTime;

    @Column(name = "fighting_room_json")
    private byte[] fightingRoomJson;

}
