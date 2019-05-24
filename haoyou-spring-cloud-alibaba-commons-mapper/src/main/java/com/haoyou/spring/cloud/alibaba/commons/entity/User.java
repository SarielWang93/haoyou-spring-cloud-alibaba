package com.haoyou.spring.cloud.alibaba.commons.entity;



import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


@Data
public class User extends BaseMessage implements Serializable {
    private static final long serialVersionUID = 5542845286421320049L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 昵称
     */
    private String name;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 登录平台提供登陆码
     */
    private String uid;

    /**
     * 货币
     */
    private Integer coin;

    /**
     * 包裹道具栏个数
     */
    @Column(name = "prop_max")
    private Integer propMax;

    /**
     * 状态（正常，删除，封号，等）
     */
    private Integer state;

    /**
     * 登录平台（腾讯，小米，网易……）
     */
    private String platform;

    /**
     * 道具（json存储）
     */
    private String props;

    /**
     * 平台提供信息（json存储）
     */
    @Column(name = "platform_param")
    private String platformParam;

    /**
     * 匹配基准值
     */
    @Column(name = "`rank`")
    private Integer rank;

    /**
     * 钻石
     */
    private Integer diamond;

    /**
     * 体力
     */
    private Integer vitality;

}