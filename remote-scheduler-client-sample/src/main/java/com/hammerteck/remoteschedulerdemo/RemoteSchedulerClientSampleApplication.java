package com.hammerteck.remoteschedulerdemo;

import org.hammertech.remotescheduler.scheduler.EnableRemoteScheduled;
import org.hammertech.remotescheduler.scheduler.RemoteScheduled;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRemoteScheduled
public class RemoteSchedulerClientSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemoteSchedulerClientSampleApplication.class, args);
    }

    @RemoteScheduled(jobName = "test-job")
    public void doSomething() {
        System.out.println("ran test-Job");
    }
}
