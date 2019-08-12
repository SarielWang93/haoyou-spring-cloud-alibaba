package com.haoyou.spring.cloud.alibaba.commons.message;


import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import lombok.Data;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 信息基类
 */
@Data
public class BaseMessage implements Serializable {


    private static final long serialVersionUID = -4373103763918267983L;






    public static BaseMessage beSuccess(){
        BaseMessage baseMessage = new BaseMessage();
        baseMessage.setState(ResponseMsg.MSG_SUCCESS);
        return baseMessage;
    }
    public static BaseMessage beErr(){
        BaseMessage baseMessage = new BaseMessage();
        baseMessage.setState(ResponseMsg.MSG_ERR);
        return baseMessage;
    }


    /**
     * 信息状态
     */
    @Transient
    protected Integer state;

    /**
     * 额外信息位置
     */
    @Transient
    protected Map<String, Object> otherMsg;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseMessage that = (BaseMessage) o;
        return Objects.equals(state, that.state) &&
                Objects.equals(otherMsg, that.otherMsg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, otherMsg);
    }





}
