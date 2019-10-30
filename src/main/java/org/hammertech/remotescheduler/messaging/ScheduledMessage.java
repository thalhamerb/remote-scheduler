package org.hammertech.remotescheduler.messaging;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduledMessage {

    private String jobName;
    private Long expireTime;

}
