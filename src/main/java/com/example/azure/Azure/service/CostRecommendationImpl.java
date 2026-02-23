package com.example.azure.Azure.service;

import com.example.azure.Azure.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
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
        double projectedMonthlyCost = projectFullMonthCost(vm.getVmCost());
        double avgCpu = vm.getAvgCpu();
        double peakCpu = vm.getPeakCpu();

        // ===============================
        // 1️⃣ STOPPED BUT STILL BILLED
        // ===============================
        if (powerState.contains("stopped")) {
            recommendations.add("Deallocate VM '" + vm.getInstanceName() + "' to save approximately $"
                            + format(projectedMonthlyCost) + " per month.");
        }

        // ===============================
        // 2️⃣ DEALLOCATED
        // ===============================
        if (powerState.contains("deallocated")) {
            recommendations.add("VM '" + vm.getInstanceName() + "' is already deallocated. No compute cost savings available.");
        }

        // ===============================
        // 3️⃣ RUNNING BUT IDLE (<5% CPU)
        // ===============================
        if (powerState.contains("running") && avgCpu < 5) {
            double estimatedSavings = projectedMonthlyCost * 0.60;
            recommendations.add("Downsize or schedule shutdown for VM '" + vm.getInstanceName() + "' to save approximately $"
                            + format(estimatedSavings) + " per month.");
        }

        // ===============================
        // 4️⃣ UNDERUTILIZED
        // ===============================
        if (powerState.contains("running") && avgCpu < 10 && peakCpu < 30) {
            double estimatedSavings = projectedMonthlyCost * 0.40;
            recommendations.add("Resize VM '" + vm.getInstanceName() + "' to a smaller SKU and save approximately $"
                            + format(estimatedSavings) + " per month.");
        }

        // ===============================
        // 5️⃣ HIGH COST + LOW UTILIZATION
        // ===============================
        if (powerState.contains("running") && projectedMonthlyCost > 300 && avgCpu < 20) {
            double estimatedSavings = projectedMonthlyCost * 0.30;
            recommendations.add("Optimize VM '" + vm.getInstanceName() + "' to reduce approximately $"
                            + format(estimatedSavings) + " per month from current $" + format(projectedMonthlyCost)
                            + " monthly spend.");
        }
        return recommendations;
    }

    private double projectFullMonthCost(double monthToDateCost) {
        if (monthToDateCost <= 0) {
            return 0;
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        int currentDay = today.getDayOfMonth();
        int totalDaysInMonth = java.time.YearMonth.from(today).lengthOfMonth();
        if (currentDay == 0) {
            return monthToDateCost;
        }
        //Today = 15th
        //MTD cost = $150
        //Month = 30 days
        //(150 / 15) * 30 = $300

        double projectedCost = (monthToDateCost / currentDay) * totalDaysInMonth;
        return Math.round(projectedCost);
    }

    private String format(double value) {
        return String.format("%.2f", value);
    }

    public List<String> generateSqlCostRecommendations(AzureSqlServiceDto db) {
        List<String> recommendations = new ArrayList<>();
        String status = db.getStatus() != null ? db.getStatus().toLowerCase() : "unknown";
        String edition = db.getEdition() != null ? db.getEdition().toLowerCase() : "unknown";
        String sku = db.getSku() != null ? db.getSku().toLowerCase() : "";
        double cost = Math.max(projectFullMonthCost(db.getDatabaseCost()), 0.0);

        // ================================
        // 1️⃣ Paused / Offline Database
        // ================================
        if (status.contains("paused") || status.contains("offline")) {
            recommendations.add("Delete SQL Database '" + db.getDatabaseName() + "' to save approximately $"
                            + format(cost) + " per month.");
        }

        // ================================
        // 2️⃣ Business Critical Tier
        // ================================
        if (sku.contains("bc") || edition.contains("businesscritical")) {
            double estimatedSavings = cost * 0.35; // approx BC → GP difference
            recommendations.add("Downgrade SQL Database '" + db.getDatabaseName()
                            + "' from Business Critical to General Purpose " + "to save approximately $"
                            + format(estimatedSavings) + " per month.");
        }

        // ================================
        // 3️⃣ Premium Tier
        // ================================
        if (edition.contains("premium")) {
            double estimatedSavings = cost * 0.30;
            recommendations.add("Resize Premium SQL Database '" + db.getDatabaseName()
                            + "' to lower tier and save approximately $"+ format(estimatedSavings) + " per month."
            );
        }

        // ================================
        // 4️⃣ Standalone (Elastic Pool Opportunity)
        // ================================
        if (db.getElasticPoolName() == null && cost > 100) {
            double estimatedSavings = cost * 0.25;
            recommendations.add("Move SQL Database '" + db.getDatabaseName() + "' to Elastic Pool to save approximately $"
                            + format(estimatedSavings) + " per month.");
        }

        return recommendations;
    }

    public List<String> generateVnetCostRecommendations(AzureVirtualNetworksDto vnet) {

        List<String> recommendations = new ArrayList<>();

        // =====================================
        // 1️⃣ DDoS Protection Enabled
        // =====================================
        if (Boolean.TRUE.equals(vnet.isEnableDdosProtection())) {

            recommendations.add(
                    "Virtual Network '" + vnet.getNetworkName()
                            + "' has DDoS Protection enabled. "
                            + "Azure DDoS Protection Standard incurs additional charges. "
                            + "Ensure this VNet supports internet-facing production workloads "
                            + "to justify the cost."
            );
        }

        // =====================================
        // 2️⃣ No Subnets (Potentially Unused)
        // =====================================
        if (vnet.getSubnets() == null || vnet.getSubnets().isBlank()) {

            recommendations.add(
                    "Virtual Network '" + vnet.getNetworkName()
                            + "' has no subnets configured. "
                            + "If the VNet is not in use, consider removing it to reduce "
                            + "management complexity and dependent resource costs."
            );
        }

        // =====================================
        // 3️⃣ Custom DNS Configured
        // =====================================
        if (vnet.getDnsServerIps() != null && !vnet.getDnsServerIps().isEmpty()) {

            recommendations.add(
                    "Virtual Network '" + vnet.getNetworkName()
                            + "' is configured with custom DNS servers. "
                            + "Verify associated DNS infrastructure costs and ensure "
                            + "they are actively required."
            );
        }

        // =====================================
        // 4️⃣ VM Protection Enabled
        // =====================================
        if (Boolean.TRUE.equals(vnet.isEnableVmProtection())) {

            recommendations.add(
                    "Virtual Network '" + vnet.getNetworkName()
                            + "' has VM protection enabled. "
                            + "Review security configuration to confirm it aligns "
                            + "with workload requirements and cost considerations."
            );
        }

        // =====================================
        // 5️⃣ DDoS Plan Attached
        // =====================================
        if (vnet.getDdosProtectionPlanId() != null) {

            recommendations.add(
                    "Virtual Network '" + vnet.getNetworkName()
                            + "' is associated with a DDoS Protection Plan. "
                            + "Confirm the plan is required and shared effectively "
                            + "across multiple VNets to optimize cost."
            );
        }

        return recommendations;
    }

    public List<String> generateStorageCostRecommendations(AzureStorageAccountDto storage) {

        List<String> recommendations = new ArrayList<>();

        double mtdCost = Math.max(storage.getStorageAccountCost(), 0.0);
        double projectedCost = projectFullMonthCost(mtdCost);

        String sku = storage.getSku() != null
                ? storage.getSku().toLowerCase()
                : "";

        String accessTier = storage.getAccessTier() != null
                ? storage.getAccessTier().toLowerCase()
                : "";

        boolean noBlobs = storage.getBlobContainers() == null
                || storage.getBlobContainers().isEmpty();

        boolean noQueues = storage.getQueueNames() == null
                || storage.getQueueNames().isEmpty();


        // =====================================
        // 1️⃣ Unused Storage Account
        // =====================================
        if (noBlobs && noQueues && mtdCost > 0) {

            recommendations.add(
                    "Storage Account '" + storage.getStorageAccountName()
                            + "' appears unused. Projected monthly cost is approximately $"
                            + projectedCost
                            + ". If not required, deleting the account could eliminate this cost."
            );
        }

        // =====================================
        // 2️⃣ Premium SKU Review
        // =====================================
        if (sku.contains("premium") && mtdCost > 0) {

            recommendations.add(
                    "Storage Account '" + storage.getStorageAccountName()
                            + "' is using Premium SKU (" + storage.getSku()
                            + "). Projected monthly cost is approximately $"
                            + projectedCost
                            + ". Ensure workload requires high performance. "
                            + "Otherwise consider Standard tier for cost optimization."
            );
        }

        // =====================================
        // 3️⃣ Hot Tier Optimization
        // =====================================
        if ("hot".equals(accessTier) && mtdCost > 0) {

            recommendations.add(
                    "Storage Account '" + storage.getStorageAccountName()
                            + "' is configured with Hot access tier. "
                            + "Projected monthly cost is approximately $"
                            + projectedCost
                            + ". If data is infrequently accessed, consider Cool or Archive tier "
                            + "to reduce storage cost."
            );
        }

        // =====================================
        // 4️⃣ Geo-Redundant Replication Review
        // =====================================
        if (sku.contains("grs") && mtdCost > 0) {

            recommendations.add(
                    "Storage Account '" + storage.getStorageAccountName()
                            + "' is using geo-redundant replication ("
                            + storage.getSku()
                            + "). Projected monthly cost is approximately $"
                            + projectedCost
                            + ". Confirm cross-region redundancy is required. "
                            + "If not, evaluate locally redundant storage (LRS) to optimize cost."
            );
        }

        // =====================================
        // 5️⃣ General Cost Awareness
        // =====================================
        if (mtdCost > 0) {

            recommendations.add(
                    "Storage Account '" + storage.getStorageAccountName()
                            + "' has a projected monthly cost of approximately $"
                            + projectedCost
                            + ". Review lifecycle policies, access tiers, and replication settings "
                            + "to ensure optimal cost efficiency."
            );
        }
        return recommendations;
    }

    public List<String> generateDiskCostRecommendations(AzureDiskDto disk) {

        List<String> recommendations = new ArrayList<>();
        double mtdCost = Math.max(disk.getDiskCost(), 0.0);
        double projectedCost = projectFullMonthCost(mtdCost);

        String sku = disk.getSku() != null ? disk.getSku().toLowerCase() : "";
        String diskState = disk.getDiskState() != null ? disk.getDiskState().toLowerCase() : "";
        Map<String, String> attachedVms =
                disk.getAttachedVm() != null ? disk.getAttachedVm() : new HashMap<>();

        boolean isUnattached =
                diskState.contains("unattached") ||
                        attachedVms.isEmpty() ||
                        attachedVms.containsKey("Not Attached");


        // =====================================
        // 1️⃣ Unattached Disk
        // =====================================
        if (isUnattached && mtdCost > 0) {

            recommendations.add(
                    "Disk '" + disk.getDiskName()
                            + "' is not attached to any virtual machine. "
                            + "Projected monthly cost is approximately $"
                            + projectedCost
                            + ". If no longer required, deleting it could eliminate this cost."
            );

            return recommendations;
        }

        // =====================================
        // 2️⃣ Premium / Ultra Tier
        // =====================================
        if ((sku.contains("premium") || sku.contains("ultra")) && mtdCost > 0) {

            recommendations.add(
                    "Disk '" + disk.getDiskName()
                            + "' is using high-performance tier (" + disk.getSku()
                            + "). Projected monthly cost is approximately $"
                            + projectedCost
                            + ". Validate performance requirements and consider Standard SSD if appropriate."
            );
        }

        // =====================================
        // 3️⃣ Large Disk
        // =====================================
        if (disk.getDiskSizeGb() != null && disk.getDiskSizeGb() > 512 && mtdCost > 0) {

            recommendations.add(
                    "Disk '" + disk.getDiskName()
                            + "' is provisioned with "
                            + disk.getDiskSizeGb()
                            + " GB. Projected monthly cost is approximately $"
                            + projectedCost
                            + ". Review utilization and resize if over-provisioned."
            );
        }

        // =====================================
        // 2️⃣ Attached to Stopped VM(s)
        // =====================================
        attachedVms.forEach((vmName, vmState) -> {

            if (vmState != null && vmState.toLowerCase().contains("stopped")) {

                recommendations.add(
                        "Disk '" + disk.getDiskName()
                                + "' is attached to stopped VM '" + vmName
                                + "'. Storage charges continue regardless of VM power state."
                );
            }
        });

        // =====================================
        // 3️⃣ Premium / Ultra Tier Review
        // =====================================
        if (sku.contains("premium") || sku.contains("ultra")) {

            recommendations.add(
                    "Disk '" + disk.getDiskName()
                            + "' is provisioned with high-performance tier ("
                            + disk.getSku()
                            + "). Ensure workload requires this performance level. "
                            + "If not, evaluate Standard SSD for cost optimization."
            );
        }

        // =====================================
        // 4️⃣ Large Provisioned Size Review
        // =====================================
        if (disk.getDiskSizeGb() != null && disk.getDiskSizeGb() > 512) {

            recommendations.add(
                    "Disk '" + disk.getDiskName()
                            + "' is provisioned with large capacity ("
                            + disk.getDiskSizeGb() + " GB). "
                            + "Review actual utilization and consider resizing if over-provisioned."
            );
        }

        // =====================================
        // 5️⃣ Standard HDD Tier Review
        // =====================================
        if (sku.contains("standard") && sku.contains("hdd")) {

            recommendations.add(
                    "Disk '" + disk.getDiskName()
                            + "' is using Standard HDD tier. "
                            + "If performance-sensitive workload exists, confirm appropriate tier selection."
            );
        }

        return recommendations;
    }


    public List<String> generatePublicIpCostRecommendations(AzurePublicIpsDto ip) {

        List<String> recommendations = new ArrayList<>();

        double mtdCost = Math.max(ip.getPublicIpCost(), 0.0);
        double projectedCost = projectFullMonthCost(mtdCost);

        String allocationMethod = ip.getAllocationMethod() != null
                ? ip.getAllocationMethod().toLowerCase()
                : "";

        String skuType = ip.getPublicIpAddressType() != null
                ? ip.getPublicIpAddressType().toLowerCase()
                : "";

        // =====================================
        // 1️⃣ Unused Public IP (Full Saving Possible)
        // =====================================
        if ((ip.getIpAddress() == null || ip.getIpAddress().isEmpty()) && mtdCost > 0) {

            recommendations.add(
                    "Public IP '" + ip.getIpName()
                            + "' does not appear to be associated with an active resource. "
                            + "Projected monthly cost is approximately $"
                            + projectedCost
                            + ". If not required, deleting it could eliminate this cost."
            );
        }

        // =====================================
        // 2️⃣ Static Allocation Cost Review
        // =====================================
        if ("static".equals(allocationMethod) && mtdCost > 0) {

            recommendations.add(
                    "Public IP '" + ip.getIpName()
                            + "' is configured with Static allocation. "
                            + "Projected monthly cost is approximately $"
                            + projectedCost
                            + ". If static addressing is not required, "
                            + "consider Dynamic allocation to optimize cost."
            );
        }

        // =====================================
        // 3️⃣ Standard SKU Review
        // =====================================
        if (skuType.contains("standard") && mtdCost > 0) {

            recommendations.add(
                    "Public IP '" + ip.getIpName()
                            + "' is using Standard SKU. "
                            + "Projected monthly cost is approximately $"
                            + projectedCost
                            + ". Ensure advanced networking features are required "
                            + "to justify the higher cost compared to Basic SKU."
            );
        }

        // =====================================
        // 4️⃣ IPv6 Review
        // =====================================
        if ("ipv6".equalsIgnoreCase(ip.getPublicIpAddressVersion()) && mtdCost > 0) {

            recommendations.add(
                    "Public IP '" + ip.getIpName()
                            + "' is configured as IPv6. "
                            + "Projected monthly cost is approximately $"
                            + projectedCost
                            + ". Confirm IPv6 traffic requirement to ensure cost alignment."
            );
        }

        // =====================================
        // 5️⃣ General Cost Awareness
        // =====================================
        if (mtdCost > 0) {

            recommendations.add(
                    "Public IP '" + ip.getIpName()
                            + "' has a projected monthly cost of approximately $"
                            + projectedCost
                            + ". Review allocation method and SKU to ensure optimal cost efficiency."
            );
        }

        return recommendations;
    }

    public List<String> generateNatGatewayCostRecommendations(AzureNatGatewayDto nat) {

        List<String> recommendations = new ArrayList<>();

        double mtdCost = Math.max(nat.getNatGatewayCost(), 0.0);
        double projectedCost = projectFullMonthCost(mtdCost);

        boolean noSubnets = nat.getSubnetCount() == 0;
        boolean noPublicIps = nat.getPublicIpCount() == 0;

        // =====================================
        // 1️⃣ Unassociated NAT Gateway (Major Waste)
        // =====================================
        if ((noSubnets || noPublicIps) && mtdCost > 0) {

            recommendations.add(
                    "NAT Gateway '" + nat.getNatName()
                            + "' is not fully associated with active subnets or public IPs. "
                            + "Projected monthly cost is approximately $"
                            + projectedCost
                            + ". If unused, removing it could eliminate this cost."
            );
        }

        // =====================================
        // 2️⃣ No Subnet Attached
        // =====================================
        if (noSubnets && mtdCost > 0) {

            recommendations.add(
                    "NAT Gateway '" + nat.getNatName()
                            + "' is not attached to any subnet. "
                            + "Projected monthly cost is approximately $"
                            + projectedCost
                            + ". NAT Gateway incurs charges even when not routing traffic."
            );
        }

        // =====================================
        // 3️⃣ Multiple Public IPs Review
        // =====================================
        if (nat.getPublicIpCount() > 1 && mtdCost > 0) {

            recommendations.add(
                    "NAT Gateway '" + nat.getNatName()
                            + "' is associated with "
                            + nat.getPublicIpCount()
                            + " public IP addresses. "
                            + "Projected monthly cost is approximately $"
                            + projectedCost
                            + ". Ensure multiple outbound IPs are required."
            );
        }

        // =====================================
        // 4️⃣ Idle Timeout Review
        // =====================================
        if (nat.getIdleTimeoutInMinutes() != null
                && nat.getIdleTimeoutInMinutes() > 10
                && mtdCost > 0) {

            recommendations.add(
                    "NAT Gateway '" + nat.getNatName()
                            + "' has idle timeout configured to "
                            + nat.getIdleTimeoutInMinutes()
                            + " minutes. "
                            + "Projected monthly cost is approximately $"
                            + projectedCost
                            + ". Review configuration to ensure alignment with workload needs."
            );
        }

        // =====================================
        // 5️⃣ General Cost Awareness
        // =====================================
        if (mtdCost > 0) {

            recommendations.add(
                    "NAT Gateway '" + nat.getNatName()
                            + "' has a projected monthly cost of approximately $"
                            + projectedCost
                            + ". Review subnet associations and public IP configuration "
                            + "to ensure optimal cost efficiency."
            );
        }

        return recommendations;
    }

    public List<String> generateSnapshotCostRecommendations(AzureSnapshotDto snapshot) {

        List<String> recommendations = new ArrayList<>();

        double cost = Math.max(snapshot.getCost(), 0.0);
        String sku = snapshot.getSku() != null ? snapshot.getSku().toLowerCase() : "";
        int sizeGb = snapshot.getDiskSizeGb() != null ? snapshot.getDiskSizeGb() : 0;

        // Parse creation time
        OffsetDateTime creationTime = null;
        if (snapshot.getTimeCreated() != null && !snapshot.getTimeCreated().isEmpty()) {
            creationTime = OffsetDateTime.parse(snapshot.getTimeCreated());
        }

        // =====================================
        // 1️⃣ Old Snapshot (older than 90 days)
        // =====================================
        if (creationTime != null) {
            OffsetDateTime ninetyDaysAgo = OffsetDateTime.now().minusDays(90);
            if (creationTime.isBefore(ninetyDaysAgo)) {
                recommendations.add(
                        "Snapshot '" + snapshot.getSnapshotName() + "' was created on " + creationTime.toLocalDate() +
                                " and is older than 90 days. Consider deleting old snapshots to save storage cost."
                );
            }
        }

        // =====================================
        // 2️⃣ Unused Snapshot
        // =====================================
        if (sizeGb > 0 && cost > 0) {
            recommendations.add(
                    "Snapshot '" + snapshot.getSnapshotName() + "' is taking up " + sizeGb + " GB and has incurred $" + cost
                            + " in projected month-to-date charges. "
                            + "If it is no longer required, consider deleting it to save storage cost."
            );
        }

        // =====================================
        // 3️⃣ Premium / High Performance SKU
        // =====================================
        if (sku.contains("premium") && cost > 0) {
            recommendations.add(
                    "Snapshot '" + snapshot.getSnapshotName() + "' is using a Premium SKU (" + snapshot.getSku()
                            + ") with projected cost of $" + cost + ". "
                            + "Ensure high performance is required; otherwise, consider Standard tier for cost savings."
            );
        }

        // =====================================
        // 4️⃣ Large Snapshot Size
        // =====================================
        if (sizeGb > 512 && cost > 0) {
            recommendations.add(
                    "Snapshot '" + snapshot.getSnapshotName() + "' is large (" + sizeGb + " GB) with projected cost of $" + cost + ". "
                            + "Review if this size is necessary; resizing or deleting unnecessary snapshots can reduce cost."
            );
        }

        // =====================================
        // 5️⃣ General Cost Awareness
        // =====================================
        if (cost > 0) {
            recommendations.add(
                    "Snapshot '" + snapshot.getSnapshotName() + "' has projected cost of $" + cost + ". "
                            + "Consider reviewing lifecycle management and retention policies to optimize cost."
            );
        }

        return recommendations;
    }

    public List<String> generateLoadBalancerCostRecommendations(AzureLoadBalancerDto lb) {

        List<String> recommendations = new ArrayList<>();

        double cost = Math.max(lb.getCost(), 0.0);
        String sku = lb.getSku() != null ? lb.getSku().toLowerCase() : "";
        int frontendCount = lb.getFrontendIpCount();
        int backendCount = lb.getBackendPoolCount();
        int ruleCount = lb.getRuleCount();

        // =====================================
        // 1️⃣ High Cost Load Balancer
        // =====================================
        if (cost > 50) {
            recommendations.add(
                    "Load Balancer '" + lb.getLoadBalancerName() + "' has a projected cost of $" + cost + ". "
                            + "Review usage, SKU selection, and associated resources to optimize cost."
            );
        }

        // =====================================
        // 2️⃣ Premium SKU Review
        // =====================================
        if (sku.contains("standard") || sku.contains("premium")) {
            recommendations.add(
                    "Load Balancer '" + lb.getLoadBalancerName() + "' is using SKU (" + lb.getSku() + "). "
                            + "Ensure advanced features like zone redundancy or high availability are required to justify cost."
            );
        }

        // =====================================
        // 3️⃣ Unused Frontend / Backend Resources
        // =====================================
        if (frontendCount == 0 && backendCount == 0 && cost > 0) {
            recommendations.add(
                    "Load Balancer '" + lb.getLoadBalancerName() + "' has no frontends or backend pools but has incurred $" + cost
                            + " in projected cost. Consider deleting unused load balancer to save cost."
            );
        }

        // =====================================
        // 4️⃣ Many Rules / Probes
        // =====================================
        if (ruleCount > 10 && cost > 0) {
            recommendations.add(
                    "Load Balancer '" + lb.getLoadBalancerName() + "' has " + ruleCount + " rules configured. "
                            + "Evaluate if all rules are required; reducing unnecessary rules can optimize cost."
            );
        }

        // =====================================
        // 5️⃣ General Cost Awareness
        // =====================================
        if (cost > 0) {
            recommendations.add(
                    "Load Balancer '" + lb.getLoadBalancerName() + "' has projected cost of $" + cost + ". "
                            + "Review frontend/backends, SKU, and usage patterns to ensure cost efficiency."
            );
        }

        return recommendations;
    }
}
