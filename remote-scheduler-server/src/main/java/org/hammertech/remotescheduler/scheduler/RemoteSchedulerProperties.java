package org.hammertech.remotescheduler.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("org.hammertech.remote-scheduler")
@Getter
@Setter
public class RemoteSchedulerProperties {

    /**
     * queue name prefix defined in remote scheduler
     */
    private String queueNamePrefix = "org.hammertech.remote-scheduler";

}
