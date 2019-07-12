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
public class PetUpLevHandle extends ManagerHandle {

    private static final long serialVersionUID = 4862255531025859349L;

    @Override
    protected void setHandleType() {
        this.handleType = SendType.PET_UP_LEV;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        return cultivateService.petUpLev(req);
    }
}
