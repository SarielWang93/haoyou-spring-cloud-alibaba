package com.haoyou.spring.cloud.alibaba.cultivate.reward;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Award extends BaseMessage implements Serializable {
    private static final long serialVersionUID = 8040903642874170701L;

    /**
     * 金币
     */
    private int coin;
    /**
     * 钻石
     */
    private int diamond;
    /**
     * 道具
     */
    private List<Prop> props;

    public Award(int coin, int diamond, List<Prop> props) {
        this.coin = coin;
        this.diamond = diamond;
        this.props = props;
    }
}
