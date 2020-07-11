# Remote Scheduler

Centralized scheduler that manages cron jobs across multiple services.  Inspired by Spring Scheduling and backed by 
Quartz scheduler, this service is useful for a microservices architecture where schedules 
are managed across many services.  

## Getting Started

The Remote Scheduler service requires Quartz and the datasource to be defined.  By default 
an in-memory datasource is used with base quartz configuration.  For resiliency in production, 
it is recommended to have an external database and Quartz running in cluster mode.  

The Remote Scheduler initiates jobs on client services by posting messages to an AMPQ message broker 
that is consumed by the client services.  The method annotated with the job name is run.


### Prerequisites

Download a standalone AMQP message broker (ex. RabbitMQ or Apache Kafka).  Using Docker Hub is recommended (https://hub.docker.com/_/rabbitmq)


### Installing

Setting up Remote Scheduler with an ephemeral in-memory database

Create application.yml file and define AMQP message broker properties.

```
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

Start the application and create an example schedule for a client app that will be called "test-app".

```
POST http://localhost:8096/jobs

    {
      "appName": "test-app",
      "jobName": "test-job",
      "cron": "0 0/1 * 1/1 * ? *"
    }
```

Create a new Spring Boot client service to use this new schedule. Add the remote-scheduler-client dependency.  
Define application.yml with application name and amqp properties similar to above.  In the 
main class add the @EnableRemoteScheduled tag and create an annotated method to run when the message for that 
job is consumed.

```
pom.xml...
    <dependency>
        <groupId>org.hammertech</groupId>
        <artifactId>remote-scheduler-client</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>

application.yml...
    spring:
      application:
        name: test-app
      rabbitmq:
          host: localhost
          port: 5672
          username: guest
          password: guest

Main class...

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

You should see that every minute the test job writes a log to the console.  Put your job logic in 
this method.

## Production Deployment

In production it is recommended to set up an external database.  To do this, add the 
appropriate database dependency, define the datasoure applicaion properties, and configure Quartz in
clustered mode

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

The queue name created on the AMQP message broker is a combination of a <prefix>.<job name>
By default the prefix = org.hammertech, but can be changed by defining the following property 
in the Remote Scheduler service and the client service.  Also the min and max number of consumers
to process job messages can be overriden.  

```
org:
  hammertech:
    remote-scheduler:
      queue-name-prefix: org.hammertech
```

Also the min and max number of consumers to process job messages in the client service 
can be overriden.  Below are the defaults  

```
org:
  hammertech:
    remote-scheduler:
      concurrent-consumers: 5
      max-conccurent-consumers: 10
```

## Built With

* [Quartz Scheduler](http://www.quartz-scheduler.org/) - The underlying scheduler
* [Maven](https://maven.apache.org/) - Dependency Management

## Future improvements

1) Make Remote Scheduler a dependency that can be added to a new project rather than 
having to update the project directly
2) Make so client application posts to a queue every 15 seconds while running a job 
so the remote scheduler can track the job is still running.  Also send a completion message 
when the job is completed.   If the remote scheduler stops getting ping messages without 
getting a completion message, we know the job was unable to complete.  
Being aware of these failures would allow re-running of the job a configurable number of times.
Also this will remove the need for expire strategy and expire time defined in application.
3) Add test cases

## Authors

* **Brian Thalhamer** - *Initial work*