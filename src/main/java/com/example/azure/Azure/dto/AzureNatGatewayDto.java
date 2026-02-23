package com.example.azure.Azure.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AzureNatGatewayDto {
    private String subscriptionId;
    private String resourceGroup;
    private String region;
    private String natName;
    private Integer idleTimeoutInMinutes;
    private Integer publicIpCount;
    private Integer subnetCount;
    private String publicIpIds;
    private String subnetIds;
    private double totalDataTransferredGb;
    private double noOfPacketsTransferred;
    private double totalConnectionCount;
    private Double natGatewayCost;
    private String costOptimization;
}