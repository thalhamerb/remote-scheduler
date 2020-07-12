package org.hammertech.remotescheduler.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteSchedulerBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware, ApplicationListener<ContextRefreshedEvent> {

    private final Logger log = LoggerFactory.getLogger("org.hammertech.remoteschedulerclient.scheduler.RemoteSchedulerBeanPostProcessor");
    private ConfigurableListableBeanFactory beanFactory;
    private RemoteScheduledConfigurer remoteScheduledConfigurer;
    private Environment environment;
    private RemoteSchedulerProperties remoteSchedulerProperties;

    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap(64));
    private Map<String, ScheduledMethod> scheduledMethods = new HashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (!this.nonAnnotatedClasses.contains(targetClass)) {
            boolean hasAnnotations = false;
            for (final Method method : targetClass.getMethods()) {
                RemoteScheduled remoteScheduled = AnnotationUtils.findAnnotation(method, RemoteScheduled.class);
                if (remoteScheduled != null) {
                    hasAnnotations = true;
                    regiesterTask(bean, method, remoteScheduled);
                }
            }

            if (!hasAnnotations) {
                this.nonAnnotatedClasses.add(targetClass);
            }
        }
        return bean;
    }

    private void regiesterTask(Object bean, Method method, RemoteScheduled remoteScheduled) {
        if (!scheduledMethods.containsKey(remoteScheduled.jobName())) {
            ScheduledMethod scheduledMethod = new ScheduledMethod(bean, method);
            scheduledMethods.put(remoteScheduled.jobName(), scheduledMethod);
        } else {
            String message = String.format("Remote schedule for %s defined more than once", remoteScheduled.jobName());
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            throw new IllegalArgumentException("RemoteScheduledBeanPostProcessor requires a ConfigurableListableBeanFactory");
        }
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.nonAnnotatedClasses.clear();
        setBeanFieldsAfterInitialization();

        String appName = environment.getProperty("spring.application.name");
        if (appName == null) {
            throw new IllegalStateException("spring.application.name property not defined");
        }

        registerMessageListenerContainer(appName);
    }

    private void setBeanFieldsAfterInitialization() {
        remoteScheduledConfigurer = beanFactory.getBean(RemoteScheduledConfigurer.class);
        remoteSchedulerProperties = beanFactory.getBean(RemoteSchedulerProperties.class);
        environment = beanFactory.getBean(Environment.class);

        if (remoteScheduledConfigurer == null) {
            throw new IllegalStateException("RemoteScheduledConfigurer not defined");
        }
        if (remoteScheduledConfigurer.getConnectionFactory() == null) {
            throw new IllegalStateException("RemoteScheduledConfigurer's connection factory not defined");
        }
        if (environment == null) {
            throw  new IllegalStateException("spring environment not defined");
        }
    }

    private void registerMessageListenerContainer(String appName) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(remoteScheduledConfigurer.getConnectionFactory());
        String queueName = String.format("%s.%s", remoteSchedulerProperties.getQueueNamePrefix(), appName);
        container.setQueueNames(queueName);
        container.setMessageListener(getMessageListener());
        container.setConcurrentConsumers(remoteSchedulerProperties.getConcurrentConsumers());
        container.setMaxConcurrentConsumers(remoteSchedulerProperties.getMaxConccurentConsumers());
        beanFactory.registerSingleton("org.hammertech.remoteschedulerclient.scheduler.remoteSchedulerListenerContainer", container);
        container.afterPropertiesSet();
        container.start();
        log.info("Listening for Remote Scheduler messages on queue with name: " + queueName);
    }

    private MessageListener getMessageListener() {
        return message -> {
            try {
                ScheduledMessage scheduledMessage = (ScheduledMessage) SerializationUtils.deserialize(message.getBody());
                ScheduledMethod scheduledMethod = scheduledMethods.get(scheduledMessage.getJobName());
                if (scheduledMethod == null) {
                    log.error("Messge for job {} not processed because application not configured to process job", scheduledMessage.getJobName());
                } else if (scheduledMessage.getExpireTime() != null && System.currentTimeMillis() > scheduledMessage.getExpireTime()) {
                    log.info("Message for job {} not processed because it expired at time {}",
                            scheduledMessage.getJobName(), scheduledMessage.getExpireTime());
                } else {
                    scheduledMethod.getMethod().invoke(scheduledMethod.getBean());
                }
            } catch (Exception e) {
                log.error("Unable to process message: {}", message, e);
            }
        };
    }
}