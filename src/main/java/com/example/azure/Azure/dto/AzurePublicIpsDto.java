package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class AzurePublicIpsDto {
    private String name;
    private String resourceGroup;
    private String region;
    private String ipAddress;
    private String allocationMethod;
    private Integer idleTimeoutInMinutes;
    private String publicIpAddressVersion;
    private String publicIpAddressType;
    private double publicIpCost;
}
