package com.example.azure.Azure.dto;

import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.Topic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AzureServiceBusDto {
    private String name;
    private String id;
    private String resourceGroup;
    private String region;
    private String sku;
    private Integer capacity;
    private String status;
    private List<Queue> queue;
    private List<Topic> topics;
}
