package com.haoyou.spring.cloud.alibaba.scheduled;


import com.haoyou.spring.cloud.alibaba.email.CheckEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/31 10:48
 * 所有定时操作在此类统一配置
 */
@Service
public class ScheduledService {
    private final static Logger logger = LoggerFactory.getLogger(ScheduledService.class);

    @Autowired
    private CheckEmail checkEmail;

    /**
     * 每隔一小时，检查邮箱
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void inspectSettlement() {

        logger.info(String.format("邮件查询:%s",CheckEmail.user2));

        checkEmail.getEmails(CheckEmail.user2,CheckEmail.password2,CheckEmail.OUTLOOK_HOST);
    }


}
