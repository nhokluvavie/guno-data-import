package com.guno.etl.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectedOrderData {

    // Raw order data from each platform
    private List<ShopeeOrderDto> shopeeOrders = new ArrayList<>();
    private List<TikTokOrderDto> tiktokOrders = new ArrayList<>();
    private List<FacebookOrderDto> facebookOrders = new ArrayList<>();

    // Metadata
    private int totalOrderCount;
    private boolean hasShopeeData;
    private boolean hasTiktokData;
    private boolean hasFacebookData;

    /**
     * Add Shopee orders to collection
     */
    public void addShopeeOrders(List<ShopeeOrderDto> orders) {
        if (orders != null && !orders.isEmpty()) {
            this.shopeeOrders.addAll(orders);
            this.hasShopeeData = true;
            updateTotalCount();
        }
    }

    /**
     * Add TikTok orders to collection
     */
    public void addTiktokOrders(List<TikTokOrderDto> orders) {
        if (orders != null && !orders.isEmpty()) {
            this.tiktokOrders.addAll(orders);
            this.hasTiktokData = true;
            updateTotalCount();
        }
    }

    /**
     * Add Facebook orders to collection
     */
    public void addFacebookOrders(List<FacebookOrderDto> orders) {
        if (orders != null && !orders.isEmpty()) {
            this.facebookOrders.addAll(orders);
            this.hasFacebookData = true;
            updateTotalCount();
        }
    }

    /**
     * Update total order count
     */
    private void updateTotalCount() {
        this.totalOrderCount = shopeeOrders.size() + tiktokOrders.size() + facebookOrders.size();
    }

    /**
     * Check if collection has any data
     */
    public boolean hasAnyData() {
        return totalOrderCount > 0;
    }

    /**
     * Get breakdown summary
     */
    public String getDataSummary() {
        return String.format("Total: %d orders (Shopee: %d, TikTok: %d, Facebook: %d)",
                totalOrderCount,
                shopeeOrders.size(),
                tiktokOrders.size(),
                facebookOrders.size());
    }

    /**
     * Clear all collected data
     */
    public void clear() {
        shopeeOrders.clear();
        tiktokOrders.clear();
        facebookOrders.clear();
        totalOrderCount = 0;
        hasShopeeData = false;
        hasTiktokData = false;
        hasFacebookData = false;
    }

    /**
     * Get memory usage estimate in MB
     */
    public double getEstimatedMemoryUsageMB() {
        // Rough estimate: ~2KB per order on average
        return (totalOrderCount * 2.0) / 1024.0;
    }

    /**
     * Check if memory usage is within acceptable limits
     */
    public boolean isMemoryUsageAcceptable(double maxMemoryMB) {
        return getEstimatedMemoryUsageMB() <= maxMemoryMB;
    }
}