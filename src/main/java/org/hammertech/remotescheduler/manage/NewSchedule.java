package org.hammertech.remotescheduler.manage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewSchedule {

    private String appName;
    private String jobName;
    private String description;
    private String cron;
    private ExpireStrategy expireStrategy;
    private Long secondsToExpire;
}
