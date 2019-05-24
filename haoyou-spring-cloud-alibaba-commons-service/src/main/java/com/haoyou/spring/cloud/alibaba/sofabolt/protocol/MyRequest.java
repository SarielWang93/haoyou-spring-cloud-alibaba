package com.haoyou.spring.cloud.alibaba.sofabolt.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import lombok.Data;

import java.io.Serializable;

/**
 * 请求统一封装类
 * 注意：必须实现 Serializable 接口，因为默认的编码器：ProtocolCodeBasedEncoder extends MessageToByteEncoder<Serializable>，
 * 只对 Serializable 实现类进行编码
 */
@Data
@JsonIgnoreProperties(value = {"msgJson","user"},ignoreUnknown = true)
public class MyRequest implements Serializable {

    private static final long serialVersionUID = -9210417058872020109L;

    //manager type
    @JsonProperty(value = "Id")
    private Integer id;
    //用户uid
    private String useruid;
    //信息内容
    private byte[] msg;

    //玩家
    private User user;

    //信息内容json（临时使用）
    private String msgJson;


    public MyRequest() {
    }

    public MyRequest(Integer id, String useruid, byte[] msg) {
        this.id = id;
        this.useruid = useruid;
        this.msg = msg;
    }
}
