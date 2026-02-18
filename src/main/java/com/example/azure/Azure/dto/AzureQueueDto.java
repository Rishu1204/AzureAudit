package com.example.azure.Azure.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AzureQueueDto {
    private String storageAccountName;
    private String queueName;
    private Map<String, String> metadata;
}