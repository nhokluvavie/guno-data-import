// TikTokEtlService.java - TikTok ETL Service (Clean Implementation)
package com.guno.etl.service;

import com.guno.etl.dto.ShopeeOrderDto;
import com.guno.etl.dto.TikTokApiResponse;
import com.guno.etl.dto.TikTokOrderDto;
import com.guno.etl.dto.TikTokItemDto;
import com.guno.etl.entity.*;
import com.guno.etl.repository.*;
import com.guno.etl.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TikTokEtlService {

    private static final Logger log = LoggerFactory.getLogger(TikTokEtlService.class);

    @Autowired
    private TikTokApiService tikTokApiService;

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

    @Value("${etl.platforms.tiktok.name:TIKTOK}")
    private String platformName;

    // ===== MAIN ETL METHOD =====
    public EtlResult processOrdersForDate(String dateString) {
        log.info("Starting TikTok ETL process for date: {}", dateString);

        long startTime = System.currentTimeMillis();
        int totalOrders = 0;
        int ordersProcessed = 0;
        List<FailedOrder> failedOrders = new ArrayList<>();

        try {
            // Fetch orders for specific date from API
            TikTokApiResponse response = tikTokApiService.fetchAllOrdersForDate(dateString);

            if (response == null || response.getData() == null) {
                log.warn("No TikTok data received for date: {}", dateString);

                // Create EtlResult using constructor
                EtlResult result = new EtlResult();
                result.success = true;
                result.totalOrders = 0;
                result.ordersProcessed = 0;
                result.errorMessage = "No data available for the specified date";
                result.failedOrders = failedOrders;
                return result;
            }

            List<TikTokOrderDto> orders = response.getData().getOrders();
            totalOrders = orders.size();

            log.info("Processing {} TikTok orders for date: {}", totalOrders, dateString);

            // Process each order
            for (TikTokOrderDto orderDto : orders) {
                try {
                    processOrderUpsert(orderDto);
                    ordersProcessed++;

                    if (ordersProcessed % 10 == 0) {
                        log.info("Processed {} / {} TikTok orders for date: {}",
                                ordersProcessed, totalOrders, dateString);
                    }

                } catch (Exception e) {
                    log.error("Failed to process TikTok order {} for date {}: {}",
                            orderDto.getOrderId(), dateString, e.getMessage());

                    failedOrders.add(new FailedOrder(orderDto.getOrderId(), e.getMessage()));
                }
            }

            long durationMs = System.currentTimeMillis() - startTime;
            double successRate = totalOrders > 0 ? (double) ordersProcessed / totalOrders * 100 : 0;

            log.info("Completed TikTok ETL for date: {} - {}/{} orders processed ({}%) in {} ms",
                    dateString, ordersProcessed, totalOrders, String.format("%.1f", successRate), durationMs);

            // Create success result
            EtlResult result = new EtlResult();
            result.success = (ordersProcessed > 0 || totalOrders == 0);
            result.totalOrders = totalOrders;
            result.ordersProcessed = ordersProcessed;
            result.errorMessage = failedOrders.isEmpty() ? null :
                    String.format("%d orders failed processing", failedOrders.size());
            result.failedOrders = failedOrders;
            return result;

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            log.error("TikTok ETL failed for date {}: {}", dateString, e.getMessage());

            // Create failure result
            EtlResult result = new EtlResult();
            result.success = false;
            result.totalOrders = totalOrders;
            result.ordersProcessed = ordersProcessed;
            result.errorMessage = e.getMessage();
            result.failedOrders = failedOrders;
            return result;
        }
    }

    @Transactional
    public EtlResult processUpdatedOrders() {
        log.info("=== Starting TikTok ETL process ===");

        EtlResult result = new EtlResult();
        result.setStartTime(LocalDateTime.now());

        try {
            // Fetch data from TikTok API
            TikTokApiResponse response = tikTokApiService.fetchUpdatedOrders();

            if (response == null || response.getStatus() != 1) {
                result.setSuccess(false);
                result.setErrorMessage("TikTok API call failed");
                return result;
            }

            List<TikTokOrderDto> orders = response.getData().getOrders();
            if (orders == null || orders.isEmpty()) {
                log.info("No TikTok orders to process");
                result.setSuccess(true);
                result.setTotalOrders(0);
                return result;
            }

            // Process each order
            int successCount = 0;
            for (TikTokOrderDto orderDto : orders) {
                try {
                    processOrderUpsert(orderDto);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to process TikTok order: {}", orderDto.getOrderId(), e);
                    result.addFailedOrder(orderDto.getOrderId(), e.getMessage());
                }
            }

            result.setSuccess(true);
            result.setTotalOrders(orders.size());
            result.setOrdersProcessed(successCount);
            result.setEndTime(LocalDateTime.now());

            log.info("TikTok ETL completed: {}/{} orders processed", successCount, orders.size());
            return result;

        } catch (Exception e) {
            log.error("TikTok ETL process failed", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setEndTime(LocalDateTime.now());
            return result;
        }
    }

    // ===== PROCESS SINGLE ORDER =====

    private void processOrderUpsert(TikTokOrderDto orderDto) {
        String orderId = orderDto.getOrderId();
        log.debug("Processing TikTok order: {}", orderId);

        // Process tables individually to isolate errors
        try { processCustomerUpsert(orderDto); } catch (Exception e) { log.error("Customer upsert failed for order {}", orderId, e); }
        try { processOrderEntityUpsert(orderDto); } catch (Exception e) { log.error("Order upsert failed for order {}", orderId, e); }
        try { processProductsUpsert(orderDto); } catch (Exception e) { log.error("Products upsert failed for order {}", orderId, e); }
        try { processOrderItemsUpsert(orderDto); } catch (Exception e) { log.error("Order items upsert failed for order {}", orderId, e); }
        try { processGeographyUpsert(orderDto); } catch (Exception e) { log.error("Geography upsert failed for order {}", orderId, e); }

        try { processDateInfoUpsert(orderDto); } catch (Exception e) { log.error("Date info upsert failed for order {}", orderId, e); }
        try { processPaymentInfoUpsert(orderDto); } catch (Exception e) { log.error("Payment info upsert failed for order {}", orderId, e); }
        try { processShippingInfoUpsert(orderDto); } catch (Exception e) { log.error("Shipping info upsert failed for order {}", orderId, e); }
        try { processStatusInfoUpsert(orderDto); } catch (Exception e) { log.error("Status info upsert failed for order {}", orderId, e); }
        try { processOrderStatusTransition(orderDto); } catch (Exception e) { log.error("Order Status info transit failed for order {}", orderId, e); }
        try { processOrderStatusDetailUpsert(orderDto); } catch (Exception e) { log.error("Order Status Detail info upsert failed for order {}", orderId, e); }
    }

    // ===== CUSTOMER PROCESSING =====

    private void processCustomerUpsert(TikTokOrderDto orderDto) {
        if (orderDto.getData() == null) {
            return;
        }

        String phone = orderDto.getData().getRecipientAddress().getPhoneNumber().isEmpty() ? "Unknown" : orderDto.getData().getRecipientAddress().getPhoneNumber();
        if (phone == null) return;

        String phoneHash = HashUtil.hashPhone(phone);
        String customerId = orderDto.getData().getUserId() != null ? orderDto.getData().getUserId() : HashUtil.generateCustomerId(platformName, phoneHash);

        Optional<Customer> existing = customerRepository.findById(customerId);

        if (existing.isPresent()) {
            // Update existing customer
            Customer customer = existing.get();
            updateCustomerFromTikTok(customer, orderDto);
            customerRepository.save(customer);
            log.debug("Updated TikTok customer: {}", customerId);
        } else {
            // Create new customer
            Customer newCustomer = createCustomerFromTikTok(orderDto, phoneHash, customerId);
            customerRepository.save(newCustomer);
            log.debug("Created new TikTok customer: {}", customerId);
        }
    }

    private Customer createCustomerFromTikTok(TikTokOrderDto orderDto, String phoneHash, String customerId) {
        String email = orderDto.getData().getBuyerEmail();
        String emailHash = email != null ? HashUtil.hashEmail(email) : null;
        Double totalAmount = parseAmount(orderDto.getData().getPayment().getTotalAmount());

        return Customer.builder()
                .customerId(customerId)
                .customerKey(System.currentTimeMillis() % 1000000L)
                .platformCustomerId(orderDto.getData().getUserId())
                .phoneHash(phoneHash)
                .emailHash(emailHash)
                .gender(null)
                .ageGroup(null)
                .customerSegment("REGULAR")
                .customerTier("STANDARD")
                .acquisitionChannel("TIKTOK")
                .firstOrderDate(convertTimestamp(orderDto.getCreateTime()))
                .lastOrderDate(convertTimestamp(orderDto.getCreateTime()))
                .totalOrders(1)
                .totalSpent(totalAmount)
                .averageOrderValue(totalAmount)
                .totalItemsPurchased(countTikTokItems(orderDto))
                .daysSinceFirstOrder(0)
                .daysSinceLastOrder(0)
                .purchaseFrequencyDays(0.0)
                .returnRate(0.0)
                .cancellationRate(0.0)
                .codPreferenceRate(orderDto.getData().getIsCod() ? 1.0 : 0.0)
                .favoriteCategory(null)
                .favoriteBrand(null)
                .preferredPaymentMethod(orderDto.getData().getIsCod() ? "COD" : "PREPAID")
                .preferredPlatform("TIKTOK")
                .primaryShippingProvince(extractProvince(orderDto))
                .shipsToMultipleProvinces(false)
                .loyaltyPoints(0)
                .referralCount(0)
                .isReferrer(false)
                .build();
    }

    private void updateCustomerFromTikTok(Customer customer, TikTokOrderDto orderDto) {
        customer.setLastOrderDate(convertTimestamp(orderDto.getCreateTime()));
        customer.setTotalOrders(customer.getTotalOrders() + 1);
        customer.setTotalSpent(customer.getTotalSpent() + parseAmount(orderDto.getData().getPayment().getTotalAmount()));
        customer.setAverageOrderValue(customer.getTotalSpent() / customer.getTotalOrders());
        customer.setTotalItemsPurchased(customer.getTotalItemsPurchased() + countTikTokItems(orderDto));
    }

    // ===== ORDER PROCESSING =====

    private void processOrderEntityUpsert(TikTokOrderDto orderDto) {
        String orderId = orderDto.getOrderId();

        Optional<Order> existing = orderRepository.findById(orderId);

        if (existing.isPresent()) {
            // Update existing order
            Order order = existing.get();
            updateOrderFromTikTok(order, orderDto);
            orderRepository.save(order);
            log.debug("Updated TikTok order: {}", orderId);
        } else {
            // Create new order
            Order newOrder = createOrderFromTikTok(orderDto);
            orderRepository.save(newOrder);
            log.debug("Created new TikTok order: {}", orderId);
        }
    }

    private Order createOrderFromTikTok(TikTokOrderDto orderDto) {
        String phoneHash = HashUtil.hashPhone(orderDto.getData().getRecipientAddress().getPhoneNumber().isEmpty() ? "Unknown" : orderDto.getData().getRecipientAddress().getPhoneNumber());
        String customerId = orderDto.getData().getUserId() != null ? orderDto.getData().getUserId() : HashUtil.generateCustomerId(platformName, phoneHash);
        Double totalAmount = parseAmount(orderDto.getData().getPayment().getTotalAmount());
        Double shippingFee = parseAmount(orderDto.getData().getPayment().getShippingFee());

        return Order.builder()
                .orderId(orderDto.getOrderId())
                .customerId(customerId)
                .shopId(orderDto.getShopId())
                .internalUuid(orderDto.getId())
                .orderCount(1)
                .itemQuantity(countTikTokItems(orderDto))
                .totalItemsInOrder(countTikTokItems(orderDto))
                .grossRevenue(totalAmount)
                .netRevenue(totalAmount - shippingFee)
                .shippingFee(shippingFee)
                .taxAmount(parseAmount(orderDto.getData().getPayment().getTax()))
                .discountAmount(parseAmount(orderDto.getData().getPayment().getSellerDiscount()))
                .codAmount(orderDto.getData().getIsCod() ? totalAmount : 0.0)
                .platformFee(0.0)
                .sellerDiscount(parseAmount(orderDto.getData().getPayment().getSellerDiscount()))
                .platformDiscount(parseAmount(orderDto.getData().getPayment().getPlatformDiscount()))
                .originalPrice(parseAmount(orderDto.getData().getPayment().getOriginalTotalProductPrice()))
                .estimatedShippingFee(parseAmount(orderDto.getData().getPayment().getOriginalShippingFee()))
                .actualShippingFee(shippingFee)
                .shippingWeightGram(500) // Default
                .daysToShip(1)
                .isDelivered("DELIVERED".equals(orderDto.getStatus()))
                .isCancelled("CANCELLED".equals(orderDto.getStatus()))
                .isReturned(false)
                .isCod(orderDto.getData().getIsCod())
                .isNewCustomer(true)
                .isRepeatCustomer(false)
                .isBulkOrder(false)
                .isPromotionalOrder(hasDiscount(orderDto))
                .isSameDayDelivery(false)
                .orderToShipHours(24)
                .shipToDeliveryHours(48)
                .totalFulfillmentHours(72)
                .customerOrderSequence(1)
                .customerLifetimeOrders(1)
                .customerLifetimeValue(totalAmount)
                .daysSinceLastOrder(0)
                .promotionImpact(parseAmount(orderDto.getData().getPayment().getSellerDiscount()))
                .adRevenue(0.0)
                .organicRevenue(totalAmount)
                .aov(totalAmount)
                .shippingCostRatio(shippingFee / totalAmount)
                .createdAt(convertTimestamp(orderDto.getCreateTime()))
                .rawData(1)
                .platformSpecificData(1)
                .build();
    }

    private void updateOrderFromTikTok(Order order, TikTokOrderDto orderDto) {
        order.setIsDelivered("DELIVERED".equals(orderDto.getStatus()));
        order.setIsCancelled("CANCELLED".equals(orderDto.getStatus()));
        // Update other fields as needed
    }

    // ===== PRODUCT PROCESSING =====

    private void processProductsUpsert(TikTokOrderDto orderDto) {
        if (orderDto.getData() == null || orderDto.getData().getLineItems() == null) {
            return;
        }

        for (TikTokItemDto item : orderDto.getData().getLineItems()) {
            try {
                processProductUpsert(item);
            } catch (Exception e) {
                log.error("Failed to process TikTok product: {}", item.getSkuId(), e);
            }
        }
    }

    private void processProductUpsert(TikTokItemDto item) {
        String sku = item.getSellerSku();
        String platformProductId = platformName;

        ProductId productId = new ProductId(sku, platformProductId);
        Optional<Product> existing = productRepository.findById(productId);

        if (existing.isPresent()) {
            Product product = existing.get();
            updateProductFromTikTok(product, item);
            productRepository.save(product);
            log.debug("Updated TikTok product: {}", sku);
        } else {
            Product newProduct = createProductFromTikTok(item);
            productRepository.save(newProduct);
            log.debug("Created new TikTok product: {}", sku);
        }
    }

    private Product createProductFromTikTok(TikTokItemDto item) {
        return Product.builder()
                .sku(item.getSellerSku())
                .platformProductId(platformName)
                .productId(item.getProductId())
                .variationId(item.getSellerSku())
                .barcode(null)
                .productName(item.getProductName())
                .productDescription("TikTok product")
                .brand("TikTok Brand")
                .model("TikTok Model")
                .categoryLevel1("General")
                .categoryLevel2("TikTok Category")
                .categoryLevel3("TikTok Product")
                .categoryPath("General > TikTok Category > TikTok Product")
                .color("Default")
                .size("Default")
                .material("Default")
                .weightGram(500)
                .dimensions("10x10x10")
                .costPrice(parseAmount(item.getSalePrice()) * 0.7)
                .retailPrice(parseAmount(item.getSalePrice()))
                .originalPrice(parseAmount(item.getOriginalPrice()))
                .priceRange("MEDIUM")
                .isActive(true)
                .isFeatured(false)
                .isSeasonal(false)
                .isNewArrival(true)
                .isBestSeller(false)
                .primaryImageUrl(item.getSkuImage())
                .imageCount(item.getSkuImage() != null ? 1 : 0)
                .seoTitle(item.getProductName())
                .seoKeywords("tiktok,product")
                .build();
    }

    private void updateProductFromTikTok(Product product, TikTokItemDto item) {
        product.setRetailPrice(parseAmount(item.getSalePrice()));
        product.setOriginalPrice(parseAmount(item.getOriginalPrice()));
        if (item.getSkuImage() != null) {
            product.setPrimaryImageUrl(item.getSkuImage());
        }
    }

    // ===== ORDER ITEMS PROCESSING =====
    private void processOrderItemsUpsert(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            log.debug("Processing order items for order: {}", orderId);

            if (orderDto.getData() == null || orderDto.getData().getLineItems() == null) {
                log.warn("No line items found for order: {}", orderId);
                return;
            }

            // ✅ DELETE existing items first (like Shopee)
            List<OrderItem> existingItems = orderItemRepository.findByOrderIdOrderByItemSequence(orderId);
            if (!existingItems.isEmpty()) {
                orderItemRepository.deleteAll(existingItems);
                log.debug("Deleted {} existing items for order: {}", existingItems.size(), orderId);
            }

            // ✅ INSERT new items (like Shopee)
            List<TikTokItemDto> items = orderDto.getData().getLineItems();
            for (int i = 0; i < items.size(); i++) {
                TikTokItemDto item = items.get(i);
                try {
                    OrderItem orderItem = createOrderItemFromTikTokItem(orderDto, item, i + 1);
                    if (orderItem != null) {
                        orderItemRepository.save(orderItem);
                        log.debug("Created order item for order: {} - SKU: {}", orderId, item.getSkuId());
                    } else {
                        log.warn("Failed to create order item entity for order: {} - SKU: {}", orderId, item.getSkuId());
                    }
                } catch (Exception e) {
                    log.error("Failed to process order item for order: {} - SKU: {}", orderId, item.getSkuId(), e);
                    // Continue với items khác
                }
            }

            log.debug("Order items processing completed for order: {}", orderId);

        } catch (Exception e) {
            log.error("Failed to process order items for order {}: {}", orderDto.getOrderId(), e.getMessage());
            throw e; // ✅ Re-throw để trigger rollback
        }
    }

    private OrderItem createOrderItemFromTikTokItem(TikTokOrderDto orderDto, TikTokItemDto item, int itemSequence) {
        try {
            String orderId = orderDto.getOrderId();
            String sku = item.getSellerSku();

            return OrderItem.builder()
                    .orderId(orderId)
                    .sku(sku)
                    .platformProductId(platformName) // "TIKTOK"
                    .quantity(1) // TikTok default quantity
                    .unitPrice(parseAmount(item.getSalePrice()))
                    .totalPrice(parseAmount(item.getSalePrice()))
                    .itemDiscount(parseAmount(item.getOriginalPrice()) - parseAmount(item.getSalePrice()))
                    .promotionType(hasItemDiscount(item) ? "SALE" : null)
                    .promotionCode(null)
                    .itemStatus(orderDto.getStatus())
                    .itemSequence(itemSequence)
                    .opId(System.currentTimeMillis() % 1000000L)
                    .build();

        } catch (Exception e) {
            log.error("Failed to create OrderItem entity for SKU: {}", item.getSkuId(), e);
            return null;
        }
    }

    // ===== GEOGRAPHY PROCESSING =====

    private void processGeographyUpsert(TikTokOrderDto orderDto) {
        String orderId = orderDto.getOrderId();

        Optional<GeographyInfo> existing = geographyInfoRepository.findById(orderId);

        if (existing.isPresent()) {
            GeographyInfo geography = existing.get();
            updateGeographyFromTikTok(geography, orderDto);
            geographyInfoRepository.save(geography);
            log.debug("Updated TikTok geography: {}", orderId);
        } else {
            GeographyInfo newGeography = createGeographyFromTikTok(orderDto);
            geographyInfoRepository.save(newGeography);
            log.debug("Created new TikTok geography: {}", orderId);
        }
    }

    private GeographyInfo createGeographyFromTikTok(TikTokOrderDto orderDto) {
        return GeographyInfo.builder()
                .orderId(orderDto.getOrderId())
                .geographyKey(System.currentTimeMillis() % 1000000L)
                .countryCode("VN")
                .countryName("Vietnam")
                .regionCode(orderDto.getData().getRecipientAddress().getRegionCode())
                .regionName("Vietnam Region")
                .provinceCode(null)
                .provinceName(extractProvince(orderDto))
                .provinceType("Province")
                .districtCode(null)
                .districtName(extractDistrict(orderDto))
                .districtType("District")
                .wardCode(null)
                .wardName(null)
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
                .standardDeliveryDays(2)
                .expressDeliveryAvailable(true)
                .latitude(21.0)
                .longitude(105.0)
                .build();
    }

    private void updateGeographyFromTikTok(GeographyInfo geography, TikTokOrderDto orderDto) {
        // Update if needed
    }

    // ===== PAYMENT INFO PROCESSING =====

    private void processPaymentInfoUpsert(TikTokOrderDto orderDto) {
        String orderId = orderDto.getOrderId();

        Optional<PaymentInfo> existing = paymentInfoRepository.findById(orderId);

        if (existing.isPresent()) {
            PaymentInfo payment = existing.get();
            updatePaymentFromTikTok(payment, orderDto);
            paymentInfoRepository.save(payment);
        } else {
            PaymentInfo newPayment = createPaymentFromTikTok(orderDto);
            paymentInfoRepository.save(newPayment);
        }
    }

    private PaymentInfo createPaymentFromTikTok(TikTokOrderDto orderDto) {
        return PaymentInfo.builder()
                .orderId(orderDto.getOrderId())
                .paymentKey(System.currentTimeMillis() % 1000000L)
                .paymentMethod(orderDto.getData().getIsCod() ? "COD" : "PREPAID")
                .paymentCategory(orderDto.getData().getIsCod() ? "COD" : "ONLINE")
                .paymentProvider("TikTok")
                .isCod(orderDto.getData().getIsCod())
                .isPrepaid(!orderDto.getData().getIsCod())
                .isInstallment(false)
                .installmentMonths(0)
                .supportsRefund(true)
                .supportsPartialRefund(true)
                .refundProcessingDays(7)
                .riskLevel("LOW")
                .requiresVerification(false)
                .fraudScore(0.1)
                .transactionFeeRate(0.03)
                .processingFee(0.0)
                .paymentProcessingTimeMinutes(5)
                .settlementDays(3)
                .build();
    }

    private void updatePaymentFromTikTok(PaymentInfo payment, TikTokOrderDto orderDto) {
        // Update if needed
    }

    // ===== SHIPPING INFO PROCESSING =====

    private void processShippingInfoUpsert(TikTokOrderDto orderDto) {
        String orderId = orderDto.getOrderId();

        Optional<ShippingInfo> existing = shippingInfoRepository.findById(orderId);

        if (existing.isPresent()) {
            ShippingInfo shipping = existing.get();
            updateShippingFromTikTok(shipping, orderDto);
            shippingInfoRepository.save(shipping);
        } else {
            ShippingInfo newShipping = createShippingFromTikTok(orderDto);
            shippingInfoRepository.save(newShipping);
        }
    }

    private ShippingInfo createShippingFromTikTok(TikTokOrderDto orderDto) {
        return ShippingInfo.builder()
                .orderId(orderDto.getOrderId())
                .shippingKey(System.currentTimeMillis() % 1000000L)
                .providerId("TIKTOK_SHIPPING")
                .providerName("TikTok Shipping")
                .providerType("PLATFORM")
                .providerTier("STANDARD")
                .serviceType("STANDARD")
                .serviceTier("REGULAR")
                .deliveryCommitment("2-3 days")
                .shippingMethod("GROUND")
                .pickupType("PICKUP_FROM_SELLER")
                .deliveryType("HOME_DELIVERY")
                .baseFee(parseAmount(orderDto.getData().getPayment().getShippingFee()))
                .weightBasedFee(0.0)
                .distanceBasedFee(0.0)
                .codFee(orderDto.getData().getIsCod() ? 5000.0 : 0.0)
                .insuranceFee(0.0)
                .supportsCod(true)
                .supportsInsurance(false)
                .supportsFragile(false)
                .supportsRefrigerated(false)
                .providesTracking(true)
                .providesSmsUpdates(false)
                .averageDeliveryDays(2.5)
                .onTimeDeliveryRate(0.85)
                .successDeliveryRate(0.95)
                .damageRate(0.02)
                .coverageProvinces("ALL")
                .coverageNationwide(true)
                .coverageInternational(false)
                .build();
    }

    // Vị trí: Sau method createShippingInfoEntity() trong TikTokEtlService
    private void updateShippingFromTikTok(ShippingInfo existing, TikTokOrderDto orderDto) {
        try {
            // Create new shipping info để get updated values
            ShippingInfo newShippingInfo = createShippingFromTikTok(orderDto);

            if (newShippingInfo != null) {
                // Update fields theo đúng ShippingInfo entity
                existing.setShippingKey(newShippingInfo.getShippingKey());
                existing.setProviderId(newShippingInfo.getProviderId());
                existing.setProviderName(newShippingInfo.getProviderName());
                existing.setProviderType(newShippingInfo.getProviderType());
                existing.setProviderTier(newShippingInfo.getProviderTier());
                existing.setServiceType(newShippingInfo.getServiceType());
                existing.setServiceTier(newShippingInfo.getServiceTier());
                existing.setDeliveryCommitment(newShippingInfo.getDeliveryCommitment());
                existing.setShippingMethod(newShippingInfo.getShippingMethod());
                existing.setPickupType(newShippingInfo.getPickupType());
                existing.setDeliveryType(newShippingInfo.getDeliveryType());

                // Fee fields
                existing.setBaseFee(newShippingInfo.getBaseFee());
                existing.setWeightBasedFee(newShippingInfo.getWeightBasedFee());
                existing.setDistanceBasedFee(newShippingInfo.getDistanceBasedFee());
                existing.setCodFee(newShippingInfo.getCodFee());
                existing.setInsuranceFee(newShippingInfo.getInsuranceFee());

                // Support capabilities
                existing.setSupportsCod(newShippingInfo.getSupportsCod());
                existing.setSupportsInsurance(newShippingInfo.getSupportsInsurance());
                existing.setSupportsFragile(newShippingInfo.getSupportsFragile());
                existing.setSupportsRefrigerated(newShippingInfo.getSupportsRefrigerated());
                existing.setProvidesTracking(newShippingInfo.getProvidesTracking());
                existing.setProvidesSmsUpdates(newShippingInfo.getProvidesSmsUpdates());

                // Performance metrics
                existing.setAverageDeliveryDays(newShippingInfo.getAverageDeliveryDays());
                existing.setOnTimeDeliveryRate(newShippingInfo.getOnTimeDeliveryRate());
                existing.setSuccessDeliveryRate(newShippingInfo.getSuccessDeliveryRate());
                existing.setDamageRate(newShippingInfo.getDamageRate());

                // Coverage info
                existing.setCoverageProvinces(newShippingInfo.getCoverageProvinces());
                existing.setCoverageNationwide(newShippingInfo.getCoverageNationwide());
                existing.setCoverageInternational(newShippingInfo.getCoverageInternational());

                log.debug("Updated shipping info for order: {}", orderDto.getOrderId());
            } else {
                log.warn("Failed to create new shipping info for update, order: {}", orderDto.getOrderId());
            }

        } catch (Exception e) {
            log.error("Failed to update shipping info for order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    // ===== DATE INFO PROCESSING =====

    private void processDateInfoUpsert(TikTokOrderDto orderDto) {
        String orderId = orderDto.getOrderId();

        Optional<ProcessingDateInfo> existing = processingDateInfoRepository.findById(orderId);

        if (existing.isPresent()) {
            ProcessingDateInfo dateInfo = existing.get();
            updateDateInfoFromTikTok(dateInfo, orderDto);
            processingDateInfoRepository.save(dateInfo);
        } else {
            ProcessingDateInfo newDateInfo = createDateInfoFromTikTok(orderDto);
            processingDateInfoRepository.save(newDateInfo);
        }
    }

    private ProcessingDateInfo createDateInfoFromTikTok(TikTokOrderDto orderDto) {
        LocalDateTime orderDate = convertTimestamp(orderDto.getData().getUpdateTime());

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

    // Vị trí: Sau method createProcessingDateInfoEntity() trong TikTokEtlService
    private void updateDateInfoFromTikTok(ProcessingDateInfo existing, TikTokOrderDto orderDto) {
        try {
            // Create new date info để get updated values
            ProcessingDateInfo newDateInfo = createDateInfoFromTikTok(orderDto);

            if (newDateInfo != null) {
                // Update all fields with new values
                existing.setFullDate(newDateInfo.getFullDate());
                existing.setDayOfWeek(newDateInfo.getDayOfWeek());
                existing.setDayOfWeekName(newDateInfo.getDayOfWeekName());
                existing.setDayOfMonth(newDateInfo.getDayOfMonth());
                existing.setDayOfYear(newDateInfo.getDayOfYear());
                existing.setWeekOfYear(newDateInfo.getWeekOfYear());
                existing.setMonthOfYear(newDateInfo.getMonthOfYear());
                existing.setMonthName(newDateInfo.getMonthName());
                existing.setQuarterOfYear(newDateInfo.getQuarterOfYear());
                existing.setQuarterName(newDateInfo.getQuarterName());
                existing.setYear(newDateInfo.getYear());
                existing.setIsWeekend(newDateInfo.getIsWeekend());
                existing.setIsHoliday(newDateInfo.getIsHoliday());
                existing.setHolidayName(newDateInfo.getHolidayName());
                existing.setIsBusinessDay(newDateInfo.getIsBusinessDay());
                existing.setFiscalYear(newDateInfo.getFiscalYear());
                existing.setFiscalQuarter(newDateInfo.getFiscalQuarter());
                existing.setIsShoppingSeason(newDateInfo.getIsShoppingSeason());
                existing.setSeasonName(newDateInfo.getSeasonName());
                existing.setIsPeakHour(newDateInfo.getIsPeakHour());

                log.debug("Updated processing date info for order: {}", orderDto.getOrderId());
            } else {
                log.warn("Failed to create new date info for update, order: {}", orderDto.getOrderId());
            }

        } catch (Exception e) {
            log.error("Failed to update processing date info for order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    // ===== STATUS PROCESSING =====

    private void processStatusInfoUpsert(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            String tiktokStatus = orderDto.getStatus();
            log.debug("Processing status info for order: {} with status: {}", orderId, tiktokStatus);

            // ✅ ASSUME status mapping already exists (created by StatusMappingInitializer)
            Optional<Status> existingStatus = statusRepository.findByPlatformAndPlatformStatusCode(
                    platformName, tiktokStatus);

            if (existingStatus.isPresent()) {
                // ✅ UPDATE existing status if needed
                Status existing = existingStatus.get();
                String standardStatus = mapTikTokStatusToStandard(tiktokStatus);

                existing.setStandardStatusName(standardStatus);
                existing.setStatusCategory(getStatusCategory(standardStatus));
                statusRepository.save(existing);

                log.debug("Updated TikTok status mapping for platform {} status {}", platformName, tiktokStatus);
            } else {
                // ⚠️ This should not happen if StatusMappingInitializer works correctly
                log.warn("Status mapping not found for platform {} status {} - StatusMappingInitializer may have failed",
                        platformName, tiktokStatus);
            }

        } catch (Exception e) {
            log.error("Failed to process status info for order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    private void processOrderStatusTransition(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            log.debug("Processing order status transition for order: {}", orderId);

            // Map TikTok status to get status key
            String tiktokStatus = orderDto.getStatus();
            Optional<Status> statusEntity = statusRepository.findByPlatformAndPlatformStatusCode(
                    platformName, tiktokStatus);

            if (!statusEntity.isPresent()) {
                log.warn("Status entity not found for platform {} status {}, skipping status transition",
                        platformName, tiktokStatus);
                return;
            }

            Long statusKey = statusEntity.get().getStatusKey();

            // Check if status transition already exists for this status + order combination
            Optional<OrderStatus> existingTransition = orderStatusRepository.findByStatusKeyAndOrderId(
                    statusKey, orderId);

            if (existingTransition.isPresent()) {
                // UPDATE existing status transition
                OrderStatus existing = existingTransition.get();
                OrderStatus newTransition = createOrderStatusEntity(orderDto, statusKey);

                if (newTransition != null) {
                    // Update fields - direct mapping, no calculations
                    existing.setTransitionTimestamp(newTransition.getTransitionTimestamp());
                    existing.setDurationInPreviousStatusHours(newTransition.getDurationInPreviousStatusHours());
                    existing.setTransitionReason(newTransition.getTransitionReason());
                    existing.setTransitionTrigger(newTransition.getTransitionTrigger());
                    existing.setChangedBy(newTransition.getChangedBy());
                    existing.setIsOnTimeTransition(newTransition.getIsOnTimeTransition());
                    existing.setIsExpectedTransition(newTransition.getIsExpectedTransition());

                    orderStatusRepository.save(existing);
                    log.debug("Updated status transition for order: {} to status: {}", orderId, tiktokStatus);
                } else {
                    log.warn("Failed to create new status transition entity for order: {}", orderId);
                }
            } else {
                // INSERT new transition
                OrderStatus newTransition = createOrderStatusEntity(orderDto, statusKey);
                if (newTransition != null) {
                    orderStatusRepository.save(newTransition);
                    log.debug("Created new status transition for order: {} to status: {}", orderId, tiktokStatus);
                } else {
                    log.warn("Failed to create status transition entity for order: {}", orderId);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process order status transition for order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    private OrderStatus createOrderStatusEntity(TikTokOrderDto orderDto, Long statusKey) {
        try {
            String orderId = orderDto.getOrderId();
            // Get timestamp data from API - direct mapping
            Long updateTimeUnix = orderDto.getUpdateTime(); // Direct value
            LocalDateTime transitionTime = updateTimeUnix != null ?
                    LocalDateTime.ofInstant(Instant.ofEpochSecond(updateTimeUnix), java.time.ZoneId.systemDefault()) :
                    LocalDateTime.now(); // Default: current time

            // Generate date key for transition date
            Integer transitionDateKey = generateDateKey(transitionTime.toLocalDate());

            return OrderStatus.builder()
                    .statusKey(statusKey)
                    .orderId(orderId)
                    .transitionDateKey(transitionDateKey)
                    .transitionTimestamp(convertTimestamp(orderDto.getUpdateTime()))
                    .durationInPreviousStatusHours(24) // Default 24 hours
                    .transitionReason("TikTok order status update")
                    .transitionTrigger("SYSTEM")
                    .changedBy("TIKTOK_API")
                    .isOnTimeTransition(true)
                    .isExpectedTransition(true)
                    .historyKey(System.currentTimeMillis() % 1000000L)
                    .build();

        } catch (Exception e) {
            log.error("Failed to create OrderStatus entity for order: {}", orderDto.getOrderId(), e);
            return null;
        }
    }

    private void processOrderStatusDetailUpsert(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            log.debug("Processing order status detail for order: {}", orderId);

            // Get status key for current order status
            String tiktokStatus = orderDto.getStatus();
            Optional<Status> statusEntity = statusRepository.findByPlatformAndPlatformStatusCode(
                    platformName, tiktokStatus);

            if (!statusEntity.isPresent()) {
                log.warn("Status entity not found for platform {} status {}, skipping status detail",
                        platformName, tiktokStatus);
                return;
            }

            Long statusKey = statusEntity.get().getStatusKey();

            // Check if status detail already exists for this status + order combination
            Optional<OrderStatusDetail> existingDetail = orderStatusDetailRepository.findByStatusKeyAndOrderId(
                    statusKey, orderId);

            if (existingDetail.isPresent()) {
                // UPDATE existing status detail
                OrderStatusDetail existing = existingDetail.get();
                OrderStatusDetail newDetail = createOrderStatusDetailEntity(orderDto, statusKey);

                if (newDetail != null) {
                    // Update fields - direct mapping, no calculations
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
                    log.debug("Updated status detail for order: {} status: {}", orderId, tiktokStatus);
                } else {
                    log.warn("Failed to create new status detail entity for order: {}", orderId);
                }
            } else {
                // INSERT new status detail
                OrderStatusDetail newDetail = createOrderStatusDetailEntity(orderDto, statusKey);
                if (newDetail != null) {
                    orderStatusDetailRepository.save(newDetail);
                    log.debug("Created new status detail for order: {} status: {}", orderId, tiktokStatus);
                } else {
                    log.warn("Failed to create status detail entity for order: {}", orderId);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process order status detail for order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    private OrderStatusDetail createOrderStatusDetailEntity(TikTokOrderDto orderDto, Long statusKey) {
        try {
            String tiktokStatus = orderDto.getStatus();

            return OrderStatusDetail.builder()
                    .statusKey(statusKey)
                    .orderId(orderDto.getOrderId())
                    .isActiveOrder(determineIfActiveOrder(tiktokStatus))
                    .isCompletedOrder(determineIfCompletedOrder(tiktokStatus))
                    .isRevenueRecognized(determineIfRevenueRecognized(tiktokStatus))
                    .isRefundable(determineIfRefundable(tiktokStatus))
                    .isCancellable(determineIfCancellable(tiktokStatus))
                    .isTrackable(determineIfTrackable(tiktokStatus))
                    .nextPossibleStatuses(generateNextPossibleStatuses(tiktokStatus))
                    .autoTransitionHours(determineAutoTransitionHours(tiktokStatus))
                    .requiresManualAction(determineIfRequiresManualAction(tiktokStatus))
                    .statusColor(determineStatusColor(tiktokStatus))
                    .statusIcon(determineStatusIcon(tiktokStatus))
                    .customerVisible(true)
                    .customerDescription(generateCustomerDescription(tiktokStatus))
                    .averageDurationHours(getDefaultAverageDuration(tiktokStatus))
                    .successRate(getDefaultSuccessRate(tiktokStatus))
                    .build();

        } catch (Exception e) {
            log.error("Failed to create OrderStatusDetail entity for order: {}", orderDto.getOrderId(), e);
            return null;
        }
    }

    // ===== UTILITY METHODS =====

    private Integer generateDateKey(LocalDate date) {
        if (date == null) return null;

        // Generate date key in format YYYYMMDD
        return date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth();
    }

    private boolean determineIfActiveOrder(String status) {
        return !("DELIVERED".equals(status) || "CANCELLED".equals(status));
    }

    private boolean determineIfCompletedOrder(String status) {
        return "DELIVERED".equals(status);
    }

    private boolean determineIfRevenueRecognized(String status) {
        return "DELIVERED".equals(status);
    }

    private boolean determineIfRefundable(String status) {
        return "DELIVERED".equals(status) || "CANCELLED".equals(status);
    }

    private boolean determineIfCancellable(String status) {
        return !("DELIVERED".equals(status) || "CANCELLED".equals(status) || "IN_TRANSIT".equals(status));
    }

    private boolean determineIfTrackable(String status) {
        return "IN_TRANSIT".equals(status) || "AWAITING_SHIPMENT".equals(status);
    }

    private boolean determineIfRequiresManualAction(String status) {
        return "AWAITING_PAYMENT".equals(status);
    }

    private String generateNextPossibleStatuses(String currentStatus) {
        switch (currentStatus) {
            case "AWAITING_PAYMENT":
                return "AWAITING_SHIPMENT,CANCELLED";
            case "AWAITING_SHIPMENT":
                return "IN_TRANSIT,CANCELLED";
            case "IN_TRANSIT":
                return "DELIVERED";
            case "DELIVERED":
                return ""; // Final state
            case "CANCELLED":
                return ""; // Final state
            default:
                return "";
        }
    }

    private Integer determineAutoTransitionHours(String status) {
        switch (status) {
            case "AWAITING_PAYMENT":
                return 24;
            case "AWAITING_SHIPMENT":
                return 48;
            case "IN_TRANSIT":
                return 72;
            default:
                return 0;
        }
    }

    private String determineStatusColor(String status) {
        switch (status) {
            case "DELIVERED":
                return "GREEN";
            case "CANCELLED":
                return "RED";
            case "IN_TRANSIT":
                return "BLUE";
            case "AWAITING_SHIPMENT":
                return "ORANGE";
            case "AWAITING_PAYMENT":
                return "YELLOW";
            default:
                return "GRAY";
        }
    }

    private String determineStatusIcon(String status) {
        switch (status) {
            case "DELIVERED":
                return "check-circle";
            case "CANCELLED":
                return "x-circle";
            case "IN_TRANSIT":
                return "truck";
            case "AWAITING_SHIPMENT":
                return "package";
            case "AWAITING_PAYMENT":
                return "credit-card";
            default:
                return "help-circle";
        }
    }

    private String generateCustomerDescription(String status) {
        switch (status) {
            case "AWAITING_PAYMENT":
                return "Chờ thanh toán";
            case "AWAITING_SHIPMENT":
                return "Chờ giao hàng";
            case "IN_TRANSIT":
                return "Đang vận chuyển";
            case "DELIVERED":
                return "Đã giao hàng thành công";
            case "CANCELLED":
                return "Đơn hàng đã bị hủy";
            default:
                return "Trạng thái không xác định";
        }
    }

    private Double getDefaultAverageDuration(String status) {
        switch (status) {
            case "AWAITING_PAYMENT":
                return 12.0; // 12 hours
            case "AWAITING_SHIPMENT":
                return 24.0; // 24 hours
            case "IN_TRANSIT":
                return 48.0; // 48 hours
            default:
                return 24.0; // Default 24 hours
        }
    }

    private Double getDefaultSuccessRate(String status) {
        switch (status) {
            case "DELIVERED":
                return 1.0; // 100% success
            case "CANCELLED":
                return 0.0; // 0% success
            case "IN_TRANSIT":
            case "AWAITING_SHIPMENT":
                return 0.95; // 95% success rate
            case "AWAITING_PAYMENT":
                return 0.75; // 75% success rate
            default:
                return 0.85; // Default 85% success rate
        }
    }

    private Double parseAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(amount.trim());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse amount: {}", amount);
            return 0.0;
        }
    }

    private LocalDateTime convertTimestamp(Long timestamp) {
        if (timestamp == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
    }

    private int countTikTokItems(TikTokOrderDto orderDto) {
        if (orderDto.getData() == null || orderDto.getData().getLineItems() == null) {
            return 1;
        }
        return orderDto.getData().getLineItems().size();
    }

    private String extractProvince(TikTokOrderDto orderDto) {
        try {
            List<TikTokOrderDto.TikTokDistrictInfo> districtInfoList =
                    orderDto.getData().getRecipientAddress().getDistrictInfo();

            if (districtInfoList != null && !districtInfoList.isEmpty()) {
                // Lấy element đầu tiên (thường là Province/City level)
                return districtInfoList.get(0).getAddressName();
            }
        } catch (Exception e) {
            log.warn("Failed to extract province from TikTok order", e);
        }
        return "Unknown Province";
    }

    private String extractDistrict(TikTokOrderDto orderDto) {
        try {
            List<TikTokOrderDto.TikTokDistrictInfo> districtInfoList =
                    orderDto.getData().getRecipientAddress().getDistrictInfo();

            if (districtInfoList != null && districtInfoList.size() > 1) {
                // Lấy element thứ 2 (thường là District level)
                return districtInfoList.get(1).getAddressName();
            }
        } catch (Exception e) {
            log.warn("Failed to extract district from TikTok order", e);
        }
        return "Unknown District";
    }

    private boolean hasDiscount(TikTokOrderDto orderDto) {
        Double sellerDiscount = parseAmount(orderDto.getData().getPayment().getSellerDiscount());
        Double platformDiscount = parseAmount(orderDto.getData().getPayment().getPlatformDiscount());
        return sellerDiscount > 0 || platformDiscount > 0;
    }

    private boolean hasItemDiscount(TikTokItemDto item) {
        Double originalPrice = parseAmount(item.getOriginalPrice());
        Double salePrice = parseAmount(item.getSalePrice());
        return originalPrice > salePrice;
    }

    private String mapTikTokStatusToStandard(String tiktokStatus) {
        switch (tiktokStatus) {
            case "DELIVERED": return "COMPLETED";
            case "CANCELLED": return "CANCELLED";
            case "AWAITING_SHIPMENT": return "READY_TO_SHIP";
            case "IN_TRANSIT": return "SHIPPING";
            case "AWAITING_PAYMENT": return "UNPAID";
            default: return "PROCESSING";
        }
    }

    private boolean isShoppingSeason(LocalDateTime date) {
        int month = date.getMonthValue();
        return month == 11 || month == 12 || month == 1; // Nov-Dec-Jan
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
        return (hour >= 10 && hour <= 12) || (hour >= 19 && hour <= 21);
    }

    private String getStatusCategory(String status) {
        switch (status) {
            case "DELIVERED": return "FINAL";
            case "CANCELLED": return "FINAL";
            case "AWAITING_SHIPMENT": return "PROCESSING";
            case "IN_TRANSIT": return "SHIPPING";
            case "AWAITING_PAYMENT": return "PENDING";
            default: return "PROCESSING";
        }
    }

    // ===== RESULT CLASSES =====

    public static class EtlResult {
        private boolean success;
        private String errorMessage;
        private int totalOrders;
        private int ordersProcessed;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private java.util.List<FailedOrder> failedOrders = new java.util.ArrayList<>();

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public int getOrdersProcessed() { return ordersProcessed; }
        public void setOrdersProcessed(int ordersProcessed) { this.ordersProcessed = ordersProcessed; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public java.util.List<FailedOrder> getFailedOrders() { return failedOrders; }
        public void setFailedOrders(java.util.List<FailedOrder> failedOrders) { this.failedOrders = failedOrders; }

        public void addFailedOrder(String orderId, String error) {
            failedOrders.add(new FailedOrder(orderId, error));
        }

        public long getDurationMs() {
            if (startTime != null && endTime != null) {
                return java.time.Duration.between(startTime, endTime).toMillis();
            }
            return 0;
        }
    }

    public static class FailedOrder {
        private String orderId;
        private String errorMessage;

        public FailedOrder(String orderId, String errorMessage) {
            this.orderId = orderId;
            this.errorMessage = errorMessage;
        }

        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}