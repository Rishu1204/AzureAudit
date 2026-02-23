package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureSnapshotDto {
    private String subscriptionId;
    private String snapshotName;
    private String resourceGroup;
    private String region;
    private Integer diskSizeGb;
    private String sku;
    private String osType;
    private String creationData;
    private String timeCreated;
    private String sourceResourceId;
    private String provisioningState;
    private Double cost;
    private String costOptimization;
}
