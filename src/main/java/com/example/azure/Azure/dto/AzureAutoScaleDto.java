package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureAutoScaleDto {
    private String name;
    private String resourceGroup;
    private String region;
    private String targetResourceId;
    private boolean enabled;
    private int profileCount;
}
