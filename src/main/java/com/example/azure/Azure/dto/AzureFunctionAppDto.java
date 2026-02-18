package com.example.azure.Azure.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AzureFunctionAppDto {

    private String name;
    private String id;
    private String resourceGroup;
    private String region;
    private String state;
    private String runtime;
    private String defaultHostName;
}
