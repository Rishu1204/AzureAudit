package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AzureInventoryResponse {

    private List<AzureVirtualMachineDto> virtualMachines;
    private List<AzureSqlServiceDto> sqlDatabases;
    private List<AzureVirtualNetworksDto> virtualNetworks;
    private List<AzureStorageAccountDto> storageAccounts;
    private List<AzureDiskDto> disks;
    private List<AzureFunctionAppDto> functionApps;
    private List<AzureWebAppDto> webApps;
    private List<AzureCosmosDbDto> cosmosDbAccounts;
    private List<AzureRedisDto> redisCaches;
    private List<AzureEventHubDto> eventHubs;
    private List<AzureContainerRegistryDto> containerRegistries;
    private List<AzureCdnProfileDto> cdnProfiles;
    private List<AzureKubernetesDto> azureAks;
    private List<AzureContainerInstancesDto> azureContainerInstances;
    private List<AzureAutoScaleDto> azureAutoScale;
    private List<AzureVmScaleSetDto> azureVmScaleSet;
    private List<AzureDnsZoneDto> azureDnsZone;
    private List<AzureLoadBalancerDto> azureLoadBalancer;
    private List<AzureSnapshotDto> azureSnapshots;
    private List<AzurePostgreSqlDto> azurePostgreSql;
    private List<AzureLogAnalyticsWorkspaceDto> logAnalyticsWorkspaces;
    private List<AzureSynapseWorkspaceDto> synapseWorkspaces;
    private List<AzureApiManagementDto> azureApiManagementServices;
    private List<AzureWebPubSubDto> azureWebPubSubServices;
    private List<AzureServiceBusDto> azureServiceBus;
    private List<AzurePublicIpsDto> azurePublicIps;
}

