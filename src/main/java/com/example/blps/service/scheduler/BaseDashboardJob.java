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
 * Базовый класс для заданий планировщика по генерации дашбордов
 */
@Slf4j
public abstract class BaseDashboardJob implements Job {
    
    protected abstract MessageSenderService getMessageSenderService();
    protected abstract DashboardType getDashboardType();
    protected abstract String getTitle();
    protected abstract String getDescription();
    protected abstract LocalDate getStartDate();
    protected abstract LocalDate getEndDate();
    protected abstract String getRecipientsGroup();
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing {} dashboard generation job", getDashboardType());
        
        try {
            DashboardGenerationRequest request = DashboardGenerationRequest.builder()
                    .type(getDashboardType())
                    .title(getTitle())
                    .description(getDescription())
                    .startDate(getStartDate())
                    .endDate(getEndDate())
                    .autoPublish(true)
                    .recipientsGroup(getRecipientsGroup())
                    .createdBy("system")
                    .build();
            
            getMessageSenderService().sendDashboardGenerationRequest(request);
            log.info("{} dashboard generation request sent successfully", getDashboardType());
            
        } catch (Exception e) {
            log.error("Error executing {} dashboard generation job", getDashboardType(), e);
            throw new JobExecutionException(e);
        }
    }
}