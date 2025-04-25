package com.example.blps.repository.notification;

import com.example.blps.model.notification.Dashboard;
import com.example.blps.model.notification.DashboardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
    
    List<Dashboard> findByTypeOrderByCreatedAtDesc(DashboardType type);
    
    List<Dashboard> findByIsPublishedTrueOrderByCreatedAtDesc();
    
    List<Dashboard> findByIsPublishedFalseOrderByCreatedAtAsc();
    
    @Query("SELECT d FROM Dashboard d WHERE d.createdAt > :since ORDER BY d.createdAt DESC")
    List<Dashboard> findRecentDashboards(@Param("since") LocalDateTime since);
    
    @Query("SELECT d FROM Dashboard d WHERE d.type = :type AND d.isPublished = true ORDER BY d.createdAt DESC LIMIT 1")
    Dashboard findLatestPublishedByType(@Param("type") DashboardType type);
}