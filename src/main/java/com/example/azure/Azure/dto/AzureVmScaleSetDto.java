package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureVmScaleSetDto {
    private String name;
    private String resourceGroup;
    private String region;
    private String sku;
    private long capacity;
    private String upgradeMode;
    private boolean overProvision;
}
