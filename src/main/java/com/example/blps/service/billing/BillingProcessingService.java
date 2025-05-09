package com.example.blps.service.billing;

import com.example.blps.dto.billing.BillingRequest;
import com.example.blps.model.billing.BillingData;
import com.example.blps.service.integration.Bitrix24Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingProcessingService {
    
    private final BillingCalculationService billingCalculationService;
    private final Bitrix24Service bitrix24Service;
    
    public static final String BILLING_PROCESSING_QUEUE = "billing.processing.queue";
    
    @JmsListener(destination = BILLING_PROCESSING_QUEUE)
    public void processBillingRequest(BillingRequest request) {
        log.info("Processing billing request for campaign: {}", request.getCampaignId());
        
        try {
            
            BillingData billingData = billingCalculationService.calculateBilling(request);
            
            sendToBitrix24(billingData);
            
            log.info("Billing processed successfully for campaign: {}", request.getCampaignId());
        } catch (Exception e) {
            log.error("Error processing billing request", e);
        }
    }
    
    private void sendToBitrix24(BillingData billingData) {
        try {
            String invoiceData = createBitrix24Invoice(billingData);
            
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("entityTypeId", "31");
            params.put("fields[TITLE]", "Счет: " + billingData.getCampaignName());
            params.put("fields[OPPORTUNITY]", billingData.getTotalSpent().toString());
            params.put("fields[CURRENCY_ID]", "RUB");
            params.put("fields[STAGE_ID]", "NEW");
            params.put("fields[COMMENTS]", invoiceData);

            log.info("ОТПРАВКА СУКИИИИИИИИИИИИИИИИИИИ");
            
            String result = bitrix24Service.connector.executeMethod("crm.item.add", params);
            log.info("Invoice created in Bitrix24: {}", result);
            
        } catch (Exception e) {
            log.error("Error sending invoice to Bitrix24", e);
            throw new RuntimeException("Failed to create invoice in Bitrix24", e);
        }
    }
    
    private String createBitrix24Invoice(BillingData billingData) {
        StringBuilder invoice = new StringBuilder();
        
        invoice.append("СЧЕТ НА ОПЛАТУ\n");
        invoice.append("================\n\n");
        invoice.append("Кампания: ").append(billingData.getCampaignName()).append("\n");
        invoice.append("Период: ").append(billingData.getPeriodStart())
               .append(" - ").append(billingData.getPeriodEnd()).append("\n");
        invoice.append("Дата генерации: ").append(billingData.getGeneratedAt()).append("\n\n");
        
        invoice.append("ДЕТАЛИЗАЦИЯ:\n");
        invoice.append("------------\n");
        
        billingData.getItems().forEach(item -> {
            invoice.append(String.format("- %s: %d x %.2f = %.2f руб.\n",
                    item.getDescription(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()));
        });
        
        invoice.append("\n------------\n");
        invoice.append("ИТОГО: ").append(billingData.getTotalSpent()).append(" руб.\n");
        
        invoice.append("\nСТАТИСТИКА:\n");
        invoice.append("- Всего кликов: ").append(billingData.getTotalClicks()).append("\n");
        invoice.append("- Всего конверсий: ").append(billingData.getTotalConversions()).append("\n");
        
        return invoice.toString();
    }
}