package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Friends implements Serializable {
    private static final long serialVersionUID = -6354380084396747117L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 玩家1
     */
    @Column(name = "user_uid1")
    private String userUid1;

    /**
     * 玩家2
     */
    @Column(name = "user_uid2")
    private String userUid2;

    @Column(name = "creat_time")
    private Date creatTime;

    /**
     * 聊天记录
     */
    @Column(name = "chat_record")
    private byte[] chatRecord;

    /**
     * 亲密度
     */
    private Integer intimacy;

}
