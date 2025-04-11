package com.example.blps.dto.data;

import com.example.blps.model.dataEntity.CampaignStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SetStatusRequest {
    private List<Long> campaignIds;
    private CampaignStatus status;
}