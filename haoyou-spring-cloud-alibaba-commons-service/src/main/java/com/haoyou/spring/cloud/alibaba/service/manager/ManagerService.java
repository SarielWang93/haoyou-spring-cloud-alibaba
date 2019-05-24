package com.haoyou.spring.cloud.alibaba.service.manager;

import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;

/**
 * 调用manager服务
 */

public interface ManagerService {

    BaseMessage handle(MyRequest req);

}
