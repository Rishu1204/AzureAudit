package com.example.azure.Azure.service;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.containerservice.models.KubernetesClusterAgentPool;
import com.azure.resourcemanager.loganalytics.LogAnalyticsManager;
import com.azure.resourcemanager.managementgroups.ManagementGroupsManager;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.fluent.models.MetricInner;
import com.azure.resourcemanager.monitor.fluent.models.ResponseInner;
import com.azure.resourcemanager.monitor.models.Metric;
import com.azure.resourcemanager.monitor.models.MetricCollection;
import com.azure.resourcemanager.monitor.models.MetricValue;
import com.azure.resourcemanager.monitor.models.TimeSeriesElement;
import com.azure.resourcemanager.postgresql.PostgreSqlManager;
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.synapse.SynapseManager;
import com.azure.resourcemanager.webpubsub.WebPubSubManager;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import com.example.azure.Azure.config.AzureProperties;
import com.example.azure.Azure.dto.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;


@Service
@Slf4j
@AllArgsConstructor
public class AzureVirtualMachineService {

    private final AzureProperties azureConfig;
    private final CostService costService;

    public List<AzureInventoryResponse> fetchInventory() {
        List<AzureInventoryResponse> responses = new ArrayList<>();
        List<String> subscriptionIds = fetchAllSubscriptionIdsForTenant();
        for (String subscriptionId : subscriptionIds) {
            Map<String, Map<String, Object>> costAnalysis = costService.getCostAnalysis(subscriptionId);
            log.info("Fetching inventory for subscription: {}", subscriptionId);
            AzureResourceManager azure = buildAzureClient(subscriptionId);
            ExecutorService executor = Executors.newFixedThreadPool(10);
            try {
                CompletableFuture<List<AzureVirtualMachineDto>> vms =
                        supplyAsyncSafe(() -> fetchVirtualMachines(azure, costAnalysis, subscriptionId), executor);
                CompletableFuture<List<AzureSqlServiceDto>> sql =
                        supplyAsyncSafe(() -> fetchSqlDatabases(azure, costAnalysis), executor);
                CompletableFuture<List<AzureVirtualNetworksDto>> networks =
                        supplyAsyncSafe(() -> fetchNetworks(azure), executor);
                CompletableFuture<List<AzureStorageAccountDto>> storage =
                        supplyAsyncSafe(() -> fetchStorageAccounts(azure, costAnalysis), executor);
                CompletableFuture<List<AzureDiskDto>> disks =
                        supplyAsyncSafe(() -> fetchDisks(azure, costAnalysis), executor);
                CompletableFuture<List<AzureFunctionAppDto>> functions =
                        supplyAsyncSafe(() -> fetchFunctionApps(azure), executor);
                CompletableFuture<List<AzureWebAppDto>> webApps =
                        supplyAsyncSafe(() -> fetchWebApps(azure), executor);
                CompletableFuture<List<AzureCosmosDbDto>> cosmos =
                        supplyAsyncSafe(() -> fetchCosmosDb(azure), executor);
                CompletableFuture<List<AzureRedisDto>> redis =
                        supplyAsyncSafe(() -> fetchRedis(azure), executor);
                CompletableFuture<List<AzureEventHubDto>> eventHubs =
                        supplyAsyncSafe(() -> fetchEventHubs(azure), executor);
                CompletableFuture<List<AzureContainerRegistryDto>> acr =
                        supplyAsyncSafe(() -> fetchContainerRegistries(azure), executor);
                CompletableFuture<List<AzureCdnProfileDto>> cdn =
                        supplyAsyncSafe(() -> fetchCdnProfiles(azure), executor);
                CompletableFuture<List<AzureKubernetesDto>> aksCluster =
                        supplyAsyncSafe(() -> fetchAksClusters(azure), executor);
                CompletableFuture<List<AzureContainerInstancesDto>> containerInstances =
                        supplyAsyncSafe(() -> fetchContainerInstances(azure), executor);
                CompletableFuture<List<AzureAutoScaleDto>> autoScale =
                        supplyAsyncSafe(() -> fetchAutoScale(azure), executor);
                CompletableFuture<List<AzureVmScaleSetDto>> vmScaleSet =
                        supplyAsyncSafe(() -> fetchVmScaleSets(azure), executor);
                CompletableFuture<List<AzureDnsZoneDto>> dnsZones =
                        supplyAsyncSafe(() -> fetchDnsZones(azure), executor);
                CompletableFuture<List<AzureLoadBalancerDto>> loadBalancers =
                        supplyAsyncSafe(() -> fetchLoadBalancers(azure), executor);
                CompletableFuture<List<AzureSnapshotDto>> snapshots =
                        supplyAsyncSafe(() -> fetchSnapshots(azure), executor);
                CompletableFuture<List<AzurePostgreSqlDto>> postGreSqlServers =
                        supplyAsyncSafe(() -> fetchPostGreSqlServers(subscriptionId), executor);
                CompletableFuture<List<AzureLogAnalyticsWorkspaceDto>> logAnalyticsWorkspaces =
                        supplyAsyncSafe(() -> fetchLogAnalyticsWorkspaces(subscriptionId), executor);
                CompletableFuture<List<AzureSynapseWorkspaceDto>> synapseWorkspaces =
                        supplyAsyncSafe(() -> fetchSynapseWorkspaces(subscriptionId), executor);
                CompletableFuture<List<AzureApiManagementDto>> apiManagementService =
                        supplyAsyncSafe(() -> fetchApiManagementServices(subscriptionId), executor);
                CompletableFuture<List<AzureWebPubSubDto>> webPubSubServices =
                        supplyAsyncSafe(() -> fetchWebPubSubServices(subscriptionId), executor);
                CompletableFuture<List<AzureServiceBusDto>> serviceBus =
                        supplyAsyncSafe(() -> fetchServiceBusNamespaces(azure), executor);
                CompletableFuture<List<AzurePublicIpsDto>> publicIps =
                        supplyAsyncSafe(() -> fetchPublicIps(azure, costAnalysis), executor);

                CompletableFuture.allOf(
                        vms, sql, networks, storage, disks,
                        functions, webApps, cosmos, redis,
                        eventHubs, acr, cdn, aksCluster, containerInstances,
                        autoScale, vmScaleSet, dnsZones, loadBalancers, snapshots,
                        postGreSqlServers, logAnalyticsWorkspaces, synapseWorkspaces,
                        apiManagementService, webPubSubServices, serviceBus, publicIps
                ).join();
                responses.add(
                        AzureInventoryResponse.builder()
                                .virtualMachines(vms.get())
                                .sqlDatabases(sql.get())
                                .virtualNetworks(networks.get())
                                .storageAccounts(storage.get())
                                .disks(disks.get())
                                .functionApps(functions.get())
                                .webApps(webApps.get())
                                .cosmosDbAccounts(cosmos.get())
                                .redisCaches(redis.get())
                                .eventHubs(eventHubs.get())
                                .containerRegistries(acr.get())
                                .cdnProfiles(cdn.get())
                                .azureAks(aksCluster.get())
                                .azureContainerInstances(containerInstances.get())
                                .azureAutoScale(autoScale.get())
                                .azureVmScaleSet(vmScaleSet.get())
                                .azureDnsZone(dnsZones.get())
                                .azureLoadBalancer(loadBalancers.get())
                                .azureSnapshots(snapshots.get())
                                .azurePostgreSql(postGreSqlServers.get())
                                .logAnalyticsWorkspaces(logAnalyticsWorkspaces.get())
                                .synapseWorkspaces(synapseWorkspaces.get())
                                .azureApiManagementServices(apiManagementService.get())
                                .azureWebPubSubServices(webPubSubServices.get())
                                .azureServiceBus(serviceBus.get())
                                .azurePublicIps(publicIps.get())
                                .build());
            } catch (Exception e) {
                log.error("Failed to build Azure inventory", e);
                throw new RuntimeException("Azure inventory failed", e);
            } finally {
                executor.shutdown();
            }
        }
        return responses;
    }


