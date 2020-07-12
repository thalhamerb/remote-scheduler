package org.hammertech.remotescheduler.manage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/jobs")
@Slf4j
@RequiredArgsConstructor
class ScheduleManagementController {

    private final ScheduleManagementService scheduleManagementService;

    @GetMapping("/{appName}")
    Set<ScheduleDetail> getAppJobs(@PathVariable String appName) throws SchedulerException {
        return scheduleManagementService.getAppJobs(appName);
    }

    @PostMapping
    void createJob(@Valid @RequestBody NewSchedule newSchedule) throws SchedulerException {
        scheduleManagementService.createJob(newSchedule);
    }

    @PutMapping("/{appName}/{jobName}/delete")
    void deleteJob(@PathVariable String appName, @PathVariable String jobName) throws SchedulerException {
        scheduleManagementService.deleteJob(appName, jobName);
    }

    @PutMapping("/{appName}/{jobName}/updateCron/{cron}")
    void updateCron(String appName, String jobName, String cron) throws SchedulerException {
        scheduleManagementService.updateCron(appName, jobName, cron);
    }

    @PutMapping("/{appName}/{jobName}/trigger")
    void triggerJob(String appName, String jobName) throws SchedulerException {
        scheduleManagementService.triggerJob(appName, jobName);
    }

    @PutMapping("/{appName}/{jobName}/pause")
    void pauseJob(String appName, String jobName) throws SchedulerException {
        scheduleManagementService.pauseJob(appName, jobName);
    }

    @PutMapping("/{appName}/{jobName}/resume")
    void resumeJob(String appName, String jobName) throws SchedulerException {
        scheduleManagementService.resumeJob(appName, jobName);
    }

    @ExceptionHandler
    ResponseEntity handleSchedulerException(SchedulerException ex) {
        log.error("Unable to process job request", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

}
