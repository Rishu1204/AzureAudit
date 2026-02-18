package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureKubernetesDto {

    private String clusterName;
    private String resourceGroup;
    private String region;
    private String kubernetesVersion;
    private String dnsPrefix;
    private String nodeResourceGroup;
    private int nodeCount;
    private String provisioningState;
}
