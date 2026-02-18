package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureLoadBalancerDto {
    private String name;
    private String resourceGroup;
    private String region;
    private String sku;
    private int frontendIpCount;
    private int backendPoolCount;
    private int ruleCount;
    private int probeCount;
}