    private <T> CompletableFuture<T> supplyAsyncSafe(
            Supplier<T> supplier,
            Executor executor) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                log.error("Azure resource fetch failed", e);
                return (T) Collections.emptyList();
            }
        }, executor);
    }

    private List<AzureVirtualMachineDto> fetchVirtualMachines(AzureResourceManager azure,
                                                              Map<String, Map<String, Object>> costExplorer,
                                                              String subscriptionId) {
        List<AzureVirtualMachineDto> result = new ArrayList<>();
        log.info("Fetching virtual machines...");
        azure.virtualMachines().list().forEach(vm -> {
            log.info("Processing VM: {} in resource group: {}", vm.name(), vm.resourceGroupName());
            AzureVirtualMachineDto dto = AzureVirtualMachineDto.builder()
                    .name(vm.name())
                    .resourceGroup(vm.resourceGroupName())
                    .region(vm.regionName())
                    .size(vm.size() != null ? vm.size().getValue() : null)
                    .osType(vm.osType() != null ? vm.osType().toString() : null)
                    .powerState(vm.powerState() != null
                            ? extractLastSegment(vm.powerState().getValue())
                            : "Unknown")
                    .networkInterfaces(extractList(vm.networkInterfaceIds()))
                    .priority(vm.innerModel().priority().getValue())
                    .publicIpAddress(extractLastSegment(vm.getPrimaryPublicIPAddressId()))
                    .type(extractLastSegment(vm.innerModel().type()))
                    .avgCpu(calculateMetric(vm.id(), "Percentage CPU", "Average", 7, false, subscriptionId))
                    .peakCpu(calculateMetric(vm.id(), "Percentage CPU", "Maximum", 7, false, subscriptionId))
                    .avgMemory(calculateMetric(vm.id(), "Available Memory Bytes", "Average", 7, true, subscriptionId))
                    .peakMemory(calculateMetric(vm.id(), "Available Memory Bytes", "Maximum", 7, true, subscriptionId))
                    .vmCost(calculateCost(costExplorer, vm.innerModel().name().toLowerCase()))
                    .build();
            result.add(dto);
        });
        return result;
    }

    private double calculateMetric(String resourceId, String metricName, String aggregationType,
            int days, boolean convertToGb, String subscriptionId) {

        TokenCredential credential = new ClientSecretCredentialBuilder()
                .tenantId(azureConfig.getTenantId())
                .clientId(azureConfig.getClientId())
                .clientSecret(azureConfig.getClientSecret())
                .build();
        AzureProfile profile = new AzureProfile(azureConfig.getTenantId(), subscriptionId, AzureEnvironment.AZURE);
        MonitorManager monitorManager = MonitorManager.configure().authenticate(credential, profile);

        OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startTime = endTime.minusDays(days);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"); // UTC
        String timespan = startTime.format(formatter) + "/" + endTime.format(formatter);

        Response<ResponseInner> innerResponse = monitorManager
                .serviceClient()
                .getMetrics()
                .listWithResponse(
                        resourceId,
                        timespan,
                        Duration.ofDays(1),
                        metricName,
                        aggregationType,
                        null,
                        null,
                        null,
                        null,
                        "2023-10-01",
                        Context.NONE
                );

        return getResult(aggregationType, convertToGb, innerResponse);
    }

    private double getResult(String aggregationType, boolean convertToGb, Response<ResponseInner> innerResponse) {
        List<MetricInner> metricsList = innerResponse.getValue().value();


        double result = aggregationType.equalsIgnoreCase("maximum") ? 0 : 0;
        double sum = 0;
        int count = 0;

        for (MetricInner metric : metricsList) {
            for (TimeSeriesElement ts : metric.timeseries()) {
                for (MetricValue value : ts.data()) {

                    if ("average".equalsIgnoreCase(aggregationType) && value.average() != null) {
                        sum += value.average();
                        count++;
                    }

                    if ("maximum".equalsIgnoreCase(aggregationType) && value.maximum() != null) {
                        result = Math.max(result, value.maximum());
                    }
                }
            }
        }

        if ("average".equalsIgnoreCase(aggregationType) && count > 0) {
            result = sum / count;
        }
        if (convertToGb) {
            result = result / (1024 * 1024 * 1024);
        }
        return result;
    }

    private String extractList(List<String> strings) {
        return strings.stream()
                .map(item -> {
                    log.info("Extracting last segment from: {}", item);
                    return extractLastSegment(item);
                }).toList().toString();
    }

    private double calculateCost(Map<String, Map<String, Object>> costExplorer, String referenceName) {
        Map<String, Object> costObject = costExplorer.get(referenceName);
        if (costObject == null) {
            return 0.0;
        }

        Object costValue = costObject.get("Cost");
        if (costValue == null) {
            return 0.0;
        }

        if (costValue instanceof Double) {
            return (Double) costValue;
        } else {
            try {
                return Double.parseDouble(costValue.toString());
            } catch (NumberFormatException e) {
                log.error("Failed to parse cost value for VM: {}", referenceName, e);
                return 0.0;
            }
        }
    }



    private String extractLastSegment(String referenceString){
        return referenceString.substring(referenceString.lastIndexOf("/") + 1);
    }

    private List<AzureVirtualNetworksDto> fetchNetworks(AzureResourceManager azure) {
        List<AzureVirtualNetworksDto> result = new ArrayList<>();
        log.info("Fetching virtual networks...");
        azure.networks().list().forEach(network -> {
            log.info("Processing network: {} in resource group: {}", network.name(), network.resourceGroupName());
            AzureVirtualNetworksDto dto = AzureVirtualNetworksDto.builder()
                    .networkName(network.name())
                    .resourceGroupName(network.resourceGroupName())
                    .region(network.regionName())
                    .addressSpace(network.addressSpaces() != null
                            ? String.join(",", network.addressSpaces())
                            : null)
                    .subnets(network.subnets() != null
                            ? String.join(",", network.subnets().keySet())
                            : null)
                    .regionalCommunity(network.regionName())
                    .enableDdosProtection(network.isDdosProtectionEnabled())
                    .ddosProtectionPlanId(network.ddosProtectionPlanId())
                    .dhcpOptions(network.innerModel().dhcpOptions() != null
                            ? String.join(",", network.innerModel().dhcpOptions().dnsServers())
                            : null)
                    .dnsServerIps(network.dnsServerIPs() != null
                            ? String.join(",", network.dnsServerIPs())
                            : null)
                    .enableVmProtection(network.isVmProtectionEnabled())
                    .eTag(network.innerModel().etag())
                    .resourceGuid(network.innerModel().resourceGuid())
                    .build();
            result.add(dto);
        });
        return result;
    }

    private List<AzureStorageAccountDto> fetchStorageAccounts(AzureResourceManager azure,
                                                              Map<String, Map<String, Object>> costExplorer) {
        List<AzureStorageAccountDto> result = new ArrayList<>();
        log.info("Fetching storage accounts...");
        azure.storageAccounts().list().forEach(storage -> {
            log.info("Processing storage account: {} in resource group: {}", storage.name(), storage.resourceGroupName());
            List<String> containerNames = new ArrayList<>();
            List<String> queueNames = new ArrayList<>();
            try {
                String key = storage.getKeys().get(0).value();
                String connectionString =
                        "DefaultEndpointsProtocol=https;" +
                                "AccountName=" + storage.name() + ";" +
                                "AccountKey=" + key + ";" +
                                "EndpointSuffix=core.windows.net";
                BlobServiceClient blobServiceClient =
                        new BlobServiceClientBuilder()
                                .connectionString(connectionString)
                                .buildClient();
                blobServiceClient.listBlobContainers()
                        .forEach(container -> containerNames.add(container.getName()));
                log.info("Fetched {} blob containers for storage account: {}", containerNames.size(), storage.name());
                QueueServiceClient queueServiceClient =
                        new QueueServiceClientBuilder()
                                .connectionString(connectionString)
                                .buildClient();
                queueServiceClient.listQueues()
                        .forEach(queue -> queueNames.add(queue.getName()));
                log.info("Fetched {} queues for storage account: {}", queueNames.size(), storage.name());
            } catch (Exception ex) {
                log.error("Error accessing storage data plane for account: {}", storage.name(), ex);
            }
            AzureStorageAccountDto dto = AzureStorageAccountDto.builder()
                    .name(storage.name())
                    .resourceGroup(storage.resourceGroupName())
                    .region(storage.regionName())
                    .provisioningState(storage.innerModel().provisioningState().name())
                    .sku(storage.skuType() != null ? storage.skuType().name().getValue() : null)
                    .kind(storage.kind() != null ? storage.kind().toString() : null)
                    .accessTier(storage.accessTier() != null ? storage.accessTier().toString() : null)
                    .primaryStatus(storage.accountStatuses().primary().name())
                    .blobContainers(containerNames)
                    .queueNames(queueNames)
                    .storageAccountCost(calculateCost(costExplorer, storage.innerModel().name().toLowerCase()))
                    .build();

            result.add(dto);
        });

        return result;
    }


    private List<AzureDiskDto> fetchDisks(AzureResourceManager azure,
                                          Map<String, Map<String, Object>> costExplorer) {
        List<AzureDiskDto> result = new ArrayList<>();
        log.info("Fetching disks...");
        azure.disks().list().forEach(disk -> {
            log.info("Processing disk: {} in resource group: {}", disk.name(), disk.resourceGroupName());
            AzureDiskDto dto = AzureDiskDto.builder()
                    .name(disk.name())
                    .resourceGroup(disk.resourceGroupName())
                    .region(disk.regionName())
                    .sizeInGb(disk.sizeInGB())
                    .sku(disk.sku() != null ? disk.sku().toString() : null)
                    .osType(disk.osType() != null ? disk.osType().toString() : null)
                    .diskSizeGb(Optional.ofNullable(disk.innerModel().diskSizeGB()).orElse(0))
                    .diskState(disk.innerModel().diskState().getValue())
                    .diskIopsReadOnly(Optional.ofNullable(disk.innerModel().diskIopsReadOnly()).orElse(0L))
                    .diskIopsReadWrite(Optional.ofNullable(disk.innerModel().diskIopsReadWrite()).orElse(0L))
                    .diskMBpsReadOnly(Optional.ofNullable(disk.innerModel().diskMBpsReadOnly()).orElse(0L))
                    .diskMBpsReadWrite(Optional.ofNullable(disk.innerModel().diskMBpsReadWrite()).orElse(0L))
                    .diskCost(calculateCost(costExplorer, disk.innerModel().name().toLowerCase()))
                    .build();
            result.add(dto);
        });
        return result;
    }

    private List<AzureFunctionAppDto> fetchFunctionApps(AzureResourceManager azure) {
        List<AzureFunctionAppDto> result = new ArrayList<>();
        log.info("Fetching function apps...");
        azure.functionApps().list().forEach(function -> {
            log.info("Processing function app: {} in resource group: {}", function.name(), function.resourceGroupName());
            AzureFunctionAppDto dto = AzureFunctionAppDto.builder()
                    .name(function.name())
                    .id(function.id())
                    .resourceGroup(function.resourceGroupName())
                    .region(function.regionName())
                    .state(function.state())
//                    .runtime(function.javaVersion())
                    .defaultHostName(function.defaultHostname())
                    .build();

            result.add(dto);
        });

        return result;
    }


    private List<AzureWebAppDto> fetchWebApps(AzureResourceManager azure) {
        List<AzureWebAppDto> result = new ArrayList<>();
        log.info("Fetching web apps...");
        azure.webApps().list().forEach(webApp -> {
        log.info("Processing web app: {} in resource group: {}", webApp.name(), webApp.resourceGroupName());
            AzureWebAppDto dto = AzureWebAppDto.builder()
                    .name(webApp.name())
                    .id(webApp.id())
                    .resourceGroup(webApp.resourceGroupName())
                    .region(webApp.regionName())
                    .defaultHostName(webApp.defaultHostname())
                    .state(webApp.state())
                    .httpsOnly(webApp.httpsOnly())
                    .build();

            result.add(dto);
        });

        return result;
    }


    private List<AzureCosmosDbDto> fetchCosmosDb(AzureResourceManager azure) {
        List<AzureCosmosDbDto> result = new ArrayList<>();
        log.info("Fetching Cosmos DB accounts...");
        azure.cosmosDBAccounts().list().forEach(account -> {
            log.info("Processing Cosmos DB account: {} in resource group: {}", account.name(), account.resourceGroupName());
            AzureCosmosDbDto dto = AzureCosmosDbDto.builder()
                    .name(account.name())
                    .id(account.id())
                    .resourceGroup(account.resourceGroupName())
                    .region(account.regionName())
                    .kind(account.kind() != null
                            ? account.kind().toString()
                            : null)
                    .enableAutomaticFailover(account.automaticFailoverEnabled())
                    .build();

            result.add(dto);
        });

        return result;
    }


    private List<AzureRedisDto> fetchRedis(AzureResourceManager azure) {
        List<AzureRedisDto> result = new ArrayList<>();
        log.info("Fetching Redis caches...");
        azure.redisCaches().list().forEach(redis -> {
            log.info("Processing Redis cache: {} in resource group: {}", redis.name(), redis.resourceGroupName());
            AzureRedisDto dto = AzureRedisDto.builder()
                    .name(redis.name())
                    .id(redis.id())
                    .resourceGroup(redis.resourceGroupName())
                    .region(redis.regionName())
                    .sku(redis.sku() != null ? redis.sku().name().getValue() : null)
                    .redisVersion(redis.redisVersion())
                    .enableNonSslPort(redis.nonSslPort())
                    .build();

            result.add(dto);
        });

        return result;
    }


    private List<AzureEventHubDto> fetchEventHubs(AzureResourceManager azure) {
        List<AzureEventHubDto> result = new ArrayList<>();
        log.info("Fetching Event Hubs namespaces...");
        azure.eventHubNamespaces().list().forEach(namespace -> {
            log.info("Processing Event Hub namespace: {} in resource group: {}", namespace.name(), namespace.resourceGroupName());
            AzureEventHubDto dto = AzureEventHubDto.builder()
                    .name(namespace.name())
                    .id(namespace.id())
                    .resourceGroup(namespace.resourceGroupName())
                    .region(namespace.regionName())
                    .sku(namespace.sku() != null ? namespace.sku().name().getValue() : null)
                    .build();

            result.add(dto);
        });
        return result;
    }


    private List<AzureContainerRegistryDto> fetchContainerRegistries(AzureResourceManager azure) {
        List<AzureContainerRegistryDto> result = new ArrayList<>();
        log.info("Fetching Container Registries...");
        azure.containerRegistries().list().forEach(registry -> {
            log.info("Processing Container Registry: {} in resource group: {}", registry.name(), registry.resourceGroupName());
            AzureContainerRegistryDto dto = AzureContainerRegistryDto.builder()
                    .name(registry.name())
                    .id(registry.id())
                    .resourceGroup(registry.resourceGroupName())
                    .region(registry.regionName())
                    .sku(registry.sku() != null ? registry.sku().name().getValue() : null)
                    .loginServer(registry.loginServerUrl())
                    .adminUserEnabled(registry.adminUserEnabled())
                    .build();

            result.add(dto);
        });

        return result;
    }


    private List<AzureCdnProfileDto> fetchCdnProfiles(AzureResourceManager azure) {
        List<AzureCdnProfileDto> result = new ArrayList<>();
        log.info("Fetching CDN profiles...");
        azure.cdnProfiles().list().forEach(profile -> {
            log.info("Processing CDN profile: {} in resource group: {}", profile.name(), profile.resourceGroupName());
            AzureCdnProfileDto dto = AzureCdnProfileDto.builder()
                    .name(profile.name())
                    .id(profile.id())
                    .resourceGroup(profile.resourceGroupName())
                    .region(profile.regionName())
                    .sku(profile.sku() != null ? profile.sku().name().getValue() : null)
                    .build();

            result.add(dto);
        });

        return result;
    }


    private List<AzureSqlServiceDto> fetchSqlDatabases(AzureResourceManager azure,
                                                       Map<String, Map<String, Object>> costExplorer) {
        List<AzureSqlServiceDto> inventoryList = new ArrayList<>();
        log.info("Fetching SQL Databases...");
        try {
            PagedIterable<SqlServer> sqlServers = azure.sqlServers().list();
            sqlServers.forEach(sqlServer -> {
                log.info("Processing SQL Server: {} in resource group: {}", sqlServer.name(), sqlServer.resourceGroupName());
                List<SqlDatabase> databases = sqlServer.databases().list();
                databases.forEach(sqlDatabase -> {
                    log.info("Processing SQL Database: {} in SQL Server: {}", sqlDatabase.name(), sqlServer.name());
                    Boolean securityPolicyEnabled = null;
                    try {
                        if (sqlDatabase.getThreatDetectionPolicy() != null) {
                            securityPolicyEnabled =
                                    sqlDatabase.getThreatDetectionPolicy()
                                            .isDefaultSecurityAlertPolicy();
                        }
                    } catch (Exception ignored) {
                        securityPolicyEnabled = null;
                    }
                    AzureSqlServiceDto sqlServiceDto = AzureSqlServiceDto.builder()
                            .databaseId(sqlDatabase.databaseId())
                            .databaseName(sqlDatabase.name())
                            .region(sqlDatabase.region() != null
                                    ? sqlDatabase.region().label()
                                    : null)
                            .resourceGroupName(sqlDatabase.resourceGroupName())
                            .creationDate(sqlDatabase.creationDate() != null
                                    ? sqlDatabase.creationDate().toString()
                                    : null)
                            .defaultSecondaryLocation(sqlDatabase.defaultSecondaryLocation())
                            .edition(sqlDatabase.edition() != null
                                    ? sqlDatabase.edition().getValue()
                                    : null)
                            .collation(sqlDatabase.collation())
                            .isDefaultSecurityAlertPolicyEnabled(securityPolicyEnabled)
                            .status(sqlDatabase.status() != null
                                    ? sqlDatabase.status().getValue()
                                    : null)
                            .sqlServerName(sqlDatabase.sqlServerName())
                            .elasticPoolName(sqlDatabase.elasticPoolName())
                            .databaseCost(calculateCost(costExplorer, sqlDatabase.name()))
                            .build();
                    inventoryList.add(sqlServiceDto);
                });
            });
        } catch (Exception e) {
            log.error("Error occurred while fetching Azure SQL Databases", e);
        }
        return inventoryList;
    }

    private List<AzureKubernetesDto> fetchAksClusters(AzureResourceManager azure) {
        List<AzureKubernetesDto> result = new ArrayList<>();
        log.info("Fetching AKS clusters...");
        azure.kubernetesClusters().list().forEach(cluster -> {
            log.info("Processing AKS cluster: {} in resource group: {}", cluster.name(), cluster.resourceGroupName());
            AzureKubernetesDto dto = AzureKubernetesDto.builder()
                    .clusterName(cluster.name())
                    .resourceGroup(cluster.resourceGroupName())
                    .region(cluster.regionName())
                    .kubernetesVersion(cluster.version())
                    .dnsPrefix(cluster.dnsPrefix())
                    .nodeResourceGroup(cluster.nodeResourceGroup())
                    .nodeCount(cluster.agentPools().values()
                            .stream()
                            .mapToInt(KubernetesClusterAgentPool::count)
                            .sum())
                    .provisioningState(cluster.provisioningState())
                    .build();
            result.add(dto);
        });
        return result;
    }

    private List<AzureContainerInstancesDto> fetchContainerInstances(AzureResourceManager azure) {
        List<AzureContainerInstancesDto> result = new ArrayList<>();
        log.info("Fetching Container Instances...");
        azure.containerGroups().list().forEach(containerGroup -> {
            log.info("Processing Container Group: {} in resource group: {}", containerGroup.name(), containerGroup.resourceGroupName());
            AzureContainerInstancesDto dto = AzureContainerInstancesDto.builder()
                    .name(containerGroup.name())
                    .resourceGroup(containerGroup.resourceGroupName())
                    .region(containerGroup.regionName())
                    .osType(containerGroup.osType() != null ?
                            containerGroup.osType().toString() : null)
                    .state(containerGroup.state() != null ?
                            containerGroup.state() : null)
                    .ipAddress(containerGroup.ipAddress() != null ?
                            containerGroup.ipAddress() : null)
                    .dnsName(containerGroup.dnsPrefix())
                    .containerCount(containerGroup.containers().size())
                    .restartPolicy(containerGroup.restartPolicy() != null ?
                            containerGroup.restartPolicy().toString() : null)
                    .build();

            result.add(dto);
        });

        return result;
    }

    private List<AzureAutoScaleDto> fetchAutoScale(AzureResourceManager azure) {
        List<AzureAutoScaleDto> result = new ArrayList<>();
        log.info("Fetching AutoScale settings...");
        azure.autoscaleSettings().list().forEach(setting -> {
            log.info("Processing AutoScale setting: {} in resource group: {}", setting.name(), setting.resourceGroupName());
            AzureAutoScaleDto dto = AzureAutoScaleDto.builder()
                    .name(setting.name())
                    .resourceGroup(setting.resourceGroupName())
                    .region(setting.regionName())
                    .targetResourceId(setting.targetResourceId())
                    .enabled(setting.autoscaleEnabled())
                    .profileCount(setting.profiles() != null ?
                            setting.profiles().size() : 0)
                    .build();
            result.add(dto);
        });
        return result;
    }

    private List<AzureVmScaleSetDto> fetchVmScaleSets(AzureResourceManager azure) {
        List<AzureVmScaleSetDto> result = new ArrayList<>();
        log.info("Fetching VM Scale Sets...");
        azure.virtualMachineScaleSets().list().forEach(vmss -> {
            log.info("Processing VM Scale Set: {} in resource group: {}", vmss.name(), vmss.resourceGroupName());
            AzureVmScaleSetDto dto = AzureVmScaleSetDto.builder()
                    .name(vmss.name())
                    .resourceGroup(vmss.resourceGroupName())
                    .region(vmss.regionName())
                    .sku(vmss.sku() != null ?
                            vmss.sku().sku().name() : null)
                    .capacity(vmss.capacity())
                    .upgradeMode(vmss.upgradeModel().name())
                    .overProvision(vmss.overProvisionEnabled())
                    .build();
            result.add(dto);
        });
        return result;
    }

    private List<AzureDnsZoneDto> fetchDnsZones(AzureResourceManager azure) {
        List<AzureDnsZoneDto> result = new ArrayList<>();
        log.info("Fetching DNS Zones...");
        azure.dnsZones().list().forEach(zone -> {
            log.info("Processing DNS Zone: {} in resource group: {}", zone.name(), zone.resourceGroupName());
            AzureDnsZoneDto dto = AzureDnsZoneDto.builder()
                    .zoneName(zone.name())
                    .resourceGroup(zone.resourceGroupName())
                    .region(zone.regionName())
                    .numberOfRecordSets(zone.numberOfRecordSets())
                    .nameServers(zone.nameServers() != null ?
                            String.join(",", zone.nameServers()) : null)
                    .maxNumberOfRecordSets(zone.maxNumberOfRecordSets())
                    .build();
            result.add(dto);
        });
        return result;
    }

    private List<AzureLoadBalancerDto> fetchLoadBalancers(AzureResourceManager azure) {
        List<AzureLoadBalancerDto> result = new ArrayList<>();
        log.info("Fetching Load Balancers...");
        azure.loadBalancers().list().forEach(lb -> {
            log.info("Processing Load Balancer: {} in resource group: {}", lb.name(), lb.resourceGroupName());
            AzureLoadBalancerDto dto = AzureLoadBalancerDto.builder()
                    .name(lb.name())
                    .resourceGroup(lb.resourceGroupName())
                    .region(lb.regionName())
                    .sku(lb.sku() != null ? lb.sku().sku().name().getValue() : null)
                    .frontendIpCount(lb.frontends() != null ?
                            lb.frontends().size() : 0)
                    .backendPoolCount(lb.backends() != null ?
                            lb.backends().size() : 0)
                    .ruleCount(lb.loadBalancingRules() != null ?
                            lb.loadBalancingRules().size() : 0)
                    .probeCount(lb.innerModel().probes().size())
                    .build();
            result.add(dto);
        });
        return result;
    }

    private List<AzureSnapshotDto> fetchSnapshots(AzureResourceManager azure) {
        List<AzureSnapshotDto> result = new ArrayList<>();
        log.info("Fetching Snapshots...");
        azure.snapshots().list().forEach(snapshot -> {
            log.info("Processing Snapshot: {} in resource group: {}", snapshot.name(), snapshot.resourceGroupName());
            AzureSnapshotDto dto = AzureSnapshotDto.builder()
                    .name(snapshot.name())
                    .resourceGroup(snapshot.resourceGroupName())
                    .region(snapshot.regionName())
                    .diskSizeGb(snapshot.sizeInGB())
                    .sku(snapshot.skuType().accountType().getValue())
                    .osType(snapshot.osType() != null ? snapshot.osType().toString() : null)
                    .creationData(snapshot.innerModel().creationData() != null
                            ? snapshot.innerModel().creationData().createOption().toString() : null)
                    .timeCreated(snapshot.innerModel().timeCreated() != null
                            ? snapshot.innerModel().timeCreated().toString() : null)
                    .sourceResourceId(snapshot.innerModel().creationData() != null
                            ? snapshot.innerModel().creationData().sourceResourceId()
                            : null)
                    .provisioningState(snapshot.innerModel().provisioningState())
                    .build();
            result.add(dto);
        });

        return result;
    }

    private List<AzurePostgreSqlDto> fetchPostGreSqlServers(String subscriptionId) {
        PostgreSqlManager postgreSqlManager = buildPostGreManager(subscriptionId);
        List<AzurePostgreSqlDto> result = new ArrayList<>();
        log.info("Fetching PostgreSQL servers...");
        postgreSqlManager.servers().list().forEach(server -> {
            log.info("Processing PostgreSQL server: {} ", server.name());
            AzurePostgreSqlDto dto = AzurePostgreSqlDto.builder()
                    .name(server.name())
                    .region(server.regionName())
                    .administratorLogin(server.administratorLogin())
                    .version(server.version().getValue())
                    .sku(server.sku() != null ? server.sku().name() : null)
                    .storageGb(server.storageProfile() != null ? server.storageProfile().storageMB() / 1024 : null)
                    .build();
            result.add(dto);
        });

        return result;
    }


    private PostgreSqlManager buildPostGreManager(String subscriptionId) {
        return PostgreSqlManager.configure()
                .authenticate(buildCredentials(), buildProfile(subscriptionId));
    }

    private TokenCredential buildCredentials() {
        return new ClientSecretCredentialBuilder()
                .tenantId(azureConfig.getTenantId())
                .clientId(azureConfig.getClientId())
                .clientSecret(azureConfig.getClientSecret())
                .build();
    }

    private AzureProfile buildProfile(String subscriptionId) {
        return new AzureProfile(
                azureConfig.getTenantId(),
                subscriptionId,
                AzureEnvironment.AZURE
        );
    }

    private AzureResourceManager buildAzureClient(String subscriptionId) {
        return AzureResourceManager.configure()
                .authenticate(
                        new ClientSecretCredentialBuilder()
                                .tenantId(azureConfig.getTenantId())
                                .clientId(azureConfig.getClientId())
                                .clientSecret(azureConfig.getClientSecret())
                                .build(),
                        new AzureProfile(
                                azureConfig.getTenantId(),
                                subscriptionId,
                                AzureEnvironment.AZURE
                        )
                )
                .withSubscription(subscriptionId);
    }

    private List<AzureLogAnalyticsWorkspaceDto> fetchLogAnalyticsWorkspaces(String subscriptionId) {
        List<AzureLogAnalyticsWorkspaceDto> workspacesList = new ArrayList<>();
        log.info("Fetching Log Analytics workspaces...");
        try {
            LogAnalyticsManager manager = buildLogAnalyticsManager(subscriptionId);
            manager.workspaces().list().forEach(workspace -> {
                log.info("Processing Log Analytics workspace: {} in resource group: {}", workspace.name(), workspace.resourceGroupName());
                AzureLogAnalyticsWorkspaceDto dto = AzureLogAnalyticsWorkspaceDto.builder()
                        .name(workspace.name())
                        .id(workspace.id())
                        .resourceGroup(workspace.resourceGroupName())
                        .region(workspace.regionName())
                        .sku(workspace.sku() != null ? workspace.sku().name().getValue() : null)
                        .retentionInDays(workspace.retentionInDays() != null
                                ? workspace.retentionInDays()
                                : 0)
                        .publicNetworkAccessForIngestion(workspace.publicNetworkAccessForIngestion() != null
                                ? workspace.publicNetworkAccessForIngestion().getValue()
                                : null)
                        .publicNetworkAccessForQuery(workspace.publicNetworkAccessForQuery() != null
                                ? workspace.publicNetworkAccessForQuery().getValue()
                                : null)
                        .build();
                workspacesList.add(dto);
            });

        } catch (Exception e) {
            log.error("Error fetching Log Analytics workspaces", e);
        }

        return workspacesList;
    }

    private LogAnalyticsManager buildLogAnalyticsManager(String subscriptionId) {
        return LogAnalyticsManager.configure()
                .authenticate(buildCredentials(), buildProfile(subscriptionId));
    }

    private List<AzureSynapseWorkspaceDto> fetchSynapseWorkspaces(String subscriptionId) {
        List<AzureSynapseWorkspaceDto> result = new ArrayList<>();
        log.info("Fetching Synapse workspaces and SQL pools...");
        try {
            SynapseManager manager = buildSynapseManager(subscriptionId);
            manager.workspaces().list().forEach(workspace -> {
                log.info("Processing Synapse workspace: {} in resource group: {}", workspace.name(), workspace.resourceGroupName());
                String rg = workspace.resourceGroupName();
                String wsName = workspace.name();
                manager.sqlPools().listByWorkspace(rg, wsName).forEach(pool -> {
                    log.info("Processing SQL pool: {} in Synapse workspace: {}", pool.name(), wsName);
                    AzureSynapseWorkspaceDto dto = AzureSynapseWorkspaceDto.builder()
                            .workspaceName(wsName)
                            .workspaceId(workspace.id())
                            .resourceGroup(rg)
                            .region(workspace.regionName())
                            .sqlPoolName(pool.name())
                            .sqlPoolEdition(pool.sku() != null ? pool.sku().name() : null)
                            .sqlPoolStatus(pool.status())
                            .build();

                    result.add(dto);
                });
            });

        } catch (Exception e) {
            log.error("Error fetching Synapse workspaces and pools", e);
        }
        return result;
    }

    private SynapseManager buildSynapseManager(String subscriptionId) {
        return SynapseManager.configure().authenticate(buildCredentials(), buildProfile(subscriptionId));
    }

    private List<AzureApiManagementDto> fetchApiManagementServices(String subscriptionId) {
        List<AzureApiManagementDto> result = new ArrayList<>();
        log.info("Fetching API Management services...");
        try {
            ApiManagementManager manager = buildApiManagementManager(subscriptionId);
            manager.apiManagementServices().list().forEach(service -> {
                log.info("Processing API Management service: {} in resource group: {}", service.name(), service.resourceGroupName());
                AzureApiManagementDto dto = AzureApiManagementDto.builder()
                        .name(service.name())
                        .id(service.id())
                        .resourceGroup(service.resourceGroupName())
                        .region(service.regionName())
                        .publisherEmail(service.publisherEmail())
                        .publisherName(service.publisherName())
                        .sku(service.sku() != null ? service.sku().name().getValue() : null)
                        .capacity(service.sku() != null ? service.sku().capacity() : null)
                        .gatewayUrl(service.gatewayUrl())
                        .build();
                result.add(dto);
            });
        } catch (Exception e) {
            log.error("Error fetching API Management services", e);
        }
        return result;
    }


    private ApiManagementManager buildApiManagementManager(String subscriptionId) {
        return ApiManagementManager.configure().authenticate(buildCredentials(), buildProfile(subscriptionId));
    }

    private List<AzureWebPubSubDto> fetchWebPubSubServices(String subscriptionId) {
        List<AzureWebPubSubDto> result = new ArrayList<>();
        log.info("Fetching Web PubSub services...");
        try {
            WebPubSubManager manager = buildWebPubSubManager(subscriptionId);
            manager.webPubSubs().list().forEach(webPubSubResource -> {
                log.info("Processing Web PubSub service: {} in resource group: {}", webPubSubResource.name(), webPubSubResource.resourceGroupName());
                AzureWebPubSubDto dto = AzureWebPubSubDto.builder()
                        .name(webPubSubResource.name())
                        .id(webPubSubResource.id())
                        .resourceGroup(webPubSubResource.resourceGroupName())
                        .region(webPubSubResource.regionName())
                        .sku(webPubSubResource.sku() != null ? webPubSubResource.sku().name() : null)
                        .capacity(webPubSubResource.sku() != null ? webPubSubResource.sku().capacity() : null)
                        .tls(webPubSubResource.tls() != null ? webPubSubResource.innerModel().tls().clientCertEnabled() : false)
                        .hostName(webPubSubResource.innerModel().hostname())
                        .build();
                result.add(dto);
            });
        } catch (Exception e) {
            log.error("Error fetching Azure Web PubSub services", e);
        }
        return result;
    }


    private WebPubSubManager buildWebPubSubManager(String subscriptionId) {
        return WebPubSubManager.configure().authenticate(buildCredentials(), buildProfile(subscriptionId));
    }

    private List<AzureServiceBusDto> fetchServiceBusNamespaces(AzureResourceManager azure) {
        List<AzureServiceBusDto> result = new ArrayList<>();
        log.info("Fetching Service Bus namespaces...");
        try {
            List<ServiceBusNamespace> serviceBusNamespaces = azure.serviceBusNamespaces().list().stream().toList();
            serviceBusNamespaces.forEach(bus -> {
                log.info("Processing Service Bus namespace: {} in resource group: {}", bus.name(), bus.resourceGroupName());
                List<Queue> queueList = bus.queues().list().stream().toList();
                List<Topic> topicList = bus.topics().list().stream().toList();
                AzureServiceBusDto dto = AzureServiceBusDto.builder()
                        .name(bus.name())
                        .id(bus.id())
                        .resourceGroup(bus.resourceGroupName())
                        .region(bus.regionName())
                        .sku(bus.sku() != null ? bus.sku().name().name() : null)
                        .capacity(bus.sku() != null ? bus.sku().capacity() : null)
                        .status(bus.innerModel().status())
                        .queue(queueList)
                        .topics(topicList)
                        .build();
                result.add(dto);
            });
        } catch (Exception e) {
            log.error("Error fetching Service Bus namespaces", e);
        }
        return result;
    }

    public List<String> fetchAllSubscriptionIdsForTenant() {
        ManagementGroupsManager mgManager = buildManagementGroupsManager();
        List<String> allSubscriptionIds = new ArrayList<>();
        log.info("Fetching management groups for tenant: {}", azureConfig.getTenantId());
        mgManager.managementGroups().list()
                .forEach(root -> traverseForSubscriptions(root.name(), mgManager, allSubscriptionIds));

        return allSubscriptionIds;
    }

    private void traverseForSubscriptions(String groupId, ManagementGroupsManager mgManager,
                                          List<String> allSubscriptionIds) {
        log.info("Traversing management group: {}", groupId);
        mgManager.managementGroupSubscriptions()
                .getSubscriptionsUnderManagementGroup(groupId)
                .forEach(sub -> allSubscriptionIds.add(sub.name()));
        log.info("Found {} subscriptions under group {}", allSubscriptionIds.size(), groupId);
        mgManager.managementGroups().getDescendants(groupId)
                .forEach(child -> {
                    String type = child.type();
                    String childId = child.name();
                    log.info("Processing child resource: {} of type: {}", childId, type);
                    if (!type.equalsIgnoreCase("Microsoft.Management/managementGroups/subscriptions")) {
                        log.info("Recursing into child management group: {}", childId);
                        traverseForSubscriptions(childId, mgManager, allSubscriptionIds);
                    }
                });
    }

    private ManagementGroupsManager buildManagementGroupsManager() {
        TokenCredential credential = new ClientSecretCredentialBuilder()
                .tenantId(azureConfig.getTenantId())
                .clientId(azureConfig.getClientId())
                .clientSecret(azureConfig.getClientSecret())
                .build();
        AzureProfile profile = new AzureProfile(
                azureConfig.getTenantId(),
                null,
                AzureEnvironment.AZURE
        );
        return ManagementGroupsManager.authenticate(credential, profile);
    }

    private List<AzurePublicIpsDto> fetchPublicIps(AzureResourceManager azure,
                                                              Map<String, Map<String, Object>> costExplorer) {
        List<AzurePublicIpsDto> result = new ArrayList<>();
        log.info("Fetching Public Ips...");
        azure.publicIpAddresses().list().forEach(ip -> {
            log.info("Processing public ips: {} in resource group: {}", ip.name(), ip.resourceGroupName());
            AzurePublicIpsDto dto = AzurePublicIpsDto.builder()
                    .name(ip.name())
                    .resourceGroup(ip.resourceGroupName())
                    .region(ip.regionName())
                    .ipAddress(ip.ipAddress())
                    .allocationMethod(ip.innerModel().publicIpAllocationMethod() != null
                            ? ip.innerModel().publicIpAllocationMethod().toString()
                            : null)
                    .idleTimeoutInMinutes(Optional.ofNullable(ip.innerModel().idleTimeoutInMinutes()).orElse(0))
                    .publicIpAddressType(Optional.of(extractLastSegment(ip.innerModel().type())).orElse(""))
                    .publicIpAddressVersion(ip.innerModel().publicIpAddressVersion() != null
                            ? ip.innerModel().publicIpAddressVersion().getValue()
                            : null)
                    .publicIpCost(calculateCost(costExplorer, ip.name().toLowerCase()))
                    .build();
            result.add(dto);
        });
        return result;
    }


