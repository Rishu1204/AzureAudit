package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureDnsZoneDto {
    private String zoneName;
    private String resourceGroup;
    private String region;
    private long numberOfRecordSets;
    private String nameServers;
    private long maxNumberOfRecordSets;
}
