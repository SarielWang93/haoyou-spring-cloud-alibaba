package com.haoyou.spring.cloud.alibaba.pojo.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wanghui
 * @version 1.0
 */

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class RankUser implements Serializable {

    private String userUid;

    private String name;

    private String avatar;

    private Long integral;

    private Long rank;

    public RankUser init(User user,Long integral,Long rank){
        this.userUid = user.getUid();
        this.name=user.getUserData().getName();
        this.avatar=user.getUserData().getAvatar();
        this.integral=integral;
        this.rank=rank;
        return this;
    }


}