//    =========== "Excel Export Logic" ===========


    public byte[] exportInventoryToExcel(List<AzureInventoryResponse> azureResponses) throws IOException {
        if (azureResponses == null || azureResponses.isEmpty()) {
            throw new IllegalArgumentException("Azure response list is empty");
        }
        Workbook workbook = new XSSFWorkbook();
        for (AzureInventoryResponse response : azureResponses) {
            writeSheet(workbook, "VirtualMachines", response.getVirtualMachines());
            writeSheet(workbook, "SqlDatabases", response.getSqlDatabases());
            writeSheet(workbook, "StorageAccounts", response.getStorageAccounts());
            writeSheet(workbook, "VirtualNetworks", response.getVirtualNetworks());
            writeSheet(workbook, "Disks", response.getDisks());
            writeSheet(workbook, "FunctionApps", response.getFunctionApps());
            writeSheet(workbook, "WebApps", response.getWebApps());
            writeSheet(workbook, "CosmosDb", response.getCosmosDbAccounts());
            writeSheet(workbook, "RedisCaches", response.getRedisCaches());
            writeSheet(workbook, "EventHubs", response.getEventHubs());
            writeSheet(workbook, "ContainerRegistries", response.getContainerRegistries());
            writeSheet(workbook, "CdnProfiles", response.getCdnProfiles());
            writeSheet(workbook, "AksClusters", response.getAzureAks());
            writeSheet(workbook, "ContainerInstances", response.getAzureContainerInstances());
            writeSheet(workbook, "AutoScale", response.getAzureAutoScale());
            writeSheet(workbook, "VmScaleSet", response.getAzureVmScaleSet());
            writeSheet(workbook, "DnsZones", response.getAzureDnsZone());
            writeSheet(workbook, "LoadBalancers", response.getAzureLoadBalancer());
            writeSheet(workbook, "Snapshots", response.getAzureSnapshots());
            writeSheet(workbook, "PostGreSqlServers", response.getAzurePostgreSql());
            writeSheet(workbook, "LogAnalyticsWorkspaces", response.getLogAnalyticsWorkspaces());
            writeSheet(workbook, "SynapseWorkspaces", response.getSynapseWorkspaces());
            writeSheet(workbook, "ApiManagementServices", response.getAzureApiManagementServices());
            writeSheet(workbook, "WebPubSubServices", response.getAzureWebPubSubServices());
            writeSheet(workbook, "ServiceBus", response.getAzureServiceBus());
            writeSheet(workbook, "Ip Address", response.getAzurePublicIps());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }


    private <T> void writeSheet(Workbook workbook, String sheetName, List<T> dataList) {

        // Skip if null OR empty
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        Sheet sheet = workbook.createSheet(sheetName);

        Field[] fields = dataList.get(0).getClass().getDeclaredFields();

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            headerRow.createCell(i).setCellValue(fields[i].getName());
        }

        for (int rowIndex = 0; rowIndex < dataList.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            T item = dataList.get(rowIndex);

            for (int colIndex = 0; colIndex < fields.length; colIndex++) {
                try {
                    Object value = fields[colIndex].get(item);
                    row.createCell(colIndex)
                            .setCellValue(value != null ? value.toString() : "");
                } catch (IllegalAccessException ignored) {
                }
            }
        }

        for (int i = 0; i < fields.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

}
