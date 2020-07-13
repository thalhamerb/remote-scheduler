package com.hammerteck.remoteschedulerdemo;

import org.hammertech.remotescheduler.scheduler.EnableRemoteScheduled;
import org.hammertech.remotescheduler.scheduler.RemoteScheduled;
import org.hammertech.remotescheduler.scheduler.RemoteScheduledConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableRemoteScheduled
public class RemoteSchedulerClientSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemoteSchedulerClientSampleApplication.class, args);
    }

    @Bean
    RemoteScheduledConfigurer remoteScheduledConfigurer(ConnectionFactory connectionFactory) {
        return new RemoteScheduledConfigurer(connectionFactory);
    }

    @RemoteScheduled(jobName = "test-job")
    public void doSomething() {
        System.out.println("ran test-Job");
    }
}
