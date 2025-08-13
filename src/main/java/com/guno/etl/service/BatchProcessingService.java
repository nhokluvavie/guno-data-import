package com.guno.etl.service;

import com.guno.etl.dto.*;
import com.guno.etl.entity.*;
import com.guno.etl.repository.*;
import com.guno.etl.util.HashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BatchProcessingService {

    // All repositories
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private GeographyInfoRepository geographyInfoRepository;
    @Autowired private PaymentInfoRepository paymentInfoRepository;
    @Autowired private ShippingInfoRepository shippingInfoRepository;
    @Autowired private ProcessingDateInfoRepository processingDateInfoRepository;

    @Value("${etl.database.batch-size:1000}")
    private int batchSize;

    /**
     * MAIN METHOD: Process all collected orders using bulk operations
     */
    @Transactional
    public BatchProcessingResult processBatchData(CollectedOrderData collectedData) {
        log.info("🔄 Starting BATCH processing for {} total orders", collectedData.getTotalOrderCount());

        BatchProcessingResult result = new BatchProcessingResult();
        result.setStartTime(LocalDateTime.now());

        try {
            // Process each entity type in bulk
            result.setCustomersProcessed(processCustomers(collectedData));
            result.setOrdersProcessed(processOrders(collectedData));
            result.setItemsProcessed(processOrderItems(collectedData));
            result.setProductsProcessed(processProducts(collectedData));
            result.setGeographyProcessed(processGeography(collectedData));
            result.setPaymentsProcessed(processPayments(collectedData));
            result.setShippingProcessed(processShipping(collectedData));
            result.setDateInfoProcessed(processDateInfo(collectedData));

            result.setSuccess(true);
            result.setEndTime(LocalDateTime.now());

            log.info("✅ BATCH processing completed successfully!");
            logSummary(result);

            return result;

        } catch (Exception e) {
            log.error("❌ BATCH processing failed: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setEndTime(LocalDateTime.now());
            return result;
        }
    }

    // ===== CUSTOMERS =====

    private int processCustomers(CollectedOrderData data) {
        log.info("👥 Processing customers...");

        Map<String, Customer> allCustomers = new HashMap<>();

        // Collect customers from all platforms
        data.getShopeeOrders().forEach(order -> addShopeeCustomer(allCustomers, order));
        data.getTiktokOrders().forEach(order -> addTiktokCustomer(allCustomers, order));
        data.getFacebookOrders().forEach(order -> addFacebookCustomer(allCustomers, order));

        return bulkUpsertCustomers(allCustomers);
    }

    private void addShopeeCustomer(Map<String, Customer> customerMap, ShopeeOrderDto order) {
        try {
            String phone = order.getData().getRecipientAddress().getPhone();
            if (phone == null || phone.isEmpty()) phone = "Unknown";

            String phoneHash = HashUtil.hashPhone(phone);
            String customerId = HashUtil.generateCustomerId("SHOPEE", phoneHash);

            if (!customerMap.containsKey(customerId)) {
                Customer customer = Customer.builder()
                        .customerId(customerId)
                        .phoneHash(phoneHash)
                        .acquisitionChannel("SHOPEE")
                        .lastOrderDate(java.time.LocalDateTime.now())
                        .totalOrders(1)
                        .totalSpent(order.getData().getTotalAmount() != null ? order.getData().getTotalAmount().doubleValue() : 0.0)
                        .build();

                if (customer.getTotalOrders() > 0 && customer.getTotalSpent() > 0) {
                    customer.setAverageOrderValue(customer.getTotalSpent() / customer.getTotalOrders());
                }

                customerMap.put(customerId, customer);
            }
        } catch (Exception e) {
            log.warn("Failed to extract customer from Shopee order {}: {}", order.getOrderId(), e.getMessage());
        }
    }

    private void addTiktokCustomer(Map<String, Customer> customerMap, TikTokOrderDto order) {
        try {
            // TikTok có customerId trực tiếp từ userId
            String customerId = order.getData().getUserId();
            if (customerId == null || customerId.isEmpty()) {
                // Fallback: generate từ phone nếu không có userId
                String phone = order.getData().getRecipientAddress().getPhoneNumber();
                if (phone == null || phone.isEmpty()) phone = "Unknown";
                String phoneHash = HashUtil.hashPhone(phone);
                customerId = HashUtil.generateCustomerId("TIKTOK", phoneHash);
            }

            if (!customerMap.containsKey(customerId)) {
                String phone = order.getData().getRecipientAddress().getPhoneNumber();
                String phoneHash = HashUtil.hashPhone(phone != null ? phone : "Unknown");

                Customer customer = Customer.builder()
                        .customerId(customerId)
                        .phoneHash(phoneHash)
                        .acquisitionChannel("TIKTOK")
                        .lastOrderDate(java.time.LocalDateTime.now())
                        .totalOrders(1)
                        .totalSpent(order.getData().getPayment().getTotalAmount() != null ? order.getData().getPayment().getTotalAmount() : 0.0)
                        .build();

                if (customer.getTotalOrders() > 0 && customer.getTotalSpent() > 0) {
                    customer.setAverageOrderValue(customer.getTotalSpent() / customer.getTotalOrders());
                }

                customerMap.put(customerId, customer);
            }
        } catch (Exception e) {
            log.warn("Failed to extract customer from TikTok order {}: {}", order.getOrderId(), e.getMessage());
        }
    }

    private void addFacebookCustomer(Map<String, Customer> customerMap, FacebookOrderDto order) {
        try {
            // Facebook có customerId trực tiếp
            String customerId = order.getData().getCustomer().getId();

            if (!customerMap.containsKey(customerId)) {
                String phone = getFirstPhone(order.getData().getCustomer().getPhoneNumbers());
                String phoneHash = HashUtil.hashPhone(phone != null ? phone : "Unknown");

                Customer customer = Customer.builder()
                        .customerId(customerId)
                        .phoneHash(phoneHash)
                        .acquisitionChannel("FACEBOOK")
                        .lastOrderDate(java.time.LocalDateTime.now())
                        .totalOrders(order.getData().getCustomer().getOrderCount() != null ? order.getData().getCustomer().getOrderCount() : 1)
                        .totalSpent(order.getData().getCustomer().getPurchasedAmount() != null ? order.getData().getCustomer().getPurchasedAmount().doubleValue() : 0.0)
                        .build();

                if (customer.getTotalOrders() > 0 && customer.getTotalSpent() > 0) {
                    customer.setAverageOrderValue(customer.getTotalSpent() / customer.getTotalOrders());
                }

                customerMap.put(customerId, customer);
            }
        } catch (Exception e) {
            log.warn("Failed to extract customer from Facebook order {}: {}", order.getOrderId(), e.getMessage());
        }
    }

    // ===== ORDERS =====

    private int processOrders(CollectedOrderData data) {
        log.info("📦 Processing orders...");

        List<Order> allOrders = new ArrayList<>();

        // Collect orders from all platforms
        data.getShopeeOrders().forEach(order -> allOrders.add(createShopeeOrder(order)));
        data.getTiktokOrders().forEach(order -> allOrders.add(createTiktokOrder(order)));
        data.getFacebookOrders().forEach(order -> allOrders.add(createFacebookOrder(order)));

        return bulkUpsertOrders(allOrders);
    }

    private Order createShopeeOrder(ShopeeOrderDto orderDto) {
        String phone = orderDto.getData().getRecipientAddress().getPhone();
        if (phone == null || phone.isEmpty()) phone = "Unknown";
        String phoneHash = HashUtil.hashPhone(phone);
        String customerId = HashUtil.generateCustomerId("SHOPEE", phoneHash);

        return Order.builder()
                .orderId(orderDto.getOrderId())
                .customerId(customerId)
                .shopId(String.valueOf(orderDto.getShopId()))
                .orderCount(1)
                .itemQuantity(orderDto.getData().getItemList().size())
                .totalItemsInOrder(orderDto.getData().getItemList().size())
                .grossRevenue(orderDto.getTotalAmount().doubleValue()) // Direct value
                .netRevenue((double) (orderDto.getTotalAmount() - orderDto.getData().getActualShippingFee())) // Simple subtraction
                .shippingFee(orderDto.getData().getActualShippingFee().doubleValue()) // Direct value
                .taxAmount(0.0) // Not provided in current format
                .discountAmount(0.0) // Calculate if needed
                .codAmount(orderDto.getData().getCod() ? orderDto.getTotalAmount().doubleValue() : 0.0) // Direct value
                .platformFee(0.0) // Not provided
                .sellerDiscount(0.0) // Not provided
                .platformDiscount(0.0) // Not provided
                .originalPrice(orderDto.getTotalAmount().doubleValue()) // Direct value
                .estimatedShippingFee(orderDto.getData().getEstimatedShippingFee().doubleValue()) // Direct value
                .actualShippingFee(orderDto.getData().getActualShippingFee().doubleValue()) // Direct value
                .shippingWeightGram(orderDto.getData().getOrderChargeableWeightGram())
                .daysToShip(orderDto.getData().getDaysToShip()) // Direct value, no multiplication
                .isDelivered("COMPLETED".equals(orderDto.getOrderStatus()))
                .isCancelled(false) // Would need to check status
                .isReturned(false) // Would need to check status
                .isCod(orderDto.getData().getCod())
                .isNewCustomer(true) // Will be updated later if needed
                .isRepeatCustomer(false)
                .isBulkOrder(orderDto.getData().getItemList().size() > 3)
                .isPromotionalOrder(false) // Check if any item has promotion
                .isSameDayDelivery(orderDto.getData().getDaysToShip() == 0)
                .orderToShipHours(orderDto.getData().getDaysToShip()) // Keep as days, no conversion to hours
                .shipToDeliveryHours(1) // Default estimate in days
                .totalFulfillmentHours(orderDto.getData().getDaysToShip() + 1) // Keep as days
                .customerOrderSequence(1) // Will be updated
                .customerLifetimeOrders(1) // Will be updated
                .customerLifetimeValue(orderDto.getTotalAmount().doubleValue()) // Direct value
                .daysSinceLastOrder(0)
                .promotionImpact(0.0)
                .adRevenue(0.0)
                .organicRevenue(orderDto.getTotalAmount().doubleValue()) // Direct value
                .aov(orderDto.getTotalAmount().doubleValue()) // Direct value
                .shippingCostRatio(orderDto.getData().getActualShippingFee().doubleValue() / orderDto.getTotalAmount().doubleValue()) // Keep this calculation as it's a ratio
                .createdAt(convertTimestamp(orderDto.getCreateTime()))
                .build();
    }

    private Order createTiktokOrder(TikTokOrderDto orderDto) {
        String customerId = orderDto.getData().getUserId();
        if (customerId == null || customerId.isEmpty()) {
            String phone = orderDto.getData().getRecipientAddress().getPhoneNumber();
            if (phone == null || phone.isEmpty()) phone = "Unknown";
            String phoneHash = HashUtil.hashPhone(phone);
            customerId = HashUtil.generateCustomerId("TIKTOK", phoneHash);
        }

        return Order.builder()
                .orderId(orderDto.getOrderId())
                .customerId(customerId)
                .platform("TIKTOK")
                .orderDate(convertTiktokTimestamp(orderDto.getCreateTime()))
                .orderStatus(orderDto.getData().getOrderStatus())
                .totalAmount(orderDto.getData().getPayment().getTotalAmount() != null ? orderDto.getData().getPayment().getTotalAmount() : 0.0)
                .createTime(convertTiktokTimestamp(orderDto.getCreateTime()))
                .updateTime(convertTiktokTimestamp(orderDto.getUpdateTime()))
                .build();
    }

    private Order createFacebookOrder(FacebookOrderDto orderDto) {
        String customerId = orderDto.getData().getCustomer().getId();

        return Order.builder()
                .orderId(orderDto.getOrderId())
                .customerId(customerId)
                .platform("FACEBOOK")
                .orderDate(parseFacebookDateTime(orderDto.getData().getInsertedAt()))
                .orderStatus(orderDto.getData().getOrderStatus())
                .totalAmount(orderDto.getData().getTotalAmount() != null ? orderDto.getData().getTotalAmount().doubleValue() : 0.0)
                .createTime(parseFacebookDateTime(orderDto.getData().getInsertedAt()))
                .updateTime(parseFacebookDateTime(orderDto.getData().getUpdatedAt()))
                .build();
    }

    // ===== ORDER ITEMS =====

    private int processOrderItems(CollectedOrderData data) {
        log.info("📋 Processing order items...");

        List<OrderItem> allItems = new ArrayList<>();

        // Collect items from all platforms
        data.getShopeeOrders().forEach(order -> addShopeeOrderItems(allItems, order));
        data.getTiktokOrders().forEach(order -> addTiktokOrderItems(allItems, order));
        data.getFacebookOrders().forEach(order -> addFacebookOrderItems(allItems, order));

        return bulkUpsertOrderItems(allItems);
    }

    private void addShopeeOrderItems(List<OrderItem> itemsList, ShopeeOrderDto order) {
        try {
            String orderId = order.getOrderId();
            List<ShopeeItemDto> items = order.getData().getItemList();

            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    ShopeeItemDto item = items.get(i);
                    OrderItem orderItem = OrderItem.builder()
                            .orderId(orderId)
                            .itemSequence(i + 1)
                            .productSku(item.getModelSku() != null ? item.getModelSku() : "SKU_" + item.getItemId())
                            .platformProductId("SHOPEE")
                            .itemName(item.getItemName())
                            .quantity(item.getModelQuantityPurchased() != null ? item.getModelQuantityPurchased() : 1)
                            .unitPrice(item.getModelOriginalPrice() != null ? item.getModelOriginalPrice().doubleValue() : 0.0)
                            .totalPrice(calculateItemTotal(item.getModelOriginalPrice(), item.getModelQuantityPurchased()))
                            .build();
                    itemsList.add(orderItem);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract items from Shopee order {}: {}", order.getOrderId(), e.getMessage());
        }
    }

    private void addTiktokOrderItems(List<OrderItem> itemsList, TikTokOrderDto order) {
        try {
            String orderId = order.getOrderId();
            List<TikTokItemDto> items = order.getData().getItemList();

            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    TikTokItemDto item = items.get(i);
                    OrderItem orderItem = OrderItem.builder()
                            .orderId(orderId)
                            .itemSequence(i + 1)
                            .productSku(item.getSku() != null ? item.getSku() : "SKU_" + item.getProductId())
                            .platformProductId("TIKTOK")
                            .itemName(item.getProductName())
                            .quantity(item.getQuantity() != null ? item.getQuantity() : 1)
                            .unitPrice(item.getUnitPrice() != null ? item.getUnitPrice() : 0.0)
                            .totalPrice(calculateItemTotal(item.getUnitPrice(), item.getQuantity()))
                            .build();
                    itemsList.add(orderItem);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract items from TikTok order {}: {}", order.getOrderId(), e.getMessage());
        }
    }

    private void addFacebookOrderItems(List<OrderItem> itemsList, FacebookOrderDto order) {
        try {
            String orderId = order.getOrderId();
            List<FacebookItemDto> items = order.getData().getItemList();

            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    FacebookItemDto item = items.get(i);
                    OrderItem orderItem = OrderItem.builder()
                            .orderId(orderId)
                            .itemSequence(i + 1)
                            .productSku(item.getSku() != null ? item.getSku() : "SKU_" + item.getProductId())
                            .platformProductId("FACEBOOK")
                            .itemName(item.getProductName())
                            .quantity(item.getQuantity() != null ? item.getQuantity() : 1)
                            .unitPrice(item.getUnitPrice() != null ? item.getUnitPrice() : 0.0)
                            .totalPrice(calculateItemTotal(item.getUnitPrice(), item.getQuantity()))
                            .build();
                    itemsList.add(orderItem);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract items from Facebook order {}: {}", order.getOrderId(), e.getMessage());
        }
    }

    // ===== PRODUCTS =====

    private int processProducts(CollectedOrderData data) {
        log.info("🏷️ Processing products...");

        Map<ProductId, Product> allProducts = new HashMap<>();

        // Collect products from all platforms
        data.getShopeeOrders().forEach(order -> addShopeeProducts(allProducts, order));
        data.getTiktokOrders().forEach(order -> addTiktokProducts(allProducts, order));
        data.getFacebookOrders().forEach(order -> addFacebookProducts(allProducts, order));

        return bulkUpsertProducts(allProducts);
    }

    private void addShopeeProducts(Map<ProductId, Product> productMap, ShopeeOrderDto order) {
        try {
            List<ShopeeItemDto> items = order.getData().getItemList();

            if (items != null) {
                for (ShopeeItemDto item : items) {
                    String sku = item.getModelSku() != null ? item.getModelSku() : "SKU_" + item.getItemId();
                    ProductId productId = new ProductId(sku, "SHOPEE");

                    if (!productMap.containsKey(productId)) {
                        Product product = Product.builder()
                                .productId(productId)
                                .productName(item.getItemName())
                                .price(item.getModelOriginalPrice() != null ? item.getModelOriginalPrice().doubleValue() : 0.0)
                                .platform("SHOPEE")
                                .createTime(java.time.LocalDateTime.now())
                                .updateTime(java.time.LocalDateTime.now())
                                .build();
                        productMap.put(productId, product);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract products from Shopee order {}: {}", order.getOrderId(), e.getMessage());
        }
    }

    private void addTiktokProducts(Map<ProductId, Product> productMap, TikTokOrderDto order) {
        try {
            List<TikTokItemDto> items = order.getData().getItemList();

            if (items != null) {
                for (TikTokItemDto item : items) {
                    String sku = item.getSku() != null ? item.getSku() : "SKU_" + item.getProductId();
                    ProductId productId = new ProductId(sku, "TIKTOK");

                    if (!productMap.containsKey(productId)) {
                        Product product = Product.builder()
                                .productId(productId)
                                .productName(item.getProductName())
                                .price(item.getUnitPrice() != null ? item.getUnitPrice() : 0.0)
                                .platform("TIKTOK")
                                .createTime(java.time.LocalDateTime.now())
                                .updateTime(java.time.LocalDateTime.now())
                                .build();
                        productMap.put(productId, product);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract products from TikTok order {}: {}", order.getOrderId(), e.getMessage());
        }
    }

    private void addFacebookProducts(Map<ProductId, Product> productMap, FacebookOrderDto order) {
        try {
            List<FacebookItemDto> items = order.getData().getItemList();

            if (items != null) {
                for (FacebookItemDto item : items) {
                    String sku = item.getSku() != null ? item.getSku() : "SKU_" + item.getProductId();
                    ProductId productId = new ProductId(sku, "FACEBOOK");

                    if (!productMap.containsKey(productId)) {
                        Product product = Product.builder()
                                .productId(productId)
                                .productName(item.getProductName())
                                .price(item.getUnitPrice() != null ? item.getUnitPrice() : 0.0)
                                .platform("FACEBOOK")
                                .createTime(java.time.LocalDateTime.now())
                                .updateTime(java.time.LocalDateTime.now())
                                .build();
                        productMap.put(productId, product);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract products from Facebook order {}: {}", order.getOrderId(), e.getMessage());
        }
    }

    // ===== GEOGRAPHY, PAYMENT, SHIPPING, DATE INFO =====

    private int processGeography(CollectedOrderData data) {
        log.info("🌍 Processing geography...");

        Map<String, GeographyInfo> allGeo = new HashMap<>();

        data.getShopeeOrders().forEach(order -> addGeography(allGeo, order, "SHOPEE"));
        data.getTiktokOrders().forEach(order -> addGeography(allGeo, order, "TIKTOK"));
        data.getFacebookOrders().forEach(order -> addGeography(allGeo, order, "FACEBOOK"));

        return bulkUpsertGeography(allGeo);
    }

    private int processPayments(CollectedOrderData data) {
        log.info("💳 Processing payments...");

        Map<String, PaymentInfo> allPayments = new HashMap<>();

        data.getShopeeOrders().forEach(order -> addPayment(allPayments, order, "SHOPEE"));
        data.getTiktokOrders().forEach(order -> addPayment(allPayments, order, "TIKTOK"));
        data.getFacebookOrders().forEach(order -> addPayment(allPayments, order, "FACEBOOK"));

        return bulkUpsertPayments(allPayments);
    }

    private int processShipping(CollectedOrderData data) {
        log.info("🚚 Processing shipping...");

        Map<String, ShippingInfo> allShipping = new HashMap<>();

        data.getShopeeOrders().forEach(order -> addShipping(allShipping, order, "SHOPEE"));
        data.getTiktokOrders().forEach(order -> addShipping(allShipping, order, "TIKTOK"));
        data.getFacebookOrders().forEach(order -> addShipping(allShipping, order, "FACEBOOK"));

        return bulkUpsertShipping(allShipping);
    }

    private int processDateInfo(CollectedOrderData data) {
        log.info("📅 Processing date info...");

        Map<String, ProcessingDateInfo> allDateInfo = new HashMap<>();

        data.getShopeeOrders().forEach(order -> addDateInfo(allDateInfo, order, "SHOPEE"));
        data.getTiktokOrders().forEach(order -> addDateInfo(allDateInfo, order, "TIKTOK"));
        data.getFacebookOrders().forEach(order -> addDateInfo(allDateInfo, order, "FACEBOOK"));

        return bulkUpsertDateInfo(allDateInfo);
    }

    // ===== BULK UPSERT OPERATIONS =====

    private int bulkUpsertCustomers(Map<String, Customer> customers) {
        if (customers.isEmpty()) return 0;

        // Get existing customers
        List<Customer> existing = customerRepository.findAllById(customers.keySet());
        Map<String, Customer> existingMap = existing.stream()
                .collect(Collectors.toMap(Customer::getCustomerId, c -> c));

        // Prepare upsert list
        List<Customer> toSave = new ArrayList<>();
        for (Map.Entry<String, Customer> entry : customers.entrySet()) {
            String id = entry.getKey();
            Customer newCustomer = entry.getValue();

            if (existingMap.containsKey(id)) {
                // Update existing
                Customer existing = existingMap.get(id);
                existing.setLastOrderDate(LocalDateTime.now());
                existing.setTotalOrders(existing.getTotalOrders() + 1);
                existing.setTotalSpent(existing.getTotalSpent() + newCustomer.getTotalSpent());
                existing.setAverageOrderValue(existing.getTotalSpent() / existing.getTotalOrders());
                toSave.add(existing);
            } else {
                // New customer
                toSave.add(newCustomer);
            }
        }

        // Bulk save
        customerRepository.saveAll(toSave);
        return toSave.size();
    }

    private int bulkUpsertOrders(List<Order> orders) {
        if (orders.isEmpty()) return 0;

        // Get existing orders
        Set<String> orderIds = orders.stream().map(Order::getOrderId).collect(Collectors.toSet());
        List<Order> existing = orderRepository.findAllById(orderIds);
        Map<String, Order> existingMap = existing.stream()
                .collect(Collectors.toMap(Order::getOrderId, o -> o));

        // Prepare upsert list
        List<Order> toSave = new ArrayList<>();
        for (Order newOrder : orders) {
            String orderId = newOrder.getOrderId();

            if (existingMap.containsKey(orderId)) {
                // Update existing
                Order existing = existingMap.get(orderId);
                existing.setOrderStatus(newOrder.getOrderStatus());
                existing.setTotalAmount(newOrder.getTotalAmount());
                existing.setUpdateTime(LocalDateTime.now());
                toSave.add(existing);
            } else {
                // New order
                toSave.add(newOrder);
            }
        }

        // Bulk save
        orderRepository.saveAll(toSave);
        return toSave.size();
    }

    private int bulkUpsertOrderItems(List<OrderItem> items) {
        if (items.isEmpty()) return 0;

        // For order items, we use simple saveAll (JPA handles duplicates)
        orderItemRepository.saveAll(items);
        return items.size();
    }

    private int bulkUpsertProducts(Map<ProductId, Product> products) {
        if (products.isEmpty()) return 0;

        // Get existing products
        List<Product> existing = productRepository.findAllById(products.keySet());
        Map<ProductId, Product> existingMap = existing.stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        // Prepare upsert list
        List<Product> toSave = new ArrayList<>();
        for (Map.Entry<ProductId, Product> entry : products.entrySet()) {
            ProductId productId = entry.getKey();
            Product newProduct = entry.getValue();

            if (existingMap.containsKey(productId)) {
                // Update existing
                Product existing = existingMap.get(productId);
                existing.setPrice(newProduct.getPrice());
                existing.setUpdateTime(LocalDateTime.now());
                toSave.add(existing);
            } else {
                // New product
                toSave.add(newProduct);
            }
        }

        // Bulk save
        productRepository.saveAll(toSave);
        return toSave.size();
    }

    private int bulkUpsertGeography(Map<String, GeographyInfo> geoMap) {
        if (geoMap.isEmpty()) return 0;
        geographyInfoRepository.saveAll(geoMap.values());
        return geoMap.size();
    }

    private int bulkUpsertPayments(Map<String, PaymentInfo> paymentMap) {
        if (paymentMap.isEmpty()) return 0;
        paymentInfoRepository.saveAll(paymentMap.values());
        return paymentMap.size();
    }

    private int bulkUpsertShipping(Map<String, ShippingInfo> shippingMap) {
        if (shippingMap.isEmpty()) return 0;
        shippingInfoRepository.saveAll(shippingMap.values());
        return shippingMap.size();
    }

    private int bulkUpsertDateInfo(Map<String, ProcessingDateInfo> dateInfoMap) {
        if (dateInfoMap.isEmpty()) return 0;
        processingDateInfoRepository.saveAll(dateInfoMap.values());
        return dateInfoMap.size();
    }

    // ===== HELPER METHODS =====

    private String getFirstPhone(List<String> phoneNumbers) {
        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            return phoneNumbers.get(0);
        }
        return "Unknown";
    }

    private Double calculateItemTotal(Object price, Object quantity) {
        try {
            double priceValue = 0.0;
            int quantityValue = 1;

            if (price instanceof Number) {
                priceValue = ((Number) price).doubleValue();
            }

            if (quantity instanceof Number) {
                quantityValue = ((Number) quantity).intValue();
            }

            return priceValue * quantityValue;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private java.time.LocalDateTime convertShopeeTimestamp(Long timestamp) {
        if (timestamp != null) {
            return java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochSecond(timestamp),
                    java.time.ZoneId.systemDefault()
            );
        }
        return java.time.LocalDateTime.now();
    }

    private java.time.LocalDateTime convertTiktokTimestamp(Long timestamp) {
        if (timestamp != null) {
            return java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochSecond(timestamp),
                    java.time.ZoneId.systemDefault()
            );
        }
        return java.time.LocalDateTime.now();
    }

    private java.time.LocalDateTime parseFacebookDateTime(String dateTimeString) {
        if (dateTimeString != null && !dateTimeString.isEmpty()) {
            try {
                // Facebook thường dùng ISO format: "2023-12-01T10:30:00Z"
                return java.time.LocalDateTime.parse(dateTimeString.replace("Z", ""));
            } catch (Exception e) {
                log.warn("Failed to parse Facebook datetime: {}", dateTimeString);
            }
        }
        return java.time.LocalDateTime.now();
    }    // ===== PLACEHOLDER METHODS (To be implemented as needed) =====

    private void addGeography(Map<String, GeographyInfo> geoMap, Object orderDto, String platform) {
        // TODO: Implement geography extraction based on actual requirements
        // This would extract address, province, district, etc. from order data
    }

    private void addPayment(Map<String, PaymentInfo> paymentMap, Object orderDto, String platform) {
        // TODO: Implement payment extraction based on actual requirements
        // This would extract payment method, COD status, etc. from order data
    }

    private void addShipping(Map<String, ShippingInfo> shippingMap, Object orderDto, String platform) {
        // TODO: Implement shipping extraction based on actual requirements
        // This would extract shipping provider, cost, delivery dates, etc. from order data
    }

    private void addDateInfo(Map<String, ProcessingDateInfo> dateInfoMap, Object orderDto, String platform) {
        // TODO: Implement date info extraction based on actual requirements
        // This would extract and process date dimensions from order data
    }

    // ===== UTILITY METHODS =====

    private void logSummary(BatchProcessingResult result) {
        long durationMs = result.getDurationMs();

        log.info("📊 BATCH PROCESSING SUMMARY:");
        log.info("   ⏱️  Duration: {} ms ({} seconds)", durationMs, durationMs / 1000);
        log.info("   👥 Customers: {}", result.getCustomersProcessed());
        log.info("   📦 Orders: {}", result.getOrdersProcessed());
        log.info("   📋 Items: {}", result.getItemsProcessed());
        log.info("   🏷️ Products: {}", result.getProductsProcessed());
        log.info("   🌍 Geography: {}", result.getGeographyProcessed());
        log.info("   💳 Payments: {}", result.getPaymentsProcessed());
        log.info("   🚚 Shipping: {}", result.getShippingProcessed());
        log.info("   📅 Date Info: {}", result.getDateInfoProcessed());

        int totalRecords = result.getTotalRecordsProcessed();
        log.info("   🎯 Total Records: {}", totalRecords);

        if (durationMs > 0) {
            double recordsPerSecond = (double) totalRecords / (durationMs / 1000.0);
            log.info("   ⚡ Performance: {:.1f} records/second", recordsPerSecond);
        }
    }
}