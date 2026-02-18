package com.example.azure.Azure.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AzureStorageAccountDto {

    private String name;
    private String resourceGroup;
    private String region;
    private String provisioningState;
    private String sku;
    private String kind;
    private String accessTier;
    private String primaryStatus;
    private List<String> blobContainers;
    private List<String> queueNames;
    private double storageAccountCost;

}
