package com.example.blps.service.data;

import com.example.blps.model.dataEntity.Metric;
import com.example.blps.repository.data.MetricRepository;
import org.springframework.stereotype.Service;

@Service
public class MetricService {
    private MetricRepository metricRepository;
    public MetricService(MetricRepository repository){
        this.metricRepository = repository;
    }

    public Metric saveMetric(Metric metric) {
        return metricRepository.save(metric);
    }
}