package org.hammertech.remotescheduler.manage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class ScheduleDetail {

    private String appName;
    private String jobName;
    private String description;
    private String cron;
    private ExpireStrategy expireStrategy;
    private Long secondsToExpire;
    private String triggerState;
}
