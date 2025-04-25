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
import java.time.YearMonth;

/**
 * Задание для генерации ежемесячного дашборда
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyDashboardJob implements Job {
    
    private final MessageSenderService messageSenderService;
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing monthly dashboard generation job");
        
        try {
            YearMonth previousMonth = YearMonth.now().minusMonths(1);
            LocalDate startDate = previousMonth.atDay(1);
            LocalDate endDate = previousMonth.atEndOfMonth();
            
            DashboardGenerationRequest request = DashboardGenerationRequest.builder()
                    .type(DashboardType.MONTHLY_REPORT)
                    .title("Ежемесячный отчет: " + previousMonth.getMonth() + " " + previousMonth.getYear())
                    .description("Автоматически сгенерированный отчет за " + previousMonth.getMonth() + " " + previousMonth.getYear())
                    .startDate(startDate)
                    .endDate(endDate)
                    .autoPublish(true)
                    .recipientsGroup("ROLE_ANALYST,ROLE_ADMIN,ROLE_CAMPAIGN_MANAGER")
                    .createdBy("system")
                    .build();
            
            messageSenderService.sendDashboardGenerationRequest(request);
            log.info("Monthly dashboard generation request sent successfully");
            
        } catch (Exception e) {
            log.error("Error executing monthly dashboard generation job", e);
            throw new JobExecutionException(e);
        }
    }
}