package com.example.blps.service.scheduler;

import com.example.blps.model.notification.DashboardType;
import com.example.blps.service.notification.MessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Задание для генерации еженедельного дашборда
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyDashboardJob extends BaseDashboardJob {
    
    private final MessageSenderService messageSenderService;
    
    @Override
    protected MessageSenderService getMessageSenderService() {
        return messageSenderService;
    }
    
    @Override
    protected DashboardType getDashboardType() {
        return DashboardType.WEEKLY_SUMMARY;
    }
    
    @Override
    protected String getTitle() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        return "Еженедельный отчет: " + startDate + " - " + endDate;
    }
    
    @Override
    protected String getDescription() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        return "Автоматически сгенерированный еженедельный отчет за период " + startDate + " - " + endDate;
    }
    
    @Override
    protected LocalDate getStartDate() {
        return LocalDate.now().minusDays(7);
    }
    
    @Override
    protected LocalDate getEndDate() {
        return LocalDate.now();
    }
    
    @Override
    protected String getRecipientsGroup() {
        return "ROLE_ANALYST,ROLE_ADMIN";
    }
}
