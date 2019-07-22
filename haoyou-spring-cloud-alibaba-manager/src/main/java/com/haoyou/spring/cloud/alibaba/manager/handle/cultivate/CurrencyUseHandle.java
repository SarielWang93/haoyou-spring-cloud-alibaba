package com.haoyou.spring.cloud.alibaba.manager.handle.cultivate;

import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.springframework.stereotype.Service;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/6/28 16:14
 */
@Service
public class CurrencyUseHandle extends ManagerHandle {


    private static final long serialVersionUID = -5925740476963958215L;

    @Override
    protected void setHandleType() {
        this.handleType = SendType.CURRENCY_USE;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        return cultivateService.currencyUse(req);
    }
}
