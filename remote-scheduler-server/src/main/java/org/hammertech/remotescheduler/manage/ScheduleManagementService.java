package org.hammertech.remotescheduler.manage;

import lombok.RequiredArgsConstructor;
import org.hammertech.remotescheduler.scheduler.QueuePostingJob;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class ScheduleManagementService {

    private final Scheduler scheduler;

    Set<ScheduleDetail> getAppJobs(String appName) throws SchedulerException {
        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(appName));
        return jobKeys.stream().map(jobKey -> {
            try {
                return scheduler.getJobDetail(jobKey);
            } catch (SchedulerException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).map(jobDetail -> {
          try {
              ScheduleDetail scheduleDetail = new ScheduleDetail();
              scheduleDetail.setAppName(jobDetail.getKey().getGroup());
              scheduleDetail.setJobName(jobDetail.getKey().getName());
              scheduleDetail.setDescription(jobDetail.getDescription());
              String expireStrategy = jobDetail.getJobDataMap().getString(DataMapType.EXPIRE_STRATEGY.toString());
              scheduleDetail.setExpireStrategy(ExpireStrategy.valueOf(expireStrategy));
              long expTime = jobDetail.getJobDataMap().getLong(DataMapType.EXP_TIME_MINUTES.toString());
              scheduleDetail.setSecondsToExpire(expTime != -1 ? expTime : null);
              TriggerKey triggerKey = getTriggerKey(jobDetail.getKey());
              CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
              scheduleDetail.setCron(trigger.getCronExpression());
              scheduleDetail.setTriggerState(scheduler.getTriggerState(triggerKey).toString());
              return scheduleDetail;
          } catch (SchedulerException e) {
              e.printStackTrace();
              return null;
          }
        }).collect(Collectors.toSet());
    }

    void createJob(NewSchedule newSchedule) throws SchedulerException {
        if (newSchedule.getExpireStrategy() == null) {
            newSchedule.setExpireStrategy(ExpireStrategy.NONE);
        }
        if (!ExpireStrategy.CUSTOM.equals(newSchedule.getExpireStrategy())) {
            newSchedule.setMinutesToExpire(-1L);
        }

        JobDetail jobDetail = JobBuilder.newJob().ofType(QueuePostingJob.class)
                .storeDurably()
                .withIdentity(newSchedule.getJobName(), newSchedule.getAppName())
                .usingJobData(DataMapType.EXPIRE_STRATEGY.toString(), newSchedule.getExpireStrategy().toString())
                .usingJobData(DataMapType.EXP_TIME_MINUTES.toString(), newSchedule.getMinutesToExpire())
                .withDescription(newSchedule.getDescription())
                .build();

        Trigger trigger = createTrigger(newSchedule.getAppName(), newSchedule.getJobName(), newSchedule.getCron());
        scheduler.scheduleJob(jobDetail, trigger);
    }

    private Trigger createTrigger(String appName, String jobName, String cron) {
        JobKey jobKey = getJobKey(appName, jobName);
        return TriggerBuilder.newTrigger().forJob(jobKey)
                .withIdentity(getTriggerKey(jobKey))
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
    }

    void deleteJob(String appName, String jobName) throws SchedulerException {
        JobKey jobKey = getJobKey(appName, jobName);
        scheduler.deleteJob(jobKey);
    }

    void updateCron(String appName, String jobName, String cron) throws SchedulerException {
        Trigger newTrigger = createTrigger(appName, jobName, cron);
        JobKey jobKey = getJobKey(appName, jobName);
        List<? extends Trigger> triggerList = scheduler.getTriggersOfJob(jobKey);
        if (!CollectionUtils.isEmpty(triggerList) && triggerList.size() == 1) {
            TriggerKey existingTriggerKey = triggerList.get(0).getKey();
            scheduler.rescheduleJob(existingTriggerKey, newTrigger);
        } else {
            String message = String.format("Unable to update Trigger for %s-%s because current job trigger is invalid.", appName, jobName);
            throw new IllegalArgumentException(message);
        }
    }

    void triggerJob(String appName, String jobName) throws SchedulerException {
        JobKey jobKey = getJobKey(appName, jobName);
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        scheduler.triggerJob(jobKey, jobDetail.getJobDataMap());
    }

    void pauseJob(String appName, String jobName) throws SchedulerException {
        JobKey jobKey = getJobKey(appName, jobName);
        scheduler.pauseJob(jobKey);
    }

    void resumeJob(String appName, String jobName) throws SchedulerException {
        JobKey jobKey = getJobKey(appName, jobName);
        scheduler.resumeJob(jobKey);
    }

    private TriggerKey getTriggerKey(JobKey jobKey) {
        return new TriggerKey(jobKey.getName(), jobKey.getGroup());
    }

    private JobKey getJobKey(String appName, String jobName) {
        return new JobKey(jobName, appName);
    }
}
