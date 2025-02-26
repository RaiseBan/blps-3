package com.example.blps.repository.data;

import com.example.blps.model.dataEntity.TheirCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TheirCampaignRepository extends JpaRepository<TheirCampaign, Long> {
    List<TheirCampaign> findByStartDateAfter(LocalDate date);
    List<TheirCampaign> findByEndDateBefore(LocalDate date);
}