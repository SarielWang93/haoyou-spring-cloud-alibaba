package com.haoyou.spring.cloud.alibaba.sofabolt.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import lombok.Data;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class VersionReqMsg extends BaseMessage {

    private static final long serialVersionUID = 8613414215580464286L;
    private String version;

}