package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AzureLogAnalyticsWorkspaceDto {
    private String name;
    private String id;
    private String resourceGroup;
    private String region;
    private String sku;
    private Integer retentionInDays;
    private String publicNetworkAccessForIngestion;
    private String publicNetworkAccessForQuery;
}
