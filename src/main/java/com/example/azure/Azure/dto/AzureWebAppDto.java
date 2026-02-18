package com.example.azure.Azure.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AzureWebAppDto {

    private String name;
    private String id;
    private String resourceGroup;
    private String region;
    private String defaultHostName;
    private String state;
    private Boolean httpsOnly;
}
