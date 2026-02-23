package com.example.azure.Azure.service;

import com.example.azure.Azure.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CostRecommendationImpl {

    public List<String> generateVmCostRecommendations(AzureVirtualMachineDto vm) {

        List<String> recommendations = new ArrayList<>();

        String powerState = vm.getPowerState() != null ? vm.getPowerState().toLowerCase() : "unknown";

        // ===============================
        // 1️⃣ STOPPED BUT STILL BILLED
        // ===============================
        if (powerState.contains("stopped")) {
            recommendations.add(
                    "VM '" + vm.getName() + "' is stopped but still allocated. "
                            + "Compute charges may still apply. Consider deallocating "
                            + "the VM to stop compute billing."
            );
        }

        // ===============================
        // 2️⃣ DEALLOCATED (Only Storage Cost)
        // ===============================
        if (powerState.contains("deallocated")) {

            recommendations.add(
                    "VM '" + vm.getName() + "' is deallocated. Only storage charges apply. "
                            + "If no longer required, consider deleting the VM and attached disks "
                            + "to eliminate storage cost."
            );
        }

        // ===============================
        // 3️⃣ RUNNING BUT IDLE
        // ===============================
        if (powerState.contains("running")
                && vm.getAvgCpu() < 5) {

            recommendations.add(
                    "VM '" + vm.getName() + "' is running with very low CPU utilization ("
                            + vm.getAvgCpu() + "%). Consider auto-shutdown schedules "
                            + "or downsizing to reduce compute costs."
            );
        }

        // ===============================
        // 4️⃣ UNDERUTILIZED
        // ===============================
        if (powerState.contains("running")
                && vm.getAvgCpu() < 10
                && vm.getPeakCpu() < 30) {

            recommendations.add(
                    "VM '" + vm.getName() + "' is underutilized. "
                            + "Evaluate resizing to a smaller SKU or burstable VM "
                            + "to optimize cost."
            );
        }

        // ===============================
        // 5️⃣ HIGH COST + LOW UTILIZATION
        // ===============================
        if (powerState.contains("running")
                && vm.getVmCost() > 300
                && vm.getAvgCpu() < 20) {

            recommendations.add(
                    "VM '" + vm.getName() + "' has high monthly cost ($"
                            + vm.getVmCost()
                            + ") with low utilization. Consider rightsizing "
                            + "or Reserved Instances for cost optimization."
            );
        }


        return recommendations;
    }


    public List<String> generateSqlCostRecommendations(AzureSqlServiceDto db) {

        List<String> recommendations = new ArrayList<>();

        String status = db.getStatus() != null
                ? db.getStatus().toLowerCase()
                : "unknown";

        String edition = db.getEdition() != null
                ? db.getEdition().toLowerCase()
                : "unknown";

        double cost = Math.max(db.getDatabaseCost(), 0.0);

        // ================================
        // 1️⃣ Paused / Offline Database
        // ================================
        if (status.contains("paused") || status.contains("offline")) {

            recommendations.add(
                    "SQL Database '" + db.getDatabaseName()
                            + "' is in " + db.getStatus()
                            + " state. Storage charges may still apply. "
                            + "If not required, consider deleting the database "
                            + "to eliminate unnecessary costs."
            );
        }

        // ================================
        // 2️⃣ High Monthly Cost
        // ================================
//        if (cost > 500) {
//
//            recommendations.add(
//                    "SQL Database '" + db.getDatabaseName()
//                            + "' has high monthly cost ($" + cost
//                            + "). Review vCore/DTU utilization and consider "
//                            + "rightsizing or purchasing Reserved Capacity."
//            );
//        }

        // ================================
        // 3️⃣ Premium / Business Critical Tier
        // ================================
        if (edition.contains("premium")
                || edition.contains("businesscritical")) {

            recommendations.add(
                    "SQL Database '" + db.getDatabaseName()
                            + "' is using high-tier SKU (" + db.getEdition()
                            + "). Ensure workload requires high IOPS and HA. "
                            + "Otherwise consider General Purpose tier for cost savings."
            );
        }

        // ================================
        // 4️⃣ Standalone Database
        // ================================
        if (db.getElasticPoolName() == null) {

            recommendations.add(
                    "SQL Database '" + db.getDatabaseName()
                            + "' is deployed as standalone. "
                            + "If workload is intermittent, evaluate Elastic Pool "
                            + "to optimize compute cost."
            );
        }

        return recommendations;
    }

    public List<String> generateVnetCostRecommendations(AzureVirtualNetworksDto vnet) {

        List<String> recommendations = new ArrayList<>();

        // =====================================
        // 1️⃣ DDoS Protection Plan Cost Review
        // =====================================
        if (Boolean.TRUE.equals(vnet.isEnableDdosProtection())) {

            recommendations.add(
                    "Virtual Network '" + vnet.getNetworkName()
                            + "' has DDoS Protection enabled. Azure DDoS Protection "
                            + "incurs additional charges. Ensure this VNet hosts "
                            + "internet-facing production workloads to justify the cost."
            );
        }

        // =====================================
        // 2️⃣ No Subnets Defined (Unused VNet)
        // =====================================
        if (vnet.getSubnets() == null || vnet.getSubnets().isEmpty()) {

            recommendations.add(
                    "Virtual Network '" + vnet.getNetworkName()
                            + "' has no subnets configured. "
                            + "If unused, consider removing the VNet to reduce "
                            + "management overhead and associated dependent resource costs."
            );
        }

        // =====================================
        // 3️⃣ Custom DNS Configured
        // =====================================
        if (vnet.getDnsServerIps() != null && !vnet.getDnsServerIps().isEmpty()) {

            recommendations.add(
                    "Virtual Network '" + vnet.getNetworkName()
                            + "' is using custom DNS servers. "
                            + "Ensure associated DNS infrastructure cost is justified "
                            + "and actively used."
            );
        }

        // =====================================
        // 4️⃣ VM Protection Enabled
        // =====================================
        if (Boolean.TRUE.equals(vnet.isEnableVmProtection())) {

            recommendations.add(
                    "Virtual Network '" + vnet.getNetworkName()
                            + "' has VM protection enabled. "
                            + "Review if enhanced security configuration aligns "
                            + "with workload criticality."
            );
        }

        return recommendations;
    }

    public List<String> generateStorageCostRecommendations(AzureStorageAccountDto storage) {

        List<String> recommendations = new ArrayList<>();

        double cost = Math.max(storage.getStorageAccountCost(), 0.0);


        String sku = storage.getSku() != null
                ? storage.getSku().toLowerCase()
                : "";

        String accessTier = storage.getAccessTier() != null
                ? storage.getAccessTier().toLowerCase()
                : "";

        String kind = storage.getKind() != null
                ? storage.getKind().toLowerCase()
                : "";

        // =====================================
        // 1️⃣ High Monthly Cost
        // =====================================
//        if (cost > 300) {
//
//            recommendations.add(
//                    "Storage Account '" + storage.getName()
//                            + "' has high monthly cost ($" + cost
//                            + "). Review data growth, lifecycle policies, "
//                            + "and replication configuration for optimization opportunities."
//            );
//        }

        // =====================================
        // 2️⃣ Premium SKU Review
        // =====================================
        if (sku.contains("premium")) {

            recommendations.add(
                    "Storage Account '" + storage.getName()
                            + "' is using Premium SKU (" + storage.getSku()
                            + "). Ensure workload requires high IOPS or low latency. "
                            + "Otherwise consider Standard tier to reduce cost."
            );
        }

        // =====================================
        // 3️⃣ Hot Access Tier Optimization
        // =====================================
        if ("hot".equals(accessTier)) {

            recommendations.add(
                    "Storage Account '" + storage.getName()
                            + "' is using Hot access tier. "
                            + "If data is infrequently accessed, consider Cool or Archive tier "
                            + "to reduce storage costs."
            );
        }

        // =====================================
        // 4️⃣ No Blob Containers and No Queues
        // =====================================
        boolean noBlobs = storage.getBlobContainers() == null
                || storage.getBlobContainers().isEmpty();

        boolean noQueues = storage.getQueueNames() == null
                || storage.getQueueNames().isEmpty();

        if (noBlobs && noQueues) {

            recommendations.add(
                    "Storage Account '" + storage.getName()
                            + "' does not contain any blob containers or queues. "
                            + "If unused, consider removing the account to eliminate "
                            + "associated storage and management overhead costs."
            );
        }

        // =====================================
        // 5️⃣ Legacy Storage Kind (v1)
        // =====================================
        if (kind.contains("storage")) {

            recommendations.add(
                    "Storage Account '" + storage.getName()
                            + "' may be using legacy deployment model (" + storage.getKind()
                            + "). Consider upgrading to StorageV2 for better cost "
                            + "optimization features like lifecycle management."
            );
        }

        return recommendations;
    }

    public List<String> generateDiskCostRecommendations(AzureDiskDto disk) {

        List<String> recommendations = new ArrayList<>();

        double cost = Math.max(disk.getDiskCost(), 0.0);
        String sku = disk.getSku() != null ? disk.getSku().toLowerCase() : "";
        String diskState = disk.getDiskState() != null ? disk.getDiskState().toLowerCase() : "";

        Map<String, String> attachedVms =
                disk.getAttachedVm() != null ? disk.getAttachedVm() : new HashMap<>();

        // =====================================
        // 1️⃣ Disk Completely Unattached
        // =====================================
        boolean isUnattached =
                diskState.contains("unattached") ||
                        attachedVms.isEmpty() ||
                        attachedVms.containsKey("Not Attached");

        if (isUnattached) {

            recommendations.add(
                    "Disk '" + disk.getName() +
                            "' is not attached to any VM and continues to incur storage charges. " +
                            "If not required, consider deleting it to reduce cost."
            );

            // If unattached, no need to evaluate VM states
            return recommendations;
        }

        // =====================================
        // 2️⃣ Attached to Stopped VM(s)
        // =====================================
        attachedVms.forEach((vmName, vmState) -> {

            if (vmState != null &&
                    vmState.toLowerCase().contains("stopped")) {

                recommendations.add(
                        "Disk '" + disk.getName() +
                                "' is attached to stopped VM '" + vmName +
                                "'. Storage charges continue even when VM is stopped."
                );
            }
        });

        // =====================================
        // 3️⃣ High Monthly Cost
//        // =====================================
//        if (cost > 150) {
//
//            recommendations.add(
//                    "Disk '" + disk.getName() +
//                            "' has relatively high monthly cost ($" + cost +
//                            "). Review disk size and tier for optimization."
//            );
//        }

        // =====================================
        // 4️⃣ Premium / Ultra Tier Review
        // =====================================
        if (sku.contains("premium") || sku.contains("ultra")) {

            recommendations.add(
                    "Disk '" + disk.getName() +
                            "' is using high-performance tier (" + disk.getSku() +
                            "). Validate workload requires this performance level."
            );
        }

        return recommendations;
    }


    public List<String> generatePublicIpCostRecommendations(AzurePublicIpsDto ip) {

        List<String> recommendations = new ArrayList<>();

        double cost = Math.max(ip.getPublicIpCost(), 0.0);


        String allocationMethod = ip.getAllocationMethod() != null
                ? ip.getAllocationMethod().toLowerCase()
                : "";

        String skuType = ip.getPublicIpAddressType() != null
                ? ip.getPublicIpAddressType().toLowerCase()
                : "";

        // =====================================
        // 1️⃣ Unused Public IP (Major Waste)
        // =====================================
        if (ip.getIpAddress() == null || ip.getIpAddress().isEmpty()) {

            recommendations.add(
                    "Public IP '" + ip.getName()
                            + "' does not appear to be associated with an active resource. "
                            + "Unattached Public IPs continue to incur charges. "
                            + "Consider removing if not required."
            );
        }

        // =====================================
        // 2️⃣ High Cost Public IP
        // =====================================
        if (cost > 20) {

            recommendations.add(
                    "Public IP '" + ip.getName()
                            + "' has elevated monthly cost ($" + cost
                            + "). Review SKU type and usage necessity."
            );
        }

        // =====================================
        // 3️⃣ Static Allocation Review
        // =====================================
        if ("static".equals(allocationMethod)) {

            recommendations.add(
                    "Public IP '" + ip.getName()
                            + "' is configured with Static allocation. "
                            + "If static addressing is not required, consider "
                            + "Dynamic allocation to optimize cost."
            );
        }

        // =====================================
        // 4️⃣ Standard SKU Review
        // =====================================
        if (skuType.contains("standard")) {

            recommendations.add(
                    "Public IP '" + ip.getName()
                            + "' is using Standard SKU. Ensure advanced features "
                            + "such as zone redundancy or enhanced security are required "
                            + "to justify higher cost compared to Basic SKU."
            );
        }

        // =====================================
        // 5️⃣ IPv6 Review
        // =====================================
        if ("ipv6".equalsIgnoreCase(ip.getPublicIpAddressVersion())) {

            recommendations.add(
                    "Public IP '" + ip.getName()
                            + "' is IPv6. Confirm IPv6 traffic requirement "
                            + "to ensure cost alignment."
            );
        }

        return recommendations;
    }
}
