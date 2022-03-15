package com.db.flare.flareifti.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.support.ExitCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class JobRunningService {

    private final JobRegistry jobRegistry;

    private final JobLauncher jobLauncher;

    private final ExitCodeMapper exitCodeMapper;

    @Nullable
    private JobExecution jobExecution;


    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
        postProcessor.setJobRegistry(jobRegistry);
        return postProcessor;
    }

    @Autowired
    public JobRunningService(JobRegistry jobRegistry, JobLauncher jobLauncher, ExitCodeMapper exitCodeMapper) {
        this.jobRegistry = jobRegistry;
        this.jobLauncher = jobLauncher;
        this.exitCodeMapper = exitCodeMapper;
    }

    public void runJob(String jobName, JobParameters jobParameters) {
        Job job;
        try {
            job = jobRegistry.getJob(jobName);
        } catch (NoSuchJobException e) {
            log.error("Problem occurred when trying to run job", e);
            jobExecution = null;
            return;
        }
        runJob(job, jobParameters);
    }

    public void runJob(Job job, JobParameters jobParameters) {
        try {
            jobExecution = jobLauncher.run(job, jobParameters);
        } catch (JobExecutionException e) {
            log.error("Problem occurred when trying to run job", e);
            jobExecution = null;
        }
    }

    public Optional<JobExecution> lastExecutedJob() {
        return Optional.ofNullable(jobExecution);
    }

    public long lastExecutionId() {
        return lastExecutedJob()
                .map(JobExecution::getId)
                .orElseThrow(() -> new IllegalStateException("No job execution available"));
    }

    public int lastExitCode() {
        return lastExecutedJob()
                .map(JobExecution::getExitStatus)
                .map(ExitStatus::getExitCode)
                .map(exitCodeMapper::intValue)
                .orElse(1);
    }
}
