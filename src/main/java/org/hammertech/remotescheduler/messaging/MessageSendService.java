package org.hammertech.remotescheduler.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
class MessageSendService {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Value("${org.hammertech.prefix")
    private String schedulerPrefix;

    @Value("${org.hammertech.suffix")
    private String schedulerSuffix;

    private Set<String> appQueueNames = new HashSet<>();

    void sendSchedulerMessage(String appName, String jobName, Long epochExpireTime) {
        ScheduledMessage message = new ScheduledMessage();
        message.setJobName(jobName);
        message.setExpireTime(epochExpireTime);
        String queueName = String.format("%s.%s.%s", schedulerPrefix, appName, schedulerSuffix);
        if (!appQueueNames.contains(queueName)) {
            createNewQueue(queueName);
        }
        amqpTemplate.convertAndSend("hammertech-job-exchange", queueName, message);
    }

    void createNewQueue(String queueName) {
        amqpAdmin.declareQueue(new Queue(queueName));
    }
}
