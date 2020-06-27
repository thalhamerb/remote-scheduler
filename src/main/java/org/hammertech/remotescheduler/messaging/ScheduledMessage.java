package org.hammertech.remotescheduler.messaging;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
class ScheduledMessage implements Serializable{

    private String jobName;
    private Long expireTime;

}
