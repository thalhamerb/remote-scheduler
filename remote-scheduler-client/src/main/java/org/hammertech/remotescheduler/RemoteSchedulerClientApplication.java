package org.hammertech.remotescheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RemoteSchedulerClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(RemoteSchedulerClientApplication.class, args);
	}

}

//todo - add test cases and readme file
//todo - make amqp instead of jms and with rabbitmq instead of activemq
//todo - use gradle instead of maven