package com.example.azure.Azure.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AzureRedisDto {

    private String name;
    private String id;
    private String resourceGroup;
    private String region;
    private String sku;
    private String redisVersion;
    private Boolean enableNonSslPort;
}
