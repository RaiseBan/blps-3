package com.example.blps.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@Slf4j
public abstract class BaseQuartzJob implements Job {

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("Executing {} job", this.getClass().getSimpleName());
            executeInternal(context);
        } catch (Exception e) {
            log.error("Error executing job", e);
            throw new JobExecutionException(e);
        }
    }

    protected abstract void executeInternal(JobExecutionContext context) throws Exception;
}