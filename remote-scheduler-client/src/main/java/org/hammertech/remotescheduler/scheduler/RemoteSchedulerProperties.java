package org.hammertech.remotescheduler.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("org.hammertech.remote-scheduler")
@Getter
@Setter
public class RemoteSchedulerProperties {

    /**
     * min number of queue consumers for consuming remote scheduler messages
     */
    private int concurrentConsumers = 5;

    /**
     * max number of queue consumers for consuming remote scheduler messages
     */
    private int maxConccurentConsumers = 10;

    /**
     * queue name prefix defined in remote scheduler
     */
    private String queueNamePrefix = "org.hammertech.remote-scheduler";

}
