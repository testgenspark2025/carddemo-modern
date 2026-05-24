package com.carddemo.batch.interest;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class InterestCalcJobConfig {

    public static final String JOB_NAME = "interestCalcJob";
    public static final String STEP_NAME = "interestCalcStep";

    @Bean(name = JOB_NAME)
    public Job interestCalcJob(JobRepository jobRepository, Step interestCalcStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(interestCalcStep)
                .build();
    }

    @Bean
    public Step interestCalcStep(JobRepository jobRepository,
                                 PlatformTransactionManager tx,
                                 InterestCalcTasklet tasklet) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .tasklet(tasklet, tx)
                .build();
    }
}
