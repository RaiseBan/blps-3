package com.example.blps.config;

import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import java.util.Properties;

@Configuration
@EnableScheduling
public class QuartzConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public JobFactory jobFactory() {
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory() {
            @Override
            protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
                Object job = super.createJobInstance(bundle);
                applicationContext.getAutowireCapableBeanFactory().autowireBean(job);
                return job;
            }
        };
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setJobFactory(jobFactory());
        schedulerFactory.setAutoStartup(true);

        Properties quartzProperties = new Properties();
        quartzProperties.put("org.quartz.scheduler.instanceName", "blps-scheduler");
        quartzProperties.put("org.quartz.scheduler.instanceId", "AUTO");
        quartzProperties.put("org.quartz.threadPool.threadCount", "5");
        quartzProperties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");

        schedulerFactory.setQuartzProperties(quartzProperties);
        schedulerFactory.setWaitForJobsToCompleteOnShutdown(true);
        schedulerFactory.setOverwriteExistingJobs(true);

        return schedulerFactory;
    }
}