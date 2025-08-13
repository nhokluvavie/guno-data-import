// FacebookEtlService.java - Facebook ETL Service (Complete Implementation)
package com.guno.etl.service;

import com.guno.etl.dto.FacebookApiResponse;
import com.guno.etl.dto.FacebookOrderDto;
import com.guno.etl.dto.FacebookItemDto;
import com.guno.etl.entity.*;
import com.guno.etl.repository.*;
import com.guno.etl.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FacebookEtlService {

    private static final Logger log = LoggerFactory.getLogger(FacebookEtlService.class);

    @Autowired
    private FacebookApiService facebookApiService;

    // All repositories
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private GeographyInfoRepository geographyInfoRepository;
    @Autowired private StatusRepository statusRepository;
    @Autowired private OrderStatusRepository orderStatusRepository;
    @Autowired private OrderStatusDetailRepository orderStatusDetailRepository;
    @Autowired private PaymentInfoRepository paymentInfoRepository;
    @Autowired private ShippingInfoRepository shippingInfoRepository;
    @Autowired private ProcessingDateInfoRepository processingDateInfoRepository;

    @Value("${etl.platforms.facebook.name:FACEBOOK}")
    private String platformName;

    // ===== MAIN ETL METHOD =====
    public EtlResult processOrdersForDate(String dateString) {
        log.info("Starting Facebook ETL process for date: {}", dateString);

        long startTime = System.currentTimeMillis();
        int totalOrders = 0;
        int ordersProcessed = 0;
        List<FailedOrder> failedOrders = new ArrayList<>();

        try {
            // Fetch orders for specific date from API
            FacebookApiResponse response = facebookApiService.fetchAllOrdersForDate(dateString);

            if (response == null || response.getData() == null) {
                log.warn("No Facebook data received for date: {}", dateString);

                // Create EtlResult
                EtlResult result = new EtlResult();
                result.success = true;
                result.totalOrders = 0;
                result.ordersProcessed = 0;
                result.durationMs = System.currentTimeMillis() - startTime;
                result.errorMessage = "No data available for the specified date";
                result.failedOrders = failedOrders;
                return result;
            }

            List<FacebookOrderDto> orders = response.getData().getOrders();
            totalOrders = orders.size();

            log.info("Processing {} Facebook orders for date: {}", totalOrders, dateString);

            // Process each order
            for (FacebookOrderDto orderDto : orders) {
                try {
                    processOrderUpsert(orderDto);
                    ordersProcessed++;

                    if (ordersProcessed % 10 == 0) {
                        log.info("Processed {} / {} Facebook orders for date: {}",
                                ordersProcessed, totalOrders, dateString);
                    }

                } catch (Exception e) {
                    log.error("Failed to process Facebook order {} for date {}: {}",
                            orderDto.getOrderId(), dateString, e.getMessage());

                    failedOrders.add(new FailedOrder(orderDto.getOrderId(), e.getMessage()));
                }
            }

            long durationMs = System.currentTimeMillis() - startTime;
            double successRate = totalOrders > 0 ? ((double) ordersProcessed / totalOrders) * 100 : 0;

            log.info("Completed Facebook ETL for date: {} - {}/{} orders processed ({}%) in {} ms",
                    dateString, ordersProcessed, totalOrders, String.format("%.2f", successRate), durationMs);

            // Create EtlResult
            EtlResult result = new EtlResult();
            result.success = true;
            result.totalOrders = totalOrders;
            result.ordersProcessed = ordersProcessed;
            result.durationMs = durationMs;
            result.successRate = successRate;
            result.failedOrders = failedOrders;
            return result;

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            log.error("Facebook ETL process failed for date {}: {}", dateString, e.getMessage());

            // Create failed result
            EtlResult result = new EtlResult();
            result.success = false;
            result.totalOrders = totalOrders;
            result.ordersProcessed = ordersProcessed;
            result.durationMs = durationMs;
            result.errorMessage = e.getMessage();
            result.failedOrders = failedOrders;
            return result;
        }
    }

    // ===== ORDER PROCESSING (Process all 9 tables) =====
    @Transactional
    public void processOrderUpsert(FacebookOrderDto orderDto) {
        try {
            log.debug("Processing Facebook order: {}", orderDto.getOrderId());

            // Process in correct order (dependency chain)
            processCustomerUpsert(orderDto);        // 1. Customer
            processOrderEntityUpsert(orderDto);           // 2. Order
            processProductsUpsert(orderDto);        // 3. Products
            processOrderItemsUpsert(orderDto);      // 4. Order Items
            processGeographyUpsert(orderDto);       // 5. Geography
            processProcessingDateInfoUpsert(orderDto); // 11. Processing Date Info
            processStatusUpsert(orderDto);          // 6. Status
            processOrderStatusUpsert(orderDto);     // 7. Order Status
            processOrderStatusDetailUpsert(orderDto); // 8. Order Status Detail
            processPaymentInfoUpsert(orderDto);     // 9. Payment Info
            processShippingInfoUpsert(orderDto);    // 10. Shipping Info


            log.debug("Successfully processed Facebook order: {}", orderDto.getOrderId());

        } catch (Exception e) {
            log.error("Failed to process Facebook order {}: {}", orderDto.getOrderId(), e.getMessage());
            throw e; // Re-throw để trigger rollback
        }
    }

    // ===== CUSTOMER PROCESSING =====
    private void processCustomerUpsert(FacebookOrderDto orderDto) {
        if (orderDto.getData() == null || orderDto.getData().getCustomer() == null) {
            log.warn("No customer data found for Facebook order: {}", orderDto.getOrderId());
            return;
        }

        FacebookOrderDto.FacebookCustomer customer = orderDto.getData().getCustomer();
        String phoneNumber = getFirstPhoneNumber(customer.getPhoneNumbers());

        if (phoneNumber == null) {
            log.warn("No phone number found for Facebook customer: {}", customer.getId());
            return;
        }

        String phoneHash = HashUtil.hashPhone(phoneNumber);
        Optional<Customer> existing = customerRepository.findByPhoneHash(phoneHash);

        if (existing.isPresent()) {
            Customer existingCustomer = existing.get();
            updateCustomerFromFacebook(existingCustomer, orderDto);
            customerRepository.save(existingCustomer);
            log.debug("Updated Facebook customer: {}", phoneHash);
        } else {
            Customer newCustomer = createCustomerFromFacebook(orderDto);
            customerRepository.save(newCustomer);
            log.debug("Created new Facebook customer: {}", phoneHash);
        }
    }

    private Customer createCustomerFromFacebook(FacebookOrderDto orderDto) {
        FacebookOrderDto.FacebookCustomer customer = orderDto.getData().getCustomer();
        String phoneNumber = getFirstPhoneNumber(customer.getPhoneNumbers());
        String phoneHash = HashUtil.hashPhone(phoneNumber);
        String emailHash = getFirstEmail(customer.getEmails()) != null ?
                HashUtil.hashEmail(getFirstEmail(customer.getEmails())) : null;

        return Customer.builder()
                .customerId(orderDto.getData().getCustomer().getId())
                .customerKey(customerRepository.findNextCustomerKey())
                .platformCustomerId(customer.getId())
                .phoneHash(phoneHash)
                .emailHash(emailHash)
                .gender(customer.getGender())
                .ageGroup("unknown")
                .customerSegment("STANDARD")
                .customerTier("STANDARD")
                .acquisitionChannel("FACEBOOK")
                .firstOrderDate(parseDateTime(customer.getInsertedAt()))
                .lastOrderDate(parseDateTime(customer.getLastOrderAt()))
                .totalOrders(customer.getOrderCount() != null ? customer.getOrderCount() : 0)
                .totalSpent(customer.getPurchasedAmount() != null ? customer.getPurchasedAmount().doubleValue() : 0.0)
                .averageOrderValue(calculateAverageOrderValue(customer))
                .totalItemsPurchased(0)
                .preferredPaymentMethod("COD")
                .preferredPlatform(platformName)
                .primaryShippingProvince("Unknown")
                .shipsToMultipleProvinces(false)
                .loyaltyPoints(customer.getRewardPoint() != null ? customer.getRewardPoint() : 0)
                .referralCount(customer.getCountReferrals() != null ? customer.getCountReferrals() : 0)
                .isReferrer(customer.getCountReferrals() != null && customer.getCountReferrals() > 0)
                .totalItemsPurchased(0)
                .daysSinceLastOrder(0)
                .purchaseFrequencyDays(0.0)
                .returnRate(0.0)
                .cancellationRate(0.0)
                .codPreferenceRate(1.0)
                .favoriteCategory("Unknown")
                .favoriteBrand("Unknown")
                .build();
    }

    private void updateCustomerFromFacebook(Customer customer, FacebookOrderDto orderDto) {
        FacebookOrderDto.FacebookCustomer fbCustomer = orderDto.getData().getCustomer();

        if (fbCustomer.getOrderCount() != null) {
            customer.setTotalOrders(fbCustomer.getOrderCount());
        }
        if (fbCustomer.getPurchasedAmount() != null) {
            customer.setTotalSpent(fbCustomer.getPurchasedAmount().doubleValue());
        }
        if (fbCustomer.getRewardPoint() != null) {
            customer.setLoyaltyPoints(fbCustomer.getRewardPoint());
        }

        // Recalculate derived metrics
        if (customer.getTotalOrders() > 0 && customer.getTotalSpent() > 0) {
            customer.setAverageOrderValue(customer.getTotalSpent() / customer.getTotalOrders());
        }
    }

    // ===== ORDER PROCESSING =====
    private void processOrderEntityUpsert(FacebookOrderDto orderDto) {
        String orderId = orderDto.getOrderId();
        Optional<Order> existing = orderRepository.findById(orderId);

        if (existing.isPresent()) {
            Order order = existing.get();
            updateOrderFromFacebook(order, orderDto);
            orderRepository.save(order);
            log.debug("Updated Facebook order: {}", orderId);
        } else {
            Order newOrder = createOrderFromFacebook(orderDto);
            orderRepository.save(newOrder);
            log.debug("Created new Facebook order: {}", orderId);
        }
    }

    private Order createOrderFromFacebook(FacebookOrderDto orderDto) {
        FacebookOrderDto.FacebookOrderData data = orderDto.getData();
        String customerId = getCustomerIdFromOrder(orderDto);

        return Order.builder()
                .orderId(orderDto.getOrderId())
                .customerId(customerId)
                .shopId(orderDto.getShopId() != null ? orderDto.getShopId().toString() : null)
                .internalUuid(orderDto.getId())
                .orderCount(1)
                .itemQuantity(data.getItems() != null ? data.getItems().size() : 0)
                .totalItemsInOrder(data.getItems() != null ? data.getItems().size() : 0)
                .grossRevenue(data.getTotal() != null ? data.getTotal().doubleValue() : 0.0)
                .netRevenue(calculateNetRevenue(data))
                .shippingFee(data.getShippingFee() != null ? data.getShippingFee().doubleValue() : 0.0)
                .taxAmount(data.getTax() != null ? data.getTax().doubleValue() : 0.0)
                .discountAmount(data.getDiscount() != null ? data.getDiscount().doubleValue() : 0.0)
                .codAmount(data.getCod() != null ? data.getCod().doubleValue() : 0.0)
                .platformFee(0.0)
                .sellerDiscount(0.0)
                .platformDiscount(data.getDiscount() != null ? data.getDiscount().doubleValue() : 0.0)
                .isDelivered(isDelivered(orderDto.getStatus()))
                .isCancelled(isCancelled(orderDto.getStatus()))
                .actualShippingFee(data.getShippingFee() != null ? data.getShippingFee().doubleValue() : 0.0)
                .shippingWeightGram(500)
                .orderToShipHours(0)
                .shipToDeliveryHours(0)
                .totalFulfillmentHours(0)
                .customerOrderSequence(1)
                .customerLifetimeOrders(1)
                .customerLifetimeValue(data.getTotal() != null ? data.getTotal().doubleValue() : 0.0)
                .daysSinceLastOrder(0)
                .promotionImpact(0.0)
                .adRevenue(0.0)
                .organicRevenue(data.getTotal() != null ? data.getTotal().doubleValue() : 0.0)
                .aov(data.getTotal() != null ? data.getTotal().doubleValue() : 0.0)
                .shippingCostRatio(calculateShippingCostRatio(data))
                .createdAt(parseDateTime(data.getCreatedAt()))
                .rawData(1)
                .platformSpecificData(1)
                .build();
    }

    private void updateOrderFromFacebook(Order order, FacebookOrderDto orderDto) {
        FacebookOrderDto.FacebookOrderData data = orderDto.getData();

        if (data.getTotal() != null) {
            order.setGrossRevenue(data.getTotal().doubleValue());
            order.setNetRevenue(calculateNetRevenue(data));
            order.setAov(data.getTotal().doubleValue());
            order.setOrganicRevenue(data.getTotal().doubleValue());
        }

        if (data.getShippingFee() != null) {
            order.setShippingFee(data.getShippingFee().doubleValue());
            order.setActualShippingFee(data.getShippingFee().doubleValue());
            order.setShippingCostRatio(calculateShippingCostRatio(data));
        }

        if (data.getDiscount() != null) {
            order.setDiscountAmount(data.getDiscount().doubleValue());
            order.setPlatformDiscount(data.getDiscount().doubleValue());
        }

        if (data.getCod() != null) {
            order.setCodAmount(data.getCod().doubleValue());
        }

        if (data.getTax() != null) {
            order.setTaxAmount(data.getTax().doubleValue());
        }

        // Update status booleans
        order.setIsDelivered(isDelivered(orderDto.getStatus()));
        order.setIsCancelled(isCancelled(orderDto.getStatus()));
    }

    // ===== PRODUCT PROCESSING =====
    private void processProductsUpsert(FacebookOrderDto orderDto) {
        if (orderDto.getData() == null || orderDto.getData().getItems() == null) {
            return;
        }

        for (FacebookItemDto item : orderDto.getData().getItems()) {
            try {
                processProductUpsert(item);
            } catch (Exception e) {
                log.error("Failed to process Facebook product: {}", item.getProductId(), e);
            }
        }
    }

    private void processProductUpsert(FacebookItemDto item) {
        String sku = item.getVariationCode() != null ? item.getVariationCode() : item.getProductCode();
        if (sku == null) {
            log.warn("No SKU found for Facebook item: {}", item.getId());
            return;
        }

        String platformProductId = platformName;
        ProductId productId = new ProductId(sku, platformProductId);
        Optional<Product> existing = productRepository.findById(productId);

        if (existing.isPresent()) {
            Product product = existing.get();
            updateProductFromFacebook(product, item);
            productRepository.save(product);
            log.debug("Updated Facebook product: {}", sku);
        } else {
            Product newProduct = createProductFromFacebook(item);
            productRepository.save(newProduct);
            log.debug("Created new Facebook product: {}", sku);
        }
    }

    private Product createProductFromFacebook(FacebookItemDto item) {
        String sku = item.getVariationCode() != null ? item.getVariationCode() : item.getProductCode();

        return Product.builder()
                .sku(sku)
                .platformProductId(platformName)
                .productId(item.getProductId())
                .variationId(item.getVariationId())
                .barcode("unknown")
                .productName(item.getProductName())
                .productDescription("Facebook product")
                .brand("Facebook Brand")
                .model("Facebook Model")
                .categoryLevel1("General")
                .categoryLevel2("Facebook Category")
                .categoryLevel3("Facebook Product")
                .categoryPath("General > Facebook Category > Facebook Product")
                .color("Default")
                .size("Default")
                .material("Default")
                .weightGram(500)
                .dimensions("10x10x10")
                .costPrice(item.getPrice() != null ? item.getPrice() : 0.0)
                .retailPrice(item.getPrice() != null ? item.getPrice().doubleValue() : 0.0)
                .originalPrice(item.getPrice() != null ? item.getPrice().doubleValue() : 0.0)
                .priceRange("MEDIUM")
                .isActive(true)
                .isFeatured(false)
                .isSeasonal(false)
                .isNewArrival(true)
                .isBestSeller(false)
                .primaryImageUrl(item.getImageUrl())
                .imageCount(item.getImageUrl() != null ? 1 : 0)
                .seoTitle(item.getProductName())
                .seoKeywords("facebook,product")
                .build();
    }

    private void updateProductFromFacebook(Product product, FacebookItemDto item) {
        if (item.getPrice() != null) {
            product.setRetailPrice(item.getPrice().doubleValue());
            product.setOriginalPrice(item.getPrice().doubleValue());
        }
        if (item.getImageUrl() != null) {
            product.setPrimaryImageUrl(item.getImageUrl());
        }
    }

    // ===== ORDER ITEMS PROCESSING =====
    private void processOrderItemsUpsert(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            log.debug("Processing order items for order: {}", orderId);

            if (orderDto.getData() == null || orderDto.getData().getItems() == null) {
                log.warn("No line items found for order: {}", orderId);
                return;
            }

            // Delete existing items first
            List<OrderItem> existingItems = orderItemRepository.findByOrderIdOrderByItemSequence(orderId);
            if (!existingItems.isEmpty()) {
                orderItemRepository.deleteAll(existingItems);
                log.debug("Deleted {} existing items for order: {}", existingItems.size(), orderId);
            }

            // Insert new items
            List<FacebookItemDto> items = orderDto.getData().getItems();
            for (int i = 0; i < items.size(); i++) {
                FacebookItemDto item = items.get(i);
                try {
                    OrderItem orderItem = createOrderItemFromFacebookItem(orderDto, item, i + 1);
                    if (orderItem != null) {
                        orderItemRepository.save(orderItem);
                        log.debug("Created order item for order: {} - SKU: {}", orderId, item.getVariationCode());
                    }
                } catch (Exception e) {
                    log.error("Failed to process order item for order: {} - Item: {}", orderId, item.getId(), e);
                }
            }

            log.debug("Order items processing completed for order: {}", orderId);

        } catch (Exception e) {
            log.error("Failed to process order items for order {}: {}", orderDto.getOrderId(), e.getMessage());
            throw e;
        }
    }

    private OrderItem createOrderItemFromFacebookItem(FacebookOrderDto orderDto, FacebookItemDto item, int itemSequence) {
        try {
            String orderId = orderDto.getOrderId();
            String sku = item.getVariationCode() != null ? item.getVariationCode() : item.getProductCode();

            if (sku == null) {
                log.warn("No SKU found for Facebook item: {}", item.getId());
                return null;
            }

            return OrderItem.builder()
                    .orderId(orderId)
                    .sku(sku)
                    .platformProductId(platformName)
                    .quantity(item.getQuantity() != null ? item.getQuantity() : 1)
                    .unitPrice(item.getPrice() != null ? item.getPrice().doubleValue() : 0.0)
                    .totalPrice(item.getTotalAmount() != null ? item.getTotalAmount().doubleValue() : 0.0)
                    .itemDiscount(item.getDiscountAmount() != null ? item.getDiscountAmount().doubleValue() : 0.0)
                    .promotionType(item.getDiscountType())
                    .promotionCode(null)
                    .itemStatus(mapFacebookStatus(orderDto.getStatus()))
                    .itemSequence(itemSequence)
                    .opId(System.currentTimeMillis() % 1000000L)
                    .build();

        } catch (Exception e) {
            log.error("Failed to create OrderItem entity for item: {}", item.getId(), e);
            return null;
        }
    }

    // ===== GEOGRAPHY PROCESSING =====
    private void processGeographyUpsert(FacebookOrderDto orderDto) {
        String orderId = orderDto.getOrderId();
        Optional<GeographyInfo> existing = geographyInfoRepository.findById(orderId);

        if (existing.isPresent()) {
            GeographyInfo geography = existing.get();
            updateGeographyFromFacebook(geography, orderDto);
            geographyInfoRepository.save(geography);
            log.debug("Updated Facebook geography: {}", orderId);
        } else {
            GeographyInfo newGeography = createGeographyFromFacebook(orderDto);
            if (newGeography != null) {
                geographyInfoRepository.save(newGeography);
                log.debug("Created new Facebook geography: {}", orderId);
            }
        }
    }

    private GeographyInfo createGeographyFromFacebook(FacebookOrderDto orderDto) {
        FacebookOrderDto.FacebookShippingAddress address = getShippingAddress(orderDto);
        if (address == null) {
            return null;
        }

        return GeographyInfo.builder()
                .orderId(orderDto.getOrderId())
                .geographyKey(System.currentTimeMillis() % 1000000L)
                .countryCode("VN")
                .countryName(address.getCountryName() != null ? address.getCountryName() : "Vietnam")
                .regionCode(null)
                .regionName("Vietnam Region")
                .provinceCode(address.getCityId() != null ? address.getCityId().toString() : null)
                .provinceName(address.getCityName())
                .provinceType("Province")
                .districtCode(address.getDistrictId() != null ? address.getDistrictId().toString() : null)
                .districtName(address.getDistrictName())
                .districtType("District")
                .wardCode(address.getWardId() != null ? address.getWardId().toString() : null)
                .wardName(address.getWardName())
                .wardType("Ward")
                .isUrban(true)
                .isMetropolitan(false)
                .isCoastal(false)
                .isBorder(false)
                .economicTier("TIER_2")
                .populationDensity("MEDIUM")
                .incomeLevel("MEDIUM")
                .shippingZone("ZONE_2")
                .deliveryComplexity("STANDARD")
                .standardDeliveryDays(3)
                .expressDeliveryAvailable(true)
                .build();
    }

    private void updateGeographyFromFacebook(GeographyInfo geography, FacebookOrderDto orderDto) {
        FacebookOrderDto.FacebookShippingAddress address = getShippingAddress(orderDto);
        if (address != null) {
            geography.setProvinceName(address.getCityName());
            geography.setDistrictName(address.getDistrictName());
            geography.setWardName(address.getWardName());
        }
    }

    // ===== STATUS PROCESSING =====
    private void processStatusUpsert(FacebookOrderDto orderDto) {
        String facebookStatus = orderDto.getStatus().toString();
        Optional<Status> existing = statusRepository.findByPlatformAndPlatformStatusCode(platformName, facebookStatus);

        if (!existing.isPresent()) {
            Status newStatus = createStatusFromFacebook(facebookStatus);
            statusRepository.save(newStatus);
            log.debug("Created new Facebook status: {}", facebookStatus);
        }
    }

    private Status createStatusFromFacebook(String facebookStatus) {
        return Status.builder()
                .platform(platformName)
                .platformStatusCode(facebookStatus)
                .platformStatusName(mapFacebookStatusName(facebookStatus))
                .standardStatusCode(mapToStandardStatus(facebookStatus))
                .standardStatusName(mapToStandardStatusName(facebookStatus))
                .statusCategory(mapToStatusCategory(facebookStatus))
                .build();
    }

    // ===== ORDER STATUS PROCESSING =====
    private void processOrderStatusUpsert(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            String facebookStatus = orderDto.getStatus().toString();

            // Get status entity to get statusKey
            Optional<Status> statusEntity = statusRepository.findByPlatformAndPlatformStatusCode(platformName, facebookStatus);

            if (!statusEntity.isPresent()) {
                log.warn("Status entity not found for platform {} status {}, skipping order status",
                        platformName, facebookStatus);
                return;
            }

            Long statusKey = statusEntity.get().getStatusKey();

            // Check if order status already exists for this statusKey + orderId combination
            Optional<OrderStatus> existingStatus = orderStatusRepository.findByStatusKeyAndOrderId(statusKey, orderId);

            if (existingStatus.isPresent()) {
                OrderStatus existing = existingStatus.get();
                OrderStatus newStatus = createOrderStatusFromFacebook(orderDto, statusKey);

                if (newStatus != null) {
                    existing.setTransitionTimestamp(newStatus.getTransitionTimestamp());
                    existing.setDurationInPreviousStatusHours(newStatus.getDurationInPreviousStatusHours());
                    existing.setTransitionReason(newStatus.getTransitionReason());
                    existing.setTransitionTrigger(newStatus.getTransitionTrigger());
                    existing.setChangedBy(newStatus.getChangedBy());
                    existing.setIsOnTimeTransition(newStatus.getIsOnTimeTransition());
                    existing.setIsExpectedTransition(newStatus.getIsExpectedTransition());

                    orderStatusRepository.save(existing);
                    log.debug("Updated order status for order: {}", orderId);
                }
            } else {
                OrderStatus newStatus = createOrderStatusFromFacebook(orderDto, statusKey);
                if (newStatus != null) {
                    orderStatusRepository.save(newStatus);
                    log.debug("Created new order status for order: {}", orderId);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process order status for order {}: {}", orderDto.getOrderId(), e.getMessage());
        }
    }

    private OrderStatus createOrderStatusFromFacebook(FacebookOrderDto orderDto, Long statusKey) {
        try {
            return OrderStatus.builder()
                    .statusKey(statusKey)
                    .orderId(orderDto.getOrderId())
                    .transitionDateKey(generateDateKey(parseDateTime(orderDto.getUpdatedAt())))
                    .transitionTimestamp(parseDateTime(orderDto.getUpdatedAt()))
                    .durationInPreviousStatusHours(0)
                    .transitionReason("Facebook status update")
                    .transitionTrigger("AUTOMATIC")
                    .changedBy("SYSTEM")
                    .isOnTimeTransition(true)
                    .isExpectedTransition(true)
                    .historyKey(System.currentTimeMillis() % 1000000L)
                    .build();
        } catch (Exception e) {
            log.error("Failed to create OrderStatus entity for order: {}", orderDto.getOrderId(), e);
            return null;
        }
    }

    // ===== ORDER STATUS DETAIL PROCESSING =====
    private void processOrderStatusDetailUpsert(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            String facebookStatus = orderDto.getStatus().toString();

            // Get status entity to get statusKey
            Optional<Status> statusEntity = statusRepository.findByPlatformAndPlatformStatusCode(platformName, facebookStatus);

            if (!statusEntity.isPresent()) {
                log.warn("Status entity not found for platform {} status {}, skipping status detail",
                        platformName, facebookStatus);
                return;
            }

            Long statusKey = statusEntity.get().getStatusKey();

            // Check if status detail already exists for this statusKey + orderId combination
            Optional<OrderStatusDetail> existingDetail = orderStatusDetailRepository.findByStatusKeyAndOrderId(statusKey, orderId);

            if (existingDetail.isPresent()) {
                OrderStatusDetail existing = existingDetail.get();
                OrderStatusDetail newDetail = createOrderStatusDetailFromFacebook(orderDto, statusKey);

                if (newDetail != null) {
                    existing.setIsActiveOrder(newDetail.getIsActiveOrder());
                    existing.setIsCompletedOrder(newDetail.getIsCompletedOrder());
                    existing.setIsRevenueRecognized(newDetail.getIsRevenueRecognized());
                    existing.setIsRefundable(newDetail.getIsRefundable());
                    existing.setIsCancellable(newDetail.getIsCancellable());
                    existing.setIsTrackable(newDetail.getIsTrackable());
                    existing.setNextPossibleStatuses(newDetail.getNextPossibleStatuses());
                    existing.setAutoTransitionHours(newDetail.getAutoTransitionHours());
                    existing.setRequiresManualAction(newDetail.getRequiresManualAction());
                    existing.setStatusColor(newDetail.getStatusColor());
                    existing.setStatusIcon(newDetail.getStatusIcon());
                    existing.setCustomerVisible(newDetail.getCustomerVisible());
                    existing.setCustomerDescription(newDetail.getCustomerDescription());
                    existing.setAverageDurationHours(newDetail.getAverageDurationHours());
                    existing.setSuccessRate(newDetail.getSuccessRate());

                    orderStatusDetailRepository.save(existing);
                    log.debug("Updated order status detail for order: {}", orderId);
                }
            } else {
                OrderStatusDetail newDetail = createOrderStatusDetailFromFacebook(orderDto, statusKey);
                if (newDetail != null) {
                    orderStatusDetailRepository.save(newDetail);
                    log.debug("Created new order status detail for order: {}", orderId);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process order status detail for order {}: {}", orderDto.getOrderId(), e.getMessage());
        }
    }

    private OrderStatusDetail createOrderStatusDetailFromFacebook(FacebookOrderDto orderDto, Long statusKey) {
        try {
            return OrderStatusDetail.builder()
                    .statusKey(statusKey)
                    .orderId(orderDto.getOrderId())
                    .isActiveOrder(isActiveOrder(orderDto.getStatus()))
                    .isCompletedOrder(isCompletedOrder(orderDto.getStatus()))
                    .isRevenueRecognized(isRevenueRecognized(orderDto.getStatus()))
                    .isRefundable(isRefundable(orderDto.getStatus()))
                    .isCancellable(isCancellable(orderDto.getStatus()))
                    .isTrackable(isTrackable(orderDto.getStatus()))
                    .nextPossibleStatuses("PROCESSING,SHIPPED,DELIVERED,CANCELLED")
                    .autoTransitionHours(24)
                    .requiresManualAction(false)
                    .statusColor(getStatusColor(orderDto.getStatus().toString()))
                    .statusIcon(getStatusIcon(orderDto.getStatus().toString()))
                    .customerVisible(true)
                    .customerDescription("Order " + mapFacebookStatusName(orderDto.getStatus().toString()))
                    .averageDurationHours(24.0)
                    .successRate(95.0)
                    .build();

        } catch (Exception e) {
            log.error("Failed to create OrderStatusDetail entity for order: {}", orderDto.getOrderId(), e);
            return null;
        }
    }

    // ===== PAYMENT INFO PROCESSING =====
    private void processPaymentInfoUpsert(FacebookOrderDto orderDto) {
        String orderId = orderDto.getOrderId();
        Optional<PaymentInfo> existing = paymentInfoRepository.findById(orderId);

        if (existing.isPresent()) {
            PaymentInfo payment = existing.get();
            updatePaymentInfoFromFacebook(payment, orderDto);
            paymentInfoRepository.save(payment);
        } else {
            PaymentInfo newPayment = createPaymentInfoFromFacebook(orderDto);
            paymentInfoRepository.save(newPayment);
        }
    }

    private PaymentInfo createPaymentInfoFromFacebook(FacebookOrderDto orderDto) {
        FacebookOrderDto.FacebookOrderData data = orderDto.getData();
        boolean isCodPayment = data.getCod() != null && data.getCod() > 0;

        return PaymentInfo.builder()
                .orderId(orderDto.getOrderId())
                .paymentKey(System.currentTimeMillis() % 1000000L)
                .paymentMethod(isCodPayment ? "COD" : "CASH")
                .paymentCategory(isCodPayment ? "COD" : "CASH")
                .paymentProvider("FACEBOOK")
                .isCod(isCodPayment)
                .isPrepaid(!isCodPayment)
                .isInstallment(false)
                .installmentMonths(0)
                .supportsRefund(true)
                .supportsPartialRefund(true)
                .refundProcessingDays(7)
                .riskLevel("LOW")
                .requiresVerification(false)
                .fraudScore(0.0)
                .transactionFeeRate(0.0)
                .processingFee(0.0)
                .paymentProcessingTimeMinutes(0)
                .settlementDays(1)
                .build();
    }

    private void updatePaymentInfoFromFacebook(PaymentInfo payment, FacebookOrderDto orderDto) {
        FacebookOrderDto.FacebookOrderData data = orderDto.getData();
        boolean isCodPayment = data.getCod() != null && data.getCod() > 0;

        payment.setPaymentMethod(isCodPayment ? "COD" : "CASH");
        payment.setPaymentCategory(isCodPayment ? "COD" : "CASH");
        payment.setIsCod(isCodPayment);
        payment.setIsPrepaid(!isCodPayment);
    }

    // ===== SHIPPING INFO PROCESSING =====
    private void processShippingInfoUpsert(FacebookOrderDto orderDto) {
        String orderId = orderDto.getOrderId();
        Optional<ShippingInfo> existing = shippingInfoRepository.findById(orderId);

        if (existing.isPresent()) {
            ShippingInfo shipping = existing.get();
            updateShippingInfoFromFacebook(shipping, orderDto);
            shippingInfoRepository.save(shipping);
        } else {
            ShippingInfo newShipping = createShippingInfoFromFacebook(orderDto);
            if (newShipping != null) {
                shippingInfoRepository.save(newShipping);
            }
        }
    }

    private ShippingInfo createShippingInfoFromFacebook(FacebookOrderDto orderDto) {
        FacebookOrderDto.FacebookOrderData data = orderDto.getData();

        // Extract shipping provider from tracking data
        String shippingProvider = extractShippingProvider(data);
        String trackingNumber = extractTrackingNumber(data);
        String deliveryStatus = extractDeliveryStatus(data);

        // Extract address info for shipping zone calculation
        FacebookOrderDto.FacebookShippingAddress address = getShippingAddress(orderDto);
        String province = address != null ? address.getCityName() : "Unknown";
        String district = address != null ? address.getDistrictName() : "Unknown";

        return ShippingInfo.builder()
                // Basic identification
                .orderId(orderDto.getOrderId())
                .shippingKey(System.currentTimeMillis() % 1000000L)

                // Provider information - từ data thật
                .providerId(extractProviderId(data, shippingProvider))
                .providerName(shippingProvider != null ? shippingProvider : "Facebook Logistics")
                .providerType(determineProviderType(shippingProvider))
                .providerTier(determineProviderTier(province, district))

                // Service information - từ order source và delivery status
                .serviceType(determineServiceType(data))
                .serviceTier(determineServiceTier(data, province))
                .deliveryCommitment(calculateDeliveryCommitment(data, province))
                .shippingMethod(extractShippingMethod(data))

                // Pickup and delivery - từ address data
                .pickupType(determinePickupType(data))
                .deliveryType(determineDeliveryType(address))

                // Fee structure - từ actual order data
                .baseFee(data.getShippingFee() != null ? data.getShippingFee().doubleValue() : 0.0)
                .weightBasedFee(calculateWeightBasedFee(data))
                .distanceBasedFee(calculateDistanceBasedFee(province, district))
                .codFee(calculateCodFee(data))
                .insuranceFee(calculateInsuranceFee(data))

                // Service capabilities - based on provider
                .supportsCod(data.getCod() != null && data.getCod() > 0)
                .supportsInsurance(checkInsuranceSupport(shippingProvider))
                .supportsFragile(checkFragileSupport(shippingProvider))
                .supportsRefrigerated(false) // Facebook orders typically don't need refrigeration
                .providesTracking(trackingNumber != null && !trackingNumber.isEmpty())
                .providesSmsUpdates(checkSmsSupport(shippingProvider))

                // Performance metrics - calculated from historical data
                .averageDeliveryDays(calculateAverageDeliveryDays(province, shippingProvider))
                .onTimeDeliveryRate(calculateOnTimeRate(province, shippingProvider))
                .successDeliveryRate(calculateSuccessRate(shippingProvider))
                .damageRate(calculateDamageRate(shippingProvider))

                // Coverage information - based on address data
                .coverageProvinces(calculateCoverageProvinces(shippingProvider))
                .coverageNationwide(checkNationwideCoverage(shippingProvider))
                .coverageInternational(false) // Facebook orders are domestic
                .build();
    }

    private void updateShippingInfoFromFacebook(ShippingInfo shipping, FacebookOrderDto orderDto) {
        FacebookOrderDto.FacebookOrderData data = orderDto.getData();

        if (data == null) {
            log.warn("No order data available for shipping info update: {}", orderDto.getOrderId());
            return;
        }

        // Extract updated shipping information
        String shippingProvider = extractShippingProvider(data);
        FacebookOrderDto.FacebookShippingAddress address = getShippingAddress(orderDto);
        String province = address != null ? address.getCityName() : null;
        String district = address != null ? address.getDistrictName() : null;

        // Update provider information if changed
        if (shippingProvider != null && !shippingProvider.equals(shipping.getProviderName())) {
            shipping.setProviderId(extractProviderId(data, shippingProvider));
            shipping.setProviderName(shippingProvider);
            shipping.setProviderType(determineProviderType(shippingProvider));
            shipping.setProviderTier(determineProviderTier(province, district));

            // Update provider-dependent capabilities
            shipping.setSupportsInsurance(checkInsuranceSupport(shippingProvider));
            shipping.setSupportsFragile(checkFragileSupport(shippingProvider));
            shipping.setProvidesSmsUpdates(checkSmsSupport(shippingProvider));
            shipping.setSuccessDeliveryRate(calculateSuccessRate(shippingProvider));
            shipping.setDamageRate(calculateDamageRate(shippingProvider));
            shipping.setCoverageProvinces(calculateCoverageProvinces(shippingProvider));
            shipping.setCoverageNationwide(checkNationwideCoverage(shippingProvider));
        }

        // Update service information
        shipping.setServiceType(determineServiceType(data));
        shipping.setServiceTier(determineServiceTier(data, province));
        shipping.setDeliveryCommitment(calculateDeliveryCommitment(data, province));
        shipping.setShippingMethod(extractShippingMethod(data));

        // Update delivery type based on current address
        if (address != null) {
            shipping.setDeliveryType(determineDeliveryType(address));
        }

        // Update fee information
        if (data.getShippingFee() != null) {
            shipping.setBaseFee(data.getShippingFee().doubleValue());
        }

        shipping.setWeightBasedFee(calculateWeightBasedFee(data));
        shipping.setDistanceBasedFee(calculateDistanceBasedFee(province, district));
        shipping.setCodFee(calculateCodFee(data));
        shipping.setInsuranceFee(calculateInsuranceFee(data));

        // Update service capabilities based on current order
        shipping.setSupportsCod(data.getCod() != null && data.getCod() > 0);

        // Update tracking capability
        String trackingNumber = extractTrackingNumber(data);
        shipping.setProvidesTracking(trackingNumber != null && !trackingNumber.isEmpty());

        // Update performance metrics based on location and provider
        if (shippingProvider != null && province != null) {
            shipping.setAverageDeliveryDays(calculateAverageDeliveryDays(province, shippingProvider));
            shipping.setOnTimeDeliveryRate(calculateOnTimeRate(province, shippingProvider));
        }

        log.debug("Updated shipping info for Facebook order: {} with provider: {}",
                orderDto.getOrderId(), shippingProvider);
    }

    // ===== PROCESSING DATE INFO =====
    private void processProcessingDateInfoUpsert(FacebookOrderDto orderDto) {
        String orderId = orderDto.getOrderId();
        Optional<ProcessingDateInfo> existing = processingDateInfoRepository.findById(orderId);

        if (existing.isPresent()) {
            ProcessingDateInfo dateInfo = existing.get();
            updateDateInfoFromFacebook(dateInfo, orderDto);
            processingDateInfoRepository.save(dateInfo);
        } else {
            ProcessingDateInfo newDateInfo = createDateInfoFromFacebook(orderDto);
            processingDateInfoRepository.save(newDateInfo);
        }
    }

    private ProcessingDateInfo createDateInfoFromFacebook(FacebookOrderDto orderDto) {
        LocalDateTime orderDate = parseDateTime(orderDto.getData().getCreatedAt());
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }

        return ProcessingDateInfo.builder()
                .orderId(orderDto.getOrderId())
                .dateKey(System.currentTimeMillis() % 1000000L)
                .fullDate(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(orderDate))
                .dayOfWeek(orderDate.getDayOfWeek().getValue())
                .dayOfWeekName(orderDate.getDayOfWeek().name())
                .dayOfMonth(orderDate.getDayOfMonth())
                .dayOfYear(orderDate.getDayOfYear())
                .weekOfYear(orderDate.get(java.time.temporal.WeekFields.ISO.weekOfYear()))
                .monthOfYear(orderDate.getMonthValue())
                .monthName(orderDate.getMonth().name())
                .quarterOfYear((orderDate.getMonthValue() - 1) / 3 + 1)
                .quarterName("Q" + ((orderDate.getMonthValue() - 1) / 3 + 1))
                .year(orderDate.getYear())
                .isWeekend(orderDate.getDayOfWeek().getValue() >= 6)
                .isHoliday(false)
                .holidayName(null)
                .isBusinessDay(orderDate.getDayOfWeek().getValue() <= 5)
                .fiscalYear(orderDate.getYear())
                .fiscalQuarter((orderDate.getMonthValue() - 1) / 3 + 1)
                .isShoppingSeason(isShoppingSeason(orderDate))
                .seasonName(getSeasonName(orderDate))
                .isPeakHour(isPeakHour(orderDate))
                .build();
    }

    private void updateDateInfoFromFacebook(ProcessingDateInfo existing, FacebookOrderDto orderDto) {
        ProcessingDateInfo newDateInfo = createDateInfoFromFacebook(orderDto);
        if (newDateInfo != null) {
            existing.setFullDate(newDateInfo.getFullDate());
            existing.setDayOfWeek(newDateInfo.getDayOfWeek());
            existing.setDayOfWeekName(newDateInfo.getDayOfWeekName());
            existing.setIsWeekend(newDateInfo.getIsWeekend());
            existing.setIsBusinessDay(newDateInfo.getIsBusinessDay());
        }
    }

    // ===== UTILITY METHODS =====

    private Integer generateDateKey(LocalDateTime date) {
        if (date == null) return null;

        LocalDate localDate = date.toLocalDate();
        return localDate.getYear() * 10000 + localDate.getMonthValue() * 100 + localDate.getDayOfMonth();
    }

    // Customer utilities
    private String getFirstPhoneNumber(List<String> phoneNumbers) {
        return phoneNumbers != null && !phoneNumbers.isEmpty() ? phoneNumbers.get(0) : null;
    }

    private String getFirstEmail(List<String> emails) {
        return emails != null && !emails.isEmpty() ? emails.get(0) : null;
    }


    private Double calculateAverageOrderValue(FacebookOrderDto.FacebookCustomer customer) {
        if (customer.getOrderCount() == null || customer.getOrderCount() == 0) {
            return 0.0;
        }
        Long totalAmount = customer.getPurchasedAmount() != null ? customer.getPurchasedAmount() : 0L;
        return totalAmount.doubleValue() / customer.getOrderCount();
    }

    // Order utilities
    private String getCustomerIdFromOrder(FacebookOrderDto orderDto) {
        if (orderDto.getData() != null && orderDto.getData().getCustomer() != null) {
            return orderDto.getData().getCustomer().getId();
        }
        return "UNKNOWN";
    }

    private Double calculateNetRevenue(FacebookOrderDto.FacebookOrderData data) {
        Double gross = data.getTotal() != null ? data.getTotal().doubleValue() : 0.0;
        Double shipping = data.getShippingFee() != null ? data.getShippingFee().doubleValue() : 0.0;
        Double tax = data.getTax() != null ? data.getTax().doubleValue() : 0.0;
        return gross - shipping - tax;
    }

    // Status mapping utilities
    private String mapFacebookStatus(Integer status) {
        if (status == null) return "UNKNOWN";

        switch (status) {
            case 1: return "PENDING";
            case 2: return "PROCESSING";
            case 3: return "SHIPPED";
            case 4: return "DELIVERED";
            case 5: return "CANCELLED";
            default: return "UNKNOWN";
        }
    }

    private String mapFacebookStatusName(String statusCode) {
        switch (statusCode) {
            case "1": return "Pending";
            case "2": return "Processing";
            case "3": return "Shipped";
            case "4": return "Delivered";
            case "5": return "Cancelled";
            default: return "Unknown";
        }
    }

    private String mapToStandardStatus(String facebookStatus) {
        switch (facebookStatus) {
            case "1": return "CREATED";
            case "2": return "PROCESSING";
            case "3": return "SHIPPED";
            case "4": return "COMPLETED";
            case "5": return "CANCELLED";
            default: return "UNKNOWN";
        }
    }

    private String mapToStandardStatusName(String facebookStatus) {
        switch (facebookStatus) {
            case "1": return "Order Created";
            case "2": return "Order Processing";
            case "3": return "Order Shipped";
            case "4": return "Order Completed";
            case "5": return "Order Cancelled";
            default: return "Unknown Status";
        }
    }

    private String mapToStatusCategory(String facebookStatus) {
        switch (facebookStatus) {
            case "1": return "INITIAL";
            case "2": return "PROCESSING";
            case "3": return "FULFILLMENT";
            case "4": return "COMPLETED";
            case "5": return "CANCELLED";
            default: return "OTHER";
        }
    }

    private String mapShippingStatus(Integer status) {
        if (status == null) return "PENDING";

        switch (status) {
            case 1: return "PENDING";
            case 2: return "PROCESSING";
            case 3: return "SHIPPED";
            case 4: return "DELIVERED";
            case 5: return "CANCELLED";
            default: return "PENDING";
        }
    }

    //Shipping Info
    private String extractShippingProvider(FacebookOrderDto.FacebookOrderData data) {
        // Từ tracking data trong Facebook response
        if (data.getDeliveryStatus() != null) {
            String status = data.getDeliveryStatus().toLowerCase();
            if (status.contains("ghn") || status.contains("giao hàng nhanh")) {
                return "Giao Hàng Nhanh";
            } else if (status.contains("j&t") || status.contains("jnt")) {
                return "J&T Express";
            } else if (status.contains("viettel") || status.contains("post")) {
                return "Viettel Post";
            } else if (status.contains("best") || status.contains("express")) {
                return "Best Express";
            }
        }

        // Từ order source name
        String orderSource = data.getOrderSource();
        if ("Facebook".equals(orderSource)) {
            return "Facebook Logistics Partner";
        }

        return "Unknown Provider";
    }

    /**
     * Extract tracking number from order data
     */
    private String extractTrackingNumber(FacebookOrderDto.FacebookOrderData data) {
        // Có thể có tracking number trong note hoặc delivery data
        if (data.getNote() != null && data.getNote().contains("tracking")) {
            // Extract tracking number from note if exists
            return extractTrackingFromNote(data.getNote());
        }

        return null;
    }

    /**
     * Extract delivery status from tags or status changes
     */
    private String extractDeliveryStatus(FacebookOrderDto.FacebookOrderData data) {
        if (data.getTags() != null) {
            for (FacebookOrderDto.FacebookTag tag : data.getTags()) {
                if (tag.getName() != null) {
                    String tagName = tag.getName().toLowerCase();
                    if (tagName.contains("delivery") || tagName.contains("shipped") ||
                            tagName.contains("picked up") || tagName.contains("on delivery")) {
                        return tag.getName();
                    }
                }
            }
        }

        return "Pending";
    }

    /**
     * Calculate delivery commitment based on address and historical data
     */
    private String calculateDeliveryCommitment(FacebookOrderDto.FacebookOrderData data, String province) {
        if (province == null) return "3_DAYS";

        String prov = province.toLowerCase();

        // Major cities - faster delivery
        if (prov.contains("hồ chí minh") || prov.contains("hà nội")) {
            return "1_DAY";
        } else if (prov.contains("đà nẵng") || prov.contains("cần thơ") ||
                prov.contains("hải phòng") || prov.contains("biên hòa")) {
            return "2_DAYS";
        } else {
            return "3_DAYS";
        }
    }

    /**
     * Calculate COD fee based on actual order data
     */
    private Double calculateCodFee(FacebookOrderDto.FacebookOrderData data) {
        if (data.getCod() == null || data.getCod() == 0) {
            return 0.0;
        }

        // COD fee is typically a percentage or fixed amount
        Long codAmount = data.getCod();
        if (codAmount < 100000) {
            return 3000.0; // Low value orders
        } else if (codAmount < 500000) {
            return 5000.0; // Medium value orders
        } else {
            return 10000.0; // High value orders
        }
    }

    /**
     * Calculate weight-based fee from order items
     */
    private Double calculateWeightBasedFee(FacebookOrderDto.FacebookOrderData data) {
        if (data.getItems() == null) return 0.0;

        // Estimate weight based on number of items and type
        int itemCount = data.getItems().size();
        return itemCount * 500.0; // 500 VND per estimated kg
    }

    /**
     * Calculate distance-based fee from province/district
     */
    private Double calculateDistanceBasedFee(String province, String district) {
        if (province == null) return 0.0;

        String prov = province.toLowerCase();

        // Urban areas - no extra distance fee
        if (prov.contains("hồ chí minh") || prov.contains("hà nội")) {
            return 0.0;
        }

        // Secondary cities - moderate fee
        if (prov.contains("đà nẵng") || prov.contains("cần thơ")) {
            return 2000.0;
        }

        // Remote areas - higher fee
        return 5000.0;
    }

    /**
     * Calculate insurance fee based on order value
     */
    private Double calculateInsuranceFee(FacebookOrderDto.FacebookOrderData data) {
        if (data.getTotal() == null) return 0.0;

        Long totalAmount = data.getTotal();
        if (totalAmount > 1000000) { // Orders > 1M VND
            return totalAmount * 0.001; // 0.1% insurance
        }

        return 0.0;
    }

    /**
     * Check if provider supports insurance
     */
    private Boolean checkInsuranceSupport(String provider) {
        if (provider == null) return false;

        String prov = provider.toLowerCase();
        return prov.contains("ghn") || prov.contains("j&t") || prov.contains("viettel");
    }

    /**
     * Check if provider supports fragile items
     */
    private Boolean checkFragileSupport(String provider) {
        if (provider == null) return false;

        String prov = provider.toLowerCase();
        return prov.contains("ghn") || prov.contains("viettel") || prov.contains("best");
    }

    /**
     * Check if provider supports SMS updates
     */
    private Boolean checkSmsSupport(String provider) {
        if (provider == null) return false;

        String prov = provider.toLowerCase();
        return prov.contains("ghn") || prov.contains("j&t") || prov.contains("viettel");
    }

    /**
     * Calculate average delivery days based on province and provider
     */
    private Double calculateAverageDeliveryDays(String province, String provider) {
        if (province == null) return 3.0;

        String prov = province.toLowerCase();
        double baseDays = 3.0;

        // Adjust by location
        if (prov.contains("hồ chí minh") || prov.contains("hà nội")) {
            baseDays = 1.5;
        } else if (prov.contains("đà nẵng") || prov.contains("cần thơ")) {
            baseDays = 2.0;
        }

        // Adjust by provider performance
        if (provider != null && provider.toLowerCase().contains("ghn")) {
            baseDays *= 0.9; // GHN is typically faster
        } else if (provider != null && provider.toLowerCase().contains("j&t")) {
            baseDays *= 0.95; // J&T is reliable
        }

        return baseDays;
    }

    /**
     * Calculate on-time delivery rate
     */
    private Double calculateOnTimeRate(String province, String provider) {
        double baseRate = 0.80;

        // Urban areas have better rates
        if (province != null) {
            String prov = province.toLowerCase();
            if (prov.contains("hồ chí minh") || prov.contains("hà nội")) {
                baseRate = 0.95;
            } else if (prov.contains("đà nẵng") || prov.contains("cần thơ")) {
                baseRate = 0.90;
            }
        }

        // Provider reliability
        if (provider != null) {
            String prov = provider.toLowerCase();
            if (prov.contains("ghn") || prov.contains("viettel")) {
                baseRate += 0.05;
            }
        }

        return Math.min(baseRate, 1.0);
    }

    private String extractProviderId(FacebookOrderDto.FacebookOrderData data, String provider) {
        if (provider == null) return "FB_LOGISTICS";

        String prov = provider.toLowerCase();
        if (prov.contains("ghn")) return "GHN_001";
        if (prov.contains("j&t")) return "JNT_001";
        if (prov.contains("viettel")) return "VTP_001";
        if (prov.contains("best")) return "BEST_001";

        return "FB_PARTNER_001";
    }

    private String determineProviderType(String provider) {
        if (provider == null) return "PLATFORM";

        String prov = provider.toLowerCase();
        if (prov.contains("facebook") || prov.contains("platform")) {
            return "PLATFORM";
        }

        return "THIRD_PARTY";
    }

    private String determineServiceType(FacebookOrderDto.FacebookOrderData data) {
        // Check if urgent delivery from notes
        if (data.getNote() != null && data.getNote().toLowerCase().contains("urgent")) {
            return "EXPRESS";
        }

        return "STANDARD";
    }

    private String determinePickupType(FacebookOrderDto.FacebookOrderData data) {
        // Facebook orders typically come from stores
        return "STORE";
    }

    private String determineDeliveryType(FacebookOrderDto.FacebookShippingAddress address) {
        if (address == null) return "HOME";

        // Check if delivery to office/business
        if (address.getFullAddress() != null) {
            String addr = address.getFullAddress().toLowerCase();
            if (addr.contains("company") || addr.contains("office") || addr.contains("công ty")) {
                return "OFFICE";
            }
        }

        return "HOME";
    }

    private String extractTrackingFromNote(String note) {
        // Simple regex to extract tracking numbers from note
        // This would need to be more sophisticated in real implementation
        if (note.matches(".*\\d{10,}.*")) {
            return "TRACKING_FROM_NOTE";
        }
        return null;
    }

    private String determineProviderTier(String province, String district) {
        if (province == null) return "BASIC";

        String prov = province.toLowerCase();

        // PREMIUM tier for major cities with full infrastructure
        if (prov.contains("hồ chí minh") || prov.contains("hà nội")) {
            return "PREMIUM";
        }

        // STANDARD tier for secondary cities
        if (prov.contains("đà nẵng") || prov.contains("cần thơ") ||
                prov.contains("hải phòng") || prov.contains("biên hòa") ||
                prov.contains("nha trang") || prov.contains("huế")) {
            return "STANDARD";
        }

        // BASIC tier for rural/remote areas
        if (district == null || district.trim().isEmpty()) {
            return "BASIC";
        }

        return "STANDARD";
    }

    /**
     * Determine service tier based on order data and location
     */
    private String determineServiceTier(FacebookOrderDto.FacebookOrderData data, String province) {
        if (data == null) return "BASIC";

        // PREMIUM service for high-value orders in urban areas
        if (data.getTotal() != null && data.getTotal() > 1000000) {
            if (province != null && (province.toLowerCase().contains("hồ chí minh") ||
                    province.toLowerCase().contains("hà nội"))) {
                return "PREMIUM";
            }
        }

        // STANDARD service for medium-value orders or urban areas
        if ((data.getTotal() != null && data.getTotal() > 300000) ||
                (province != null && (province.toLowerCase().contains("đà nẵng") ||
                        province.toLowerCase().contains("cần thơ")))) {
            return "STANDARD";
        }

        // Check for express service indicators in notes
        if (data.getNote() != null) {
            String note = data.getNote().toLowerCase();
            if (note.contains("express") || note.contains("urgent") ||
                    note.contains("nhanh") || note.contains("gấp")) {
                return "EXPRESS";
            }
        }

        return "BASIC";
    }

    /**
     * Extract shipping method from order data or notes
     */
    private String extractShippingMethod(FacebookOrderDto.FacebookOrderData data) {
        if (data == null) return "Standard";

        // Check notes for shipping method hints
        if (data.getNote() != null) {
            String note = data.getNote().toLowerCase();

            if (note.contains("express") || note.contains("nhanh")) {
                return "Express";
            }

            if (note.contains("same day") || note.contains("trong ngày")) {
                return "Same Day";
            }

            if (note.contains("economy") || note.contains("tiết kiệm")) {
                return "Economy";
            }

            if (note.contains("overnight") || note.contains("qua đêm")) {
                return "Overnight";
            }
        }

        // Check delivery status for method hints
        if (data.getDeliveryStatus() != null) {
            String status = data.getDeliveryStatus().toLowerCase();

            if (status.contains("express") || status.contains("fast")) {
                return "Express";
            }
        }

        // Check tags for shipping method
        if (data.getTags() != null) {
            for (FacebookOrderDto.FacebookTag tag : data.getTags()) {
                if (tag.getName() != null) {
                    String tagName = tag.getName().toLowerCase();
                    if (tagName.contains("express")) {
                        return "Express";
                    }
                    if (tagName.contains("standard")) {
                        return "Standard";
                    }
                }
            }
        }

        // Default based on order value and location
        if (data.getTotal() != null && data.getTotal() > 1000000) {
            return "Premium";
        }

        return "Standard";
    }

    /**
     * Calculate success delivery rate based on provider
     */
    private Double calculateSuccessRate(String provider) {
        if (provider == null) return 0.85;

        String prov = provider.toLowerCase();

        // Known provider success rates in Vietnam market
        if (prov.contains("ghn") || prov.contains("giao hàng nhanh")) {
            return 0.95; // GHN has excellent success rate
        }

        if (prov.contains("j&t")) {
            return 0.92; // J&T has good success rate
        }

        if (prov.contains("viettel")) {
            return 0.90; // Viettel Post reliable
        }

        if (prov.contains("best")) {
            return 0.88; // Best Express decent rate
        }

        if (prov.contains("facebook") || prov.contains("platform")) {
            return 0.85; // Facebook logistics partners
        }

        // Unknown/generic providers
        return 0.80;
    }

    /**
     * Calculate damage rate based on provider reputation
     */
    private Double calculateDamageRate(String provider) {
        if (provider == null) return 0.05;

        String prov = provider.toLowerCase();

        // Known provider damage rates (lower is better)
        if (prov.contains("ghn")) {
            return 0.01; // GHN has excellent handling
        }

        if (prov.contains("viettel")) {
            return 0.015; // Viettel Post careful handling
        }

        if (prov.contains("j&t")) {
            return 0.02; // J&T good handling
        }

        if (prov.contains("best")) {
            return 0.03; // Best Express average handling
        }

        if (prov.contains("facebook") || prov.contains("platform")) {
            return 0.04; // Platform partners variable quality
        }

        // Unknown providers - conservative estimate
        return 0.06;
    }

    /**
     * Calculate coverage provinces based on provider network
     */
    private String calculateCoverageProvinces(String provider) {
        if (provider == null) return "LIMITED";

        String prov = provider.toLowerCase();

        // Major providers with nationwide coverage
        if (prov.contains("ghn") || prov.contains("j&t") || prov.contains("viettel")) {
            return "ALL_63_PROVINCES";
        }

        if (prov.contains("best")) {
            return "MAJOR_CITIES_50_PROVINCES";
        }

        if (prov.contains("facebook") || prov.contains("platform")) {
            return "URBAN_AREAS_30_PROVINCES";
        }

        // Unknown providers - assume limited coverage
        return "LIMITED_10_PROVINCES";
    }

    /**
     * Check if provider has nationwide coverage
     */
    private Boolean checkNationwideCoverage(String provider) {
        if (provider == null) return false;

        String prov = provider.toLowerCase();

        // Providers known to have full Vietnam coverage
        return prov.contains("ghn") ||
                prov.contains("j&t") ||
                prov.contains("viettel") ||
                prov.contains("vietnam post");
    }

    // Boolean status checks
    private boolean isDelivered(Integer status) {
        return status != null && status == 4;
    }

    private boolean isCancelled(Integer status) {
        return status != null && status == 5;
    }

    private boolean isPaid(FacebookOrderDto.FacebookOrderData data) {
        return data.getCash() != null && data.getCash() > 0;
    }

    private boolean isActiveOrder(Integer status) {
        return status != null && status != 4 && status != 5;
    }

    private boolean isCompletedOrder(Integer status) {
        return status != null && status == 4;
    }

    private boolean isRevenueRecognized(Integer status) {
        return status != null && status == 4;
    }

    private boolean isRefundable(Integer status) {
        return status != null && (status == 4 || status == 5);
    }

    private boolean isCancellable(Integer status) {
        return status != null && status != 4 && status != 5;
    }

    private boolean isTrackable(Integer status) {
        return status != null && status >= 2 && status <= 4;
    }

    // Address utilities
    private FacebookOrderDto.FacebookShippingAddress getShippingAddress(FacebookOrderDto orderDto) {
        if (orderDto.getData() != null && orderDto.getData().getShippingAddress() != null) {
            return orderDto.getData().getShippingAddress();
        }

        // Fallback to customer address
        if (orderDto.getData() != null && orderDto.getData().getCustomer() != null &&
                orderDto.getData().getCustomer().getShopCustomerAddresses() != null &&
                !orderDto.getData().getCustomer().getShopCustomerAddresses().isEmpty()) {
            return orderDto.getData().getCustomer().getShopCustomerAddresses().get(0);
        }

        return null;
    }

    // Date utilities
    private LocalDateTime parseDateTime(String dateString) {
        if (dateString == null) {
            return null;
        }

        try {
            // Handle Facebook date format: "2025-07-05T07:45:47"
            if (dateString.contains("T")) {
                return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            }
            // Handle simple date format
            return LocalDate.parse(dateString).atStartOfDay();
        } catch (Exception e) {
            log.warn("Failed to parse date: {}, using current time", dateString);
            return LocalDateTime.now();
        }
    }

    // Status UI utilities
    private String getStatusColor(String statusCode) {
        switch (statusCode) {
            case "1": return "GRAY";
            case "2": return "BLUE";
            case "3": return "ORANGE";
            case "4": return "GREEN";
            case "5": return "RED";
            default: return "GRAY";
        }
    }

    private String getStatusIcon(String statusCode) {
        switch (statusCode) {
            case "1": return "clock";
            case "2": return "cog";
            case "3": return "truck";
            case "4": return "check";
            case "5": return "x";
            default: return "question";
        }
    }

    // Date analysis utilities
    private boolean isShoppingSeason(LocalDateTime date) {
        int month = date.getMonthValue();
        return month == 11 || month == 12 || month == 1; // Nov, Dec, Jan
    }

    private String getSeasonName(LocalDateTime date) {
        int month = date.getMonthValue();
        if (month >= 3 && month <= 5) return "SPRING";
        if (month >= 6 && month <= 8) return "SUMMER";
        if (month >= 9 && month <= 11) return "AUTUMN";
        return "WINTER";
    }

    private boolean isPeakHour(LocalDateTime date) {
        int hour = date.getHour();
        return (hour >= 9 && hour <= 12) || (hour >= 19 && hour <= 22);
    }

    private Double calculateShippingCostRatio(FacebookOrderDto.FacebookOrderData data) {
        if (data.getTotal() == null || data.getTotal() == 0 || data.getShippingFee() == null) {
            return 0.0;
        }
        return data.getShippingFee().doubleValue() / data.getTotal().doubleValue();
    }

    // ===== RESULT CLASSES =====
    public static class EtlResult {
        public boolean success;
        public int totalOrders;
        public int ordersProcessed;
        public long durationMs;
        public double successRate;
        public String errorMessage;
        public List<FailedOrder> failedOrders;

        // Getters for compatibility
        public boolean isSuccess() { return success; }
        public int getTotalOrders() { return totalOrders; }
        public int getOrdersProcessed() { return ordersProcessed; }
        public long getDurationMs() { return durationMs; }
        public double getSuccessRate() { return successRate; }
        public String getErrorMessage() { return errorMessage; }
        public List<FailedOrder> getFailedOrders() { return failedOrders; }
    }

    public static class FailedOrder {
        public String orderId;
        public String errorMessage;

        public FailedOrder(String orderId, String errorMessage) {
            this.orderId = orderId;
            this.errorMessage = errorMessage;
        }

        public String getOrderId() { return orderId; }
        public String getErrorMessage() { return errorMessage; }
    }
}