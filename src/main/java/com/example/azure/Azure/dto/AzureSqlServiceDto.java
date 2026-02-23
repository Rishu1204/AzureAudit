package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AzureSqlServiceDto {

    private String subscriptionId;
    private String resourceGroupName;
    private String region;
    private String databaseId;
    private String databaseName;
    private String sqlServerName;
    private String creationDate;
    private String sku;
    private String defaultSecondaryLocation;
    private String edition;
    private String collation;
    private boolean isDefaultSecurityAlertPolicyEnabled;
    private String status;
    private String elasticPoolName;
    // Metrics fields
    private double avgCpuPercent;
    private double avgStoragePercent;
    private double totalConnectionsSuccessful;

    private double databaseCost;
    private String costOptimization;
}
