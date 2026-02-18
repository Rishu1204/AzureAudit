package com.example.azure.Azure.dto;

import com.azure.resourcemanager.sql.models.SqlDatabaseUsageMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AzureSqlServiceDto {

    private String databaseId;
    private String databaseName;
    private String region;
    private String resourceGroupName;
    private String sqlServerName;
    private String creationDate;
    private String defaultSecondaryLocation;
    private String edition;
    private String collation;
    private boolean isDefaultSecurityAlertPolicyEnabled;
    private String status;
    private String elasticPoolName;
    private List<SqlDatabaseUsageMetric> usageMetrics;
    private double databaseCost;
}
