package com.haoyou.spring.cloud.alibaba.commons.domain.message;


import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 信息基类
 */
@Data
public class BaseMessage implements Serializable {


    private static final long serialVersionUID = -4373103763918267983L;
    /**
     * 信息状态
     */
    protected Integer state;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseMessage that = (BaseMessage) o;
        return Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }
}
