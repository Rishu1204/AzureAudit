package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AzureApiManagementDto {
    private String name;
    private String id;
    private String resourceGroup;
    private String region;
    private String publisherEmail;
    private String publisherName;
    private String sku;
    private Integer capacity;
    private String gatewayUrl;
}
