package com.example.blps.service.scheduler;

import com.example.blps.model.notification.DashboardType;
import com.example.blps.service.notification.MessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Задание для генерации ежемесячного дашборда
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyDashboardJob extends BaseDashboardJob {
    
    private final MessageSenderService messageSenderService;
    
    @Override
    protected MessageSenderService getMessageSenderService() {
        return messageSenderService;
    }
    
    @Override
    protected DashboardType getDashboardType() {
        return DashboardType.MONTHLY_REPORT;
    }
    
    @Override
    protected String getTitle() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        return "Ежемесячный отчет: " + previousMonth.getMonth() + " " + previousMonth.getYear();
    }
    
    @Override
    protected String getDescription() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        return "Автоматически сгенерированный отчет за " + previousMonth.getMonth() + " " + previousMonth.getYear();
    }
    
    @Override
    protected LocalDate getStartDate() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        return previousMonth.atDay(1);
    }
    
    @Override
    protected LocalDate getEndDate() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        return previousMonth.atEndOfMonth();
    }
    
    @Override
    protected String getRecipientsGroup() {
        return "ROLE_ANALYST,ROLE_ADMIN,ROLE_CAMPAIGN_MANAGER";
    }
}