package org.hammertech.remotescheduler.manage;

public class ScheduleDetail {

    private String appName;
    private String jobName;
    private String description;
    private String cron;
    private ExpireStrategy expireStrategy;
    private Long secondsToExpire;
    private String triggerStage;
}
