package com.example.azure.Azure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "azure")
@Data
public class AzureProperties {
    private String clientId;
    private String clientSecret;
    private String tenantId;
}
