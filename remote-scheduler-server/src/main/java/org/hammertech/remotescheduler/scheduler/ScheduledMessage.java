package org.hammertech.remotescheduler.scheduler;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
class ScheduledMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String jobName;
    private Long expireTime;

}
