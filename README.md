# Remote Scheduler

This is a centralized scheduler that manages cron jobs across multiple services.  Inspired by Spring Scheduling and backed by 
Quartz scheduler, this service is targeted for a message-driven microservices architecture where schedules 
are managed across many services.  

## Getting Started

The Remote Scheduler service requires a datasource and an AMQP message broker.  By default, 
an in-memory datasource is used, but can be configured to an external database (example below).  

The Remote Scheduler initiates jobs on client services by posting messages to an AMQP message broker 
that is consumed by the client services.  The method annotated with the job name is ran.  


### Prerequisites

Download a standalone AMQP message broker (ex. RabbitMQ or Apache Kafka).  It is recommended to use Docker Hub 
for a quick setup (https://hub.docker.com/_/rabbitmq)


### Setting up Remote Scheduler Server

Create a new project, add the remote scheduler server dependency to the pom

```

<dependency>
    <groupId>org.hammertech</groupId>
    <artifactId>remote-scheduler-server</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Annotate main class with @EnableRemoteScheduledServer 

```
@SpringBootApplication
@EnableRemoteScheduledServer
public class RemoteSchedulerServerSampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(RemoteSchedulerServerSampleApplication.class, args);
    }
}
```

Define AMQP message broker properties in application.yml file.  Below is the default.

```
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

Start the application and create an example schedule for a client app called "test-app".  

```
POST http://localhost:8096/jobs

    {
      "appName": "test-app",
      "jobName": "test-job",
      "cron": "0 0/1 * 1/1 * ? *"
    }
```

Look at remote-scheduler-server-sample module for an example.

### Integrate Remote Scheduler Client

In your Spring Boot client service, add the remote-scheduler-client dependency.

```
<dependency>
    <groupId>org.hammertech</groupId>
    <artifactId>remote-scheduler-client</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
  
Add the following to the application.yml.

```
spring:
  application:
    name: test-app
  rabbitmq:
      host: localhost
      port: 5672
      username: guest
      password: guest
```

Annotate the main class with @EnableRemoteScheduled and create a method annotated with the @RemoteScheduled annotation 
to run the test-job.  Start the app and see the test job writes a log to the console every minute.

```
@SpringBootApplication
@EnableRemoteScheduled
public class RemoteSchedulerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemoteSchedulerDemoApplication.class, args);
    }

    @RemoteScheduled(jobName = "test-job")
    public void doSomething() {
        System.out.println("ran test-Job");
    }
}
```

Look at remote-scheduler-server-sample module for an example.

## Production Deployment

In production, it is recommended to set up an external database for resiliency.  To do this, add the database dependency, 
define the datasource application properties, and configure Quartz in clustered mode.

```
pom.xml...
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>

application.yml...
    spring:
      datasource:
        url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/db_example
        username: springuser
        password: ThePassword
      quartz:
        job-store-type: jdbc
        jdbc:
          initialize-schema: never
        properties:
          org:
            quartz:
              jobStore:
                isClustered: "true"
              scheduler:
                instanceId: AUTO
              threadPool:
                threadCount: 100
                threadPriority: 8
```

## More Detail

Each client app creates a queue based on the naming convention: <prefix>.<app name>
By default the prefix = org.hammertech.remote-scheduler, but this can be changed by defining the following property 
in the Remote Scheduler and client services.  

```
org:
  hammertech:
    remote-scheduler:
      queue-name-prefix: org.hammertech.remote-scheduler
```

The min and max number of consumers to process job messages in the client service can be changed.

```
org:
  hammertech:
    remote-scheduler:
      concurrent-consumers: 5
      max-conccurent-consumers: 10
```

If multiple message brokers are defined in the client service, create the following bean 
to define which should be used

```
@Bean
RemoteScheduledConfigurer remoteScheduledConfigurer(ConnectionFactory connectionFactory) {
    return new RemoteScheduledConfigurer(connectionFactory);
}
```

## Future improvements

1) Right now, schedule runs are "send and forget" creating messages that expire after a configurable amount of 
time with the default being the next fire time.  To disallow concurrent runs of the same job, the client application can 
post to a queue every few seconds while running a job, so the remote scheduler can track the job is still running.  
Also send a completion message when done. 
2) Add test cases

## Authors

* **Brian Thalhamer**