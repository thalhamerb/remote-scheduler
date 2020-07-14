package org.hammertech.remotescheduler;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RemoteSchedulerServerConfig.class})
@Documented
public @interface EnableRemoteScheduledServer {


}
