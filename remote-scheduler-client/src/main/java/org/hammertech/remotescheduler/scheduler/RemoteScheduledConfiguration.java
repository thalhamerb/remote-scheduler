package org.hammertech.remotescheduler.scheduler;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RemoteSchedulerProperties.class})
public class RemoteScheduledConfiguration {

    @Bean("org.hammertech.remotescheduler.scheduler.remoteScheduedBeanPostProcessor")
    RemoteSchedulerBeanPostProcessor remoteSchedulerBeanPostProcessor() {
        return new RemoteSchedulerBeanPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(RemoteScheduledConfigurer.class)
    public RemoteScheduledConfigurer remoteScheduledConfigurer(ConnectionFactory connectionFactory) {
        return new RemoteScheduledConfigurer(connectionFactory);
    }

}
