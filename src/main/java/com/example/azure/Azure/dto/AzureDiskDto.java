package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AzureDiskDto {

    private String subscriptionId;
    private String resourceGroup;
    private String region;
    private String diskName;
    private int sizeInGb;
    private String sku;
    private String osType;
    private boolean isAttachedToVm;
    private Map<String, String> attachedVm;
    private Integer diskSizeGb;
    private String diskState;
    private Long diskIopsReadOnly;
    private Long diskIopsReadWrite;
    private Long diskMBpsReadOnly;
    private Long diskMBpsReadWrite;
    private double diskCost;
    private String costOptimization;
}
