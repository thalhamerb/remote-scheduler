# Remote Scheduler

This is a centralized scheduler that manages cron jobs across multiple services.  Inspired by Spring Scheduling and backed by 
Quartz scheduler, this service is targeted for a message-driven microservices architecture where schedules 
are managed across many services.  This is a proof of concept, and the implementation needs to be flushed out and refined.

## Getting Started

The Remote Scheduler service requires a datasource and an AMQP message broker.  By default, 
an in-memory datasource is used but can be configured to use an external database (example below).  

The Remote Scheduler initiates jobs on client services by posting messages to an AMQP message broker 
that is consumed by the client services.  The method annotated with the job name is ran.  


### Prerequisites

Download a standalone AMQP message broker (ex. RabbitMQ or Apache Kafka).  It is recommended to use Docker Hub 
for a quick setup (https://hub.docker.com/_/rabbitmq)


### Setting up Remote Scheduler Server

Create a new project and add the remote scheduler server dependency to the pom

```

<dependency>
    <groupId>org.hammertech</groupId>
    <artifactId>remote-scheduler-server</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Annotate main class with @EnableRemoteScheduledServer to enable functionality

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

Start the application and create an example schedule to be used by a client app.  We will use "test-app" as an example.  

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

Annotate the main class with @EnableRemoteScheduled to enable the client app to listen for job runs against the queue.  
Annotate methods that contain a schedule's logic with @RemoteScheduled.  Below is an example
to run the test-job.  If following the example, start the app and test job should write a log to the console every minute.

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

Look at remote-scheduler-client-sample module for an example.

## Production Deployment

In production, it is recommended to set up an external database for resiliency.  To do this, add the database dependency, 
define the datasource application properties, and configure Quartz in cluster mode.  It is left up to the user to configure 
quartz, but below shows a typical configuration.  Go to the [Quartz Scheduler website](http://www.quartz-scheduler.org/overview/)
 for more information.

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

If multiple amqp message brokers are defined in the client service, create the following bean 
to define which should be used for this scheduler

```
@Bean
RemoteScheduledConfigurer remoteScheduledConfigurer(ConnectionFactory connectionFactory) {
    return new RemoteScheduledConfigurer(connectionFactory);
}
```

When a schedule is scheduled to run, the Remote Scheduler Server posts a message to the app's queue.  Typically the
job is ran almost immediately, but if the client app is currently down or failing the message can get stale.  Ex, if 
a schedule runs every 5 minutes and the app is down for 6 minutes, the job could ran twice when the server 
is brought back up.  By default, this won't happen because the message expire on the next job run.  If desired, this can be customized 
so the schedule runs never expire or expire after a custom amount of time.  Below are example payloads for each when creating
the schedule.

```
no expiration...
{
  "appName": "test-app",
  "jobName": "test-job",
  "cron": "0 0/1 * 1/1 * ? *",
  "expireStrategy": NONE
}

custom expiration time of 5 minutes
{
  "appName": "test-app",
  "jobName": "test-job",
  "cron": "0 0/1 * 1/1 * ? *",
  "expireStrategy": CUSTOM,
  "minutesToExpire": 5
}

```
## Future improvements

1) Right now, schedule runs are "send and forget" creating messages that expire after a configurable amount of 
time with the default being the next schedule fire time.  Sometimes users do not want a schedule to be able to run
more than once at a time.  To accomplish this the client application can post to a queue every few seconds 
while running a job, so the remote scheduler can track the job is still running.  Then it can send a completion message 
when done.  This tracking would allow for a couple improvements.  Schedule expire on message can be removed because can 
be tracked on remote scheduler server side to send the next schedule message or not.  Also this would give the ability 
for the remote scheduler server to track stats about job runtimes, completions, etc.  
2) Change references to "job" to "schedule" so there isn't consistent terminology.
3) Add test cases
4) Replace Quartz Scheduler implementation in Remote Scheduler Server to use a custom implementation, since Quartz
was originally meant to be embedded in the client application rather than trigger schedules remotely.
5) Maybe create a command line client as a wrapper around controller methods, since interface with remote scheduler 
server apip and security around endpoints is currently left to the user.

## Author

* **Brian Thalhamer**