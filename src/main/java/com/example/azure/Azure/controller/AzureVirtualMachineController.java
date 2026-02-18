package com.example.azure.Azure.controller;

import com.example.azure.Azure.dto.AzureInventoryResponse;
import com.example.azure.Azure.service.AzureVirtualMachineService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/azure")
@AllArgsConstructor
public class AzureVirtualMachineController {

    private final AzureVirtualMachineService azureVirtualMachineService;

    @GetMapping("/virtual-machines")
    public List<AzureInventoryResponse> getVirtualMachine() {
        return azureVirtualMachineService.fetchInventory();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() throws IOException {

        List<AzureInventoryResponse> response = azureVirtualMachineService.fetchInventory();
        byte[] excel = azureVirtualMachineService.exportInventoryToExcel(response);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=azure-inventory.xlsx")
                .header(HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excel);
    }

}
