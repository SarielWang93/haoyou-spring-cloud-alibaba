package com.haoyou.spring.cloud.alibaba.commons.entity;



import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
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

    /**
     * 创建时间
     */
    @Column(name = "creat_date")
    private Date creatDate;

    /**
     * 创建时间
     */
    @Column(name = "last_update_date")
    private Date lastUpdateDate;

    /**
     * 最后一次登陆时间
     */
    @Column(name = "last_login_date")
    private Date lastLoginDate;

    /**
     * 最后一次登陆时间
     */
    @Column(name = "last_login_out_date")
    private Date lastLoginOutDate;

    public User notTooLong(){
        this.props=null;
        this.platformParam=null;
        return this;
    }

    public List<Prop> propList(){
        List<Prop> props = null;
        if (StrUtil.isNotEmpty(this.props)) {
            try {
                props = MapperUtils.json2list(this.props, Prop.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            props = new ArrayList<>();
        }
        return props;
    }

    public boolean addProps(List<Prop> propList){
        try {
            List<Prop> propsThis = this.propList();
            if(propsThis.size() < this.propMax){
                for(Prop prop:propList){
                    int i = 0;
                    if ((i = propsThis.indexOf(prop)) != -1) {
                        propsThis.get(i).setCount(propsThis.get(i).getCount() + 1);
                    } else {
                        prop.setCount(1);
                        propsThis.add(prop);
                    }
                }
                this.props=MapperUtils.obj2jsonIgnoreNull(propsThis);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}