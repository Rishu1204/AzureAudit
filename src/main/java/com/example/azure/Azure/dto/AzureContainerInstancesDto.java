package com.example.azure.Azure.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureContainerInstancesDto {

    private String name;
    private String resourceGroup;
    private String region;
    private String osType;
    private String state;
    private String ipAddress;
    private String dnsName;
    private int containerCount;
    private String restartPolicy;
}
