package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureManagementGroupDto {
    private String id;
    private String name;
    private String displayName;
    private String parentId;
    private List<String> childManagementGroupIds;
    private List<String> subscriptionIds;
}
