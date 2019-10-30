package org.hammertech.remotescheduler.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class SchedulerSendService {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${org.hammertech.prefix")
    private String schedulerPrefix;

    @Value("${org.hammertech.suffix")
    private String schedulerSuffix;

    void sendSchedulerMessage(String appName, String jobName, Long epochExpireTime) {
        ScheduledMessage message = new ScheduledMessage();
        message.setJobName(jobName);
        message.setExpireTime(epochExpireTime);
        String queueName = String.format("%s%s%s", schedulerPrefix, "RUNSCH_" + appName, schedulerSuffix);
        jmsTemplate.convertAndSend(queueName, message);
    }
}
