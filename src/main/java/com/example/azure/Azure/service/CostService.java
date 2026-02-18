package com.example.azure.Azure.service;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.costmanagement.CostManagementManager;
import com.azure.resourcemanager.costmanagement.models.*;
import com.example.azure.Azure.config.AzureProperties;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class CostService {

    private final AzureProperties azureConfig;

    private CostManagementManager buildCostManagementManager(String subscriptionId) {
        TokenCredential credential = new ClientSecretCredentialBuilder()
                .tenantId(azureConfig.getTenantId())
                .clientId(azureConfig.getClientId())
                .clientSecret(azureConfig.getClientSecret())
                .build();
        AzureProfile profile = new AzureProfile(azureConfig.getTenantId(), subscriptionId, AzureEnvironment.AZURE);
        return CostManagementManager.authenticate(credential, profile);
    }

    public Map<String, Map<String, Object>> getCostAnalysis(String subscriptionId) {

        CostManagementManager manager = buildCostManagementManager(subscriptionId);
        String scope = "/subscriptions/" + subscriptionId;
        QueryDefinition query = new QueryDefinition()
                .withType(ExportType.ACTUAL_COST)
                .withTimeframe(TimeframeType.MONTH_TO_DATE)
                .withDataset(new QueryDataset()
                        .withGranularity(GranularityType.fromString("Monthly"))
                        .withAggregation(Collections.singletonMap(
                                "totalCost",
                                new QueryAggregation()
                                        .withName("CostUSD")
                                        .withFunction(FunctionType.SUM)
                        ))
                        .withGrouping(Collections.singletonList(
                                new QueryGrouping()
                                        .withType(QueryColumnType.fromString("Dimension"))
                                        .withName("ResourceId")
                        ))

                );

        QueryResult result = manager.queries().usage(scope, query);

        Map<String, Map<String, Object>> costAnalysisMap = new HashMap<>();

        if (result.rows() != null) {
            for (List<Object> row : result.rows()) {

                String resourceId = row.get(2).toString();
                String[] parts = resourceId.split("/");

                Map<String, Object> resourceData = new HashMap<>();

                resourceData.put("Date", row.get(1));
                resourceData.put("Cost", row.get(0));

                String resourceName = parts.length > 8
                        ? String.join("/", Arrays.copyOfRange(parts, 8, parts.length))
                        : "";

                resourceData.put("Resource Name", resourceName);

                try {
                    resourceData.put("Resource Type", parts[6] + "/" + parts[7]);
                } catch (Exception e) {
                    resourceData.put("Resource Type", "");
                }

                resourceData.put("Resource group",
                        parts.length > 4 ? parts[4] : "");

                costAnalysisMap.put(extractLastSegment(resourceId), resourceData);
            }
        }

        return costAnalysisMap;
    }


    private String extractLastSegment(String referenceString){
        return referenceString.substring(referenceString.lastIndexOf("/") + 1);
    }
}

