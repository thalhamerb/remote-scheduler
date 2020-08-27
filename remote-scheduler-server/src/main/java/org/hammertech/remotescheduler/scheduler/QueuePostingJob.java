package org.hammertech.remotescheduler.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hammertech.remotescheduler.manage.DataMapType;
import org.hammertech.remotescheduler.manage.ExpireStrategy;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.impl.JobDetailImpl;

import java.time.Instant;

@DisallowConcurrentExecution
@Slf4j
@RequiredArgsConstructor
public class QueuePostingJob implements Job {

    private final MessageSendService messageSendService;

    @Override
    public void execute(JobExecutionContext context) {
        JobDetailImpl jobDetailImpl = (JobDetailImpl) context.getJobDetail();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String expireStrategyString = dataMap.getString(DataMapType.EXPIRE_STRATEGY.toString());
        ExpireStrategy expireStrategy = ExpireStrategy.valueOf(expireStrategyString);
        Long expireTimeMinutes = dataMap.getLong(DataMapType.EXP_TIME_MINUTES.toString());
        Long expireEpochTime = getExpireEpochTime(context, expireStrategy, expireTimeMinutes);
        log.info(jobDetailImpl.getGroup() + " " + jobDetailImpl.getName() + " triggered: " + Instant.now());
        messageSendService.sendSchedulerMessage(jobDetailImpl.getGroup(), jobDetailImpl.getName(), expireEpochTime);
    }

    private Long getExpireEpochTime(JobExecutionContext context, ExpireStrategy expireStrategy, Long expireTimeMinutes) {
        Long expireEpochTime = null;
        switch (expireStrategy) {
            case ON_NEXT_SCHEDULE:
                if (context.getNextFireTime() != null) {
                    expireEpochTime = context.getNextFireTime().getTime();
                }
                break;
            case CUSTOM:
                expireEpochTime = System.currentTimeMillis() + (expireTimeMinutes * 60000);
                break;
        }
        return expireEpochTime;
    }
}
