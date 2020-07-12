package org.hammertech.remotescheduler.scheduler;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RemoteScheduled {

    String jobName();
}
