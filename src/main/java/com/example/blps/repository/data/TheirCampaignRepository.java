package com.example.blps.repository.data;

import com.example.blps.model.dataEntity.CampaignStatus;
import com.example.blps.model.dataEntity.TheirCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TheirCampaignRepository extends JpaRepository<TheirCampaign, Long> {
    List<TheirCampaign> findByStartDateAfter(LocalDate date);
    List<TheirCampaign> findByEndDateBefore(LocalDate date);

    @Modifying
    @Query("UPDATE TheirCampaign c SET c.status = :status WHERE c.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") CampaignStatus status);

}