package com.carddemo.admin;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job interestCalcJob;

    public BatchController(JobLauncher jobLauncher,
                           @Qualifier("interestCalcJob") Job interestCalcJob) {
        this.jobLauncher = jobLauncher;
        this.interestCalcJob = interestCalcJob;
    }

    @PostMapping("/interest-calc")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> runInterestCalc(
            @RequestParam(required = false) String date) throws Exception {
        String runDate = (date == null || date.isBlank())
                ? LocalDate.now().toString() : date;
        // Validates yyyy-MM-dd format and unique key per run.
        JobParameters params = new JobParametersBuilder()
                .addString("date", runDate)
                .addLong("runStamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution exec = jobLauncher.run(interestCalcJob, params);
        return ResponseEntity.ok(Map.of(
                "jobId", exec.getJobId(),
                "executionId", exec.getId(),
                "status", exec.getStatus().toString(),
                "readCount", exec.getStepExecutions().stream()
                        .mapToLong(s -> s.getReadCount()).sum(),
                "writeCount", exec.getStepExecutions().stream()
                        .mapToLong(s -> s.getWriteCount()).sum(),
                "exitCode", exec.getExitStatus().getExitCode()));
    }
}
