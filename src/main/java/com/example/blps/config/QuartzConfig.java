package com.example.blps.config;

import org.quartz.spi.JobFactory;
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

    /**
     * JobFactory для создания бинов с внедрением зависимостей через Spring
     */
    @Bean
    public JobFactory jobFactory() {
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    /**
     * Настройка планировщика Quartz с использованием хранения в памяти
     * вместо базы данных для упрощения настройки
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setJobFactory(jobFactory());

        // Запускаем планировщик автоматически
        schedulerFactory.setAutoStartup(true);

        // Дополнительные свойства Quartz для хранения в памяти
        Properties quartzProperties = new Properties();
        quartzProperties.put("org.quartz.scheduler.instanceName", "blps-notification-scheduler");
        quartzProperties.put("org.quartz.scheduler.instanceId", "AUTO");
        quartzProperties.put("org.quartz.threadPool.threadCount", "5");

        // Используем хранилище в памяти вместо JDBC
        quartzProperties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");

        schedulerFactory.setQuartzProperties(quartzProperties);

        return schedulerFactory;
    }
}