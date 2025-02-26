package com.example.blps.repository.data;

import com.example.blps.model.dataEntity.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MetricRepository extends JpaRepository<Metric, Long> {
    List<Metric> findByCampaignId(Long campaignId);
    List<Metric> findByClickCountGreaterThan(int minClicks);
}