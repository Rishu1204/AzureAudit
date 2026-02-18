package com.example.azure.Azure.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AzureCdnProfileDto {

    private String name;
    private String id;
    private String resourceGroup;
    private String region;
    private String sku;
}
