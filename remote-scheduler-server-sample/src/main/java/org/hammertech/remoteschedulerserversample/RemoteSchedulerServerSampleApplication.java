package org.hammertech.remoteschedulerserversample;

import org.hammertech.remotescheduler.EnableRemoteScheduledServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRemoteScheduledServer
public class RemoteSchedulerServerSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemoteSchedulerServerSampleApplication.class, args);
    }

}
