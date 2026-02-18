package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureBlobContainerDto {
    private String storageAccountName;
    private String containerName;
    private String resourceGroup;
    private String region;
    private String publicAccessType;
}
