package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AzurePostgreSqlDto {

    private String name;
    private String region;
    private String administratorLogin;
    private String version;
    private String sku;
    private Integer storageGb;
}
