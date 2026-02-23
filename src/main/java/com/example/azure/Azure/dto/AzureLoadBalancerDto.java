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
    private String subscriptionId;
    private String resourceGroup;
    private String region;
    private String loadBalancerName;
    private String sku;
    private int frontendIpCount;
    private String frontEndIds;
    private String privateIpAddresses;
    private int backendPoolCount;
    private String backendIds;
    private int ruleCount;
    private int probeCount;
    private double cost;
    private String costOptimization;
}
