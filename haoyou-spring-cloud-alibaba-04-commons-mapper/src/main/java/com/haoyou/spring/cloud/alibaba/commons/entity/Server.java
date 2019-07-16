package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Server implements Serializable {
    private static final long serialVersionUID = -2693592131474049664L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 服务器编号
     */
    @Column(name = "server_num")
    private Integer serverNum;

    /**
     * 服务器名称
     */
    @Column(name = "server_name")
    private String serverName;

    /**
     * 创建时间
     */
    @Column(name = "creat_date")
    private Date creatDate;

    /**
     * 最后一次修改时间
     */
    @Column(name = "last_update_date")
    private Date lastUpdateDate;


}
