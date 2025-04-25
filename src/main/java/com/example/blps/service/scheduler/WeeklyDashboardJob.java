package com.example.blps.service.scheduler;

import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.model.notification.DashboardType;
import com.example.blps.service.notification.MessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Задание для генерации еженедельного дашборда
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyDashboardJob implements Job {
    
    private final MessageSenderService messageSenderService;
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing weekly dashboard generation job");
        
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(7);
            
            DashboardGenerationRequest request = DashboardGenerationRequest.builder()
                    .type(DashboardType.WEEKLY_SUMMARY)
                    .title("Еженедельный отчет: " + startDate + " - " + endDate)
                    .description("Автоматически сгенерированный отчет за период " + startDate + " - " + endDate)
                    .startDate(startDate)
                    .endDate(endDate)
                    .autoPublish(true)
                    .recipientsGroup("ROLE_ANALYST,ROLE_ADMIN")
                    .createdBy("system")
                    .build();
            
            messageSenderService.sendDashboardGenerationRequest(request);
            log.info("Weekly dashboard generation request sent successfully");
            
        } catch (Exception e) {
            log.error("Error executing weekly dashboard generation job", e);
            throw new JobExecutionException(e);
        }
    }
}