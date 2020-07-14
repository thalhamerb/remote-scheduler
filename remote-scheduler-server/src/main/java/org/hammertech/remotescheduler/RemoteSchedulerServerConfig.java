package org.hammertech.remotescheduler;

import org.hammertech.remotescheduler.scheduler.RemoteSchedulerServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "org.hammertech.remotescheduler")
@EnableConfigurationProperties({RemoteSchedulerServerProperties.class})
public class RemoteSchedulerServerConfig {


}
