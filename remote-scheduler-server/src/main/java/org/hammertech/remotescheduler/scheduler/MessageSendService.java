package org.hammertech.remotescheduler.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageSendService {

    private final AmqpTemplate amqpTemplate;

    private final AmqpAdmin amqpAdmin;

    private final RemoteSchedulerProperties remoteSchedulerProperties;

    private Set<String> appQueueNames = new HashSet<>();

    void sendSchedulerMessage(String appName, String jobName, Long epochExpireTime) {
        ScheduledMessage message = new ScheduledMessage();
        message.setJobName(jobName);
        message.setExpireTime(epochExpireTime);
        String queueName = String.format("%s.%s", remoteSchedulerProperties.getQueueNamePrefix(), appName);
        if (!appQueueNames.contains(queueName) && amqpAdmin.getQueueProperties(queueName) == null) {
            appQueueNames.add(queueName);
            createNewQueue(queueName);
        }
        amqpTemplate.convertAndSend(queueName, message);
    }

    public void createNewQueue(String queueName) {
        amqpAdmin.declareQueue(new Queue(queueName));
    }
}
