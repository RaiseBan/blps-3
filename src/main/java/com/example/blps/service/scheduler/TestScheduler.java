package com.example.blps.service.scheduler;

import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.dto.notification.NotificationMessage;
import com.example.blps.model.notification.DashboardType;
import com.example.blps.model.notification.NotificationType;
import com.example.blps.service.notification.MessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Тестовый планировщик заданий, который запускает задания каждую минуту
 * для проверки асинхронной обработки.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TestScheduler {

    private final Scheduler scheduler;
    private final MessageSenderService messageSenderService;
    
    @Value("${test.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    /**
     * Инициализирует тестовые задания при запуске приложения
     */
    @PostConstruct
    public void initializeTestJobs() {
        if (!schedulerEnabled) {
            log.info("Тестовый планировщик отключен");
            return;
        }

        log.info("Инициализация тестовых заданий планировщика");
        
        try {
            // Задание для отправки тестового уведомления каждую минуту
            scheduleTestNotificationJob();
            
            // Задание для генерации тестового дашборда каждые 2 минуты
            scheduleTestDashboardJob();
            
            log.info("Тестовые задания планировщика инициализированы успешно");
            
        } catch (SchedulerException e) {
            log.error("Ошибка при инициализации тестовых заданий планировщика", e);
        }
    }

    /**
     * Планирует задание для отправки тестового уведомления каждую минуту
     */
    private void scheduleTestNotificationJob() throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(TestNotificationJob.class)
                .withIdentity("testNotificationJob", "testGroup")
                .storeDurably()
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("testNotificationTrigger", "testGroup")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(1)
                        .repeatForever())
                .startNow()
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("Задание отправки тестового уведомления запланировано с интервалом 1 минута");
    }

    /**
     * Планирует задание для генерации тестового дашборда каждые 2 минуты
     */
    private void scheduleTestDashboardJob() throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(TestDashboardJob.class)
                .withIdentity("testDashboardJob", "testGroup")
                .storeDurably()
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("testDashboardTrigger", "testGroup")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(2)
                        .repeatForever())
                .startNow()
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("Задание генерации тестового дашборда запланировано с интервалом 2 минуты");
    }

    /**
     * Задание для отправки тестового уведомления
     */
    @RequiredArgsConstructor
    @Slf4j
    public static class TestNotificationJob implements Job {
        
        private final MessageSenderService messageSenderService;
        
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                log.info("Выполнение задания отправки тестового уведомления");
                
                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                
                NotificationMessage notification = NotificationMessage.builder()
                        .title("Тестовое уведомление (" + currentTime + ")")
                        .message("Это тестовое уведомление отправлено автоматически планировщиком задач " +
                                "для проверки системы асинхронной обработки уведомлений. " +
                                "Время отправки: " + currentTime)
                        .type(NotificationType.SYSTEM_ALERT)
                        .recipient("ROLE_ADMIN,ROLE_ANALYST")
                        .build();
                
                messageSenderService.sendNotification(notification);
                
                log.info("Тестовое уведомление успешно отправлено в очередь");
                
            } catch (Exception e) {
                log.error("Ошибка при выполнении задания отправки тестового уведомления", e);
                throw new JobExecutionException(e);
            }
        }
    }

    /**
     * Задание для генерации тестового дашборда
     */
    @RequiredArgsConstructor
    @Slf4j
    public static class TestDashboardJob implements Job {
        
        private final MessageSenderService messageSenderService;
        
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                log.info("Выполнение задания генерации тестового дашборда");
                
                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(7);
                
                DashboardGenerationRequest request = DashboardGenerationRequest.builder()
                        .type(DashboardType.WEEKLY_SUMMARY)
                        .title("Тестовый дашборд (" + currentTime + ")")
                        .description("Тестовый дашборд, созданный автоматически планировщиком задач " +
                                "для проверки системы асинхронной обработки. " +
                                "Время создания: " + currentTime)
                        .startDate(startDate)
                        .endDate(endDate)
                        .autoPublish(true)
                        .recipientsGroup("ROLE_ADMIN,ROLE_ANALYST")
                        .createdBy("system-scheduler")
                        .build();
                
                messageSenderService.sendDashboardGenerationRequest(request);
                
                log.info("Запрос на генерацию тестового дашборда успешно отправлен в очередь");
                
            } catch (Exception e) {
                log.error("Ошибка при выполнении задания генерации тестового дашборда", e);
                throw new JobExecutionException(e);
            }
        }
    }
}