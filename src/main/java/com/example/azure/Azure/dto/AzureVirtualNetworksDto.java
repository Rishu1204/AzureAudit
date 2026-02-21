package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class AzureVirtualNetworksDto {

    private String networkName;
    private String resourceGroupName;
    private String region;
    private String addressSpace;
    private String subnets;
    private String regionalCommunity;
    private boolean enableDdosProtection;
    private String ddosProtectionPlanId;
    private String dhcpOptions;
    private List<String> dnsServerIps;
    private boolean enableVmProtection;
    private String eTag;
    private String resourceGuid;
    private List<String> costOptimization;

}
