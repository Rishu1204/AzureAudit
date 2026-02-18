package com.example.azure.Azure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AzureSynapseWorkspaceDto {
    private String workspaceName;
    private String workspaceId;
    private String resourceGroup;
    private String region;
    private String sqlPoolName;
    private String sqlPoolEdition;
    private String sqlPoolStatus;
}
