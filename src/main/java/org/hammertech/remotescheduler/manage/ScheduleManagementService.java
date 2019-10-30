package org.hammertech.remotescheduler.manage;

import org.hammertech.remotescheduler.messaging.QueuePostingJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class ScheduleManagementService {

    @Autowired
    private Scheduler scheduler;

    public void createJob(NewSchedule newSchedule) throws SchedulerException {
        if (!ExpireStrategy.CUSTOM.equals(newSchedule.getExpireStrategy())) {
            newSchedule.setSecondsToExpire(-1L);
        }

        JobDetail jobDetail = JobBuilder.newJob().ofType(QueuePostingJob.class)
                .storeDurably()
                .withIdentity(newSchedule.getJobName(), newSchedule.getAppName())
                .usingJobData(DataMapType.EXPIRE_STATEGY.toString(), newSchedule.getExpireStrategy().toString())
                .usingJobData(DataMapType.EXP_TIME.toString(), newSchedule.getSecondsToExpire())
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

    public void deleteJob(String appName, String jobName) throws SchedulerException {
        JobKey jobKey = getJobKey(appName, jobName);
        scheduler.deleteJob(jobKey);
    }

    public void updateCron(String appName, String jobName, String cron) throws SchedulerException {
        Trigger newTrigger = createTrigger(appName, jobName, cron);
        JobKey jobKey = getJobKey(appName, jobName);
        List<Trigger> triggerList = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
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
