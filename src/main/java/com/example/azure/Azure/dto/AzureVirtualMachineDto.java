package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class AzureVirtualMachineDto {
    private String name;
    private String resourceGroup;
    private String region;
    private String size;
    private String osType;
    private String powerState;
    private String networkInterfaces;
    private int diskSize;
    private String publicIpAddress;
    private String priority;
    private String type;
    private double avgCpu;
    private double avgMemory;
    private double peakCpu;
    private double peakMemory;
    private double vmCost;
    private List<String> costOptimization;

}
