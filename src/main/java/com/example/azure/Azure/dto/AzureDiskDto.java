package com.example.azure.Azure.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AzureDiskDto {

    private String name;
    private String resourceGroup;
    private String region;
    private Integer sizeInGb;
    private String sku;
    private String osType;
    private Integer diskSizeGb;
    private String diskState;
    private Long diskIopsReadOnly;
    private Long diskIopsReadWrite;
    private Long diskMBpsReadOnly;
    private Long diskMBpsReadWrite;
    private double diskCost;
}
