package org.hammertech.remotescheduler.scheduler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;


@Getter
@AllArgsConstructor
public class RemoteScheduledConfigurer {

    private ConnectionFactory connectionFactory;

}
