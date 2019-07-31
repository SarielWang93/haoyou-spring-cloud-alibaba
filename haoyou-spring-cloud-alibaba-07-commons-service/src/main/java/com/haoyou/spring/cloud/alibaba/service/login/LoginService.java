package com.haoyou.spring.cloud.alibaba.service.login;

import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;

/**
 * 调用manager服务
 */

public interface LoginService {


    User login(MyRequest req);

    User logout(MyRequest req);

    User register(MyRequest req);

    void synchronization();
}
