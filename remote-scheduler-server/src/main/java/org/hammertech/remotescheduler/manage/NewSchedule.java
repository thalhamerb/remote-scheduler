package org.hammertech.remotescheduler.manage;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
class NewSchedule {

    @NotBlank(message = "app name required")
    private String appName;

    @NotBlank(message = "job name required")
    private String jobName;

    private String description;

    @NotBlank(message = "cron required")
    private String cron;

    private ExpireStrategy expireStrategy;

    private Long secondsToExpire;
}
