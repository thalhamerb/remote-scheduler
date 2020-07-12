package org.hammertech.remotescheduler.scheduler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

@Getter
@Setter
@AllArgsConstructor
public class ScheduledMethod {

    private Object bean;
    private Method method;
}
