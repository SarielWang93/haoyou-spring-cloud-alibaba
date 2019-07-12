package com.haoyou.spring.cloud.alibaba.cultivate.reward;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
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
     * 经验
     */
    private int exp;

    /**
     * 道具
     */
    private List<Prop> props;


    public Award(int coin, int diamond, int exp, List<Prop> props) {
        this.coin = coin;
        this.diamond = diamond;
        this.exp = exp;
        this.props = props;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Award award = (Award) o;
        return coin == award.coin &&
                diamond == award.diamond &&
                exp == award.exp &&
                Objects.equals(props, award.props);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coin, diamond, exp, props);
    }
}
