package com.haoyou.spring.cloud.alibaba.commons.entity;


import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.ZIP;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;


@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class User extends BaseMessage implements Serializable {

    private static final long serialVersionUID = 5542845286421320049L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 登录平台提供登陆码
     */
    private String uid;

    /**
     * 玩家编号
     */
    @Column(name = "id_num")
    private String idNum;


    /**
     * 服务器id
     */
    @Column(name = "server_id")
    private Integer serverId;

    /**
     * 状态（正常，删除，封号，等）
     */
    private Integer status;

    /**
     * 登录平台（腾讯，小米，网易……）
     */
    private String platform;


    /**
     * 平台提供信息（json存储）
     */
    @Column(name = "platform_param")
    private byte[] platformParam;


    /**
     * 创建时间
     */
    @Column(name = "creat_date")
    private Date creatDate;

    /**
     * 最新修改时间
     */
    @Column(name = "last_update_date")
    private Date lastUpdateDate;

    /**
     * 最后一次登陆时间
     */
    @Column(name = "last_login_date")
    private Date lastLoginDate;

    /**
     * 最后一次登出时间
     */
    @Column(name = "last_login_out_date")
    private Date lastLoginOutDate;

    /**
     * 最终登录ip地址
     */
    @Column(name = "last_login_url")
    private String lastLoginUrl;

    @Transient
    private boolean onLine;

    @Transient
    private Currency currency;

    @Transient
    private UserData userData;

    @Transient
    private Map<String, UserNumerical> userNumericalMap;


    /**
     * 简洁返回用户信息
     *
     * @return
     */
    public User notTooLong() {
        if (this.currency != null) {
            this.currency.setProps(null);
        }
        if (this.userData!= null) {
            this.userData.setDailyCheckIn(null);
            this.userData.setFunds(null);
            this.userData.setEmails(null);
        }
        this.platformParam = null;
        this.userNumericalMap = null;
        this.password = null;
        return this;
    }

    public List<Prop> propList() {
        List<Prop> props = null;
        if (this.currency.getProps() != null) {
            try {
                props = MapperUtils.json2list(new String(ZIP.unGZip(this.currency.getProps()), "UTF-8"), Prop.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            props = new ArrayList<>();
        }
        return props;
    }

    public boolean deleteProp(Prop prop) {

        int count = prop.getCount();

        return deleteProp(prop, count);
    }

    public boolean deleteProp(Prop prop, int count) {
        try {
            List<Prop> propsThis = this.propList();

            int i = 0;
            if ((i = propsThis.indexOf(prop)) != -1) {
                propsThis.get(i).setCount(propsThis.get(i).getCount() - count);
                if (propsThis.get(i).getCount() <= 0) {
                    propsThis.remove(i);
                }
                this.currency.setProps(ZIP.gZip(MapperUtils.obj2jsonIgnoreNull(propsThis).getBytes("UTF-8")));
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&

                Objects.equals(username, user.username) &&
                Objects.equals(password, user.password) &&
                Objects.equals(uid, user.uid) &&

                Objects.equals(creatDate, user.creatDate) &&
                Objects.equals(lastUpdateDate, user.lastUpdateDate);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, username, password, uid, creatDate, lastUpdateDate);
        return result;
    }
}
