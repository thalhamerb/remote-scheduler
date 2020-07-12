package org.hammertech.remotescheduler.scheduler;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RemoteScheduledConfiguration.class})
@Documented
public @interface EnableRemoteScheduled {
}
