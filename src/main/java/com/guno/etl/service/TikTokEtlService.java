// TikTokEtlService.java - Complete TikTok ETL Service Implementation
package com.guno.etl.service;

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
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class TikTokEtlService {

    private static final Logger log = LoggerFactory.getLogger(TikTokEtlService.class);

    @Autowired
    private TikTokApiService apiService;

    // All 11 repositories (same as Shopee)
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private GeographyInfoRepository geographyInfoRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private OrderStatusDetailRepository orderStatusDetailRepository;

    @Autowired
    private PaymentInfoRepository paymentInfoRepository;

    @Autowired
    private ShippingInfoRepository shippingInfoRepository;

    @Autowired
    private ProcessingDateInfoRepository processingDateInfoRepository;

    // TikTok platform configuration
    @Value("${etl.platforms.tiktok.name:TIKTOK}")
    private String platformName;

    // ===== MAIN ETL METHODS FOR TIKTOK =====

    /**
     * Process updated TikTok orders from API (main method for scheduler)
     */
    public EtlResult processUpdatedOrders() {
        log.info("=== Starting TikTok ETL process for updated orders ===");

        EtlResult result = new EtlResult();
        result.setStartTime(LocalDateTime.now());

        try {
            // Fetch updated orders from TikTok API
            TikTokApiResponse response = apiService.fetchUpdatedOrders();

            if (response == null || response.getStatus() != 1) {
                String error = "TikTok API call failed: " + (response != null ? response.getMessage() : "null response");
                result.setSuccess(false);
                result.setErrorMessage(error);
                return result;
            }

            List<TikTokOrderDto> orders = response.getData().getOrders();
            log.info("Received {} TikTok orders from API", orders.size());

            result = processOrdersWithErrorHandling(orders);

        } catch (Exception e) {
            log.error("Error in TikTok ETL process: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage("TikTok ETL process failed: " + e.getMessage());
        } finally {
            result.setEndTime(LocalDateTime.now());
            result.calculateDuration();
        }

        log.info("=== TikTok ETL process completed: {}/{} orders processed in {} ms ===",
                result.getOrdersProcessed(), result.getTotalOrders(), result.getDurationMs());

        return result;
    }

    /**
     * Process multiple TikTok orders with individual error handling
     */
    private EtlResult processOrdersWithErrorHandling(List<TikTokOrderDto> orders) {
        EtlResult result = new EtlResult();
        result.setTotalOrders(orders.size());

        for (TikTokOrderDto order : orders) {
            try {
                processOrderUpsert(order);
                result.incrementProcessed();
                log.debug("Successfully processed TikTok order: {}", order.getOrderId());
            } catch (Exception e) {
                log.error("Failed to process TikTok order {}: {}", order.getOrderId(), e.getMessage());
                result.addFailedOrder(new FailedOrder(order.getOrderId(), e.getMessage()));
            }
        }

        boolean success = result.getOrdersProcessed() > 0 || result.getTotalOrders() == 0;
        result.setSuccess(success);

        return result;
    }

    /**
     * Process single TikTok order with UPSERT logic - ALL 9 TABLES
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOrderUpsert(TikTokOrderDto orderDto) {
        try {
            log.debug("Starting TikTok UPSERT for order: {}", orderDto.getOrderId());

            // Process all related entities with UPSERT logic (same structure as Shopee)
            processCustomerUpsert(orderDto);
            processOrderEntityUpsert(orderDto);
            processOrderItemsUpsert(orderDto);
            processProductsUpsert(orderDto);
            processGeographyInfoUpsert(orderDto);

            // Process additional tables (same as Shopee)
            processDateInfoUpsert(orderDto);
            processPaymentInfoUpsert(orderDto);
            processShippingInfoUpsert(orderDto);
            processStatusInfoUpsert(orderDto);
            processOrderStatusTransition(orderDto);
            processOrderStatusDetailUpsert(orderDto);

            log.info("TikTok order {} UPSERT completed successfully - ALL 9 TABLES", orderDto.getOrderId());

        } catch (Exception e) {
            log.error("Failed to process TikTok order upsert for {}: {}", orderDto.getOrderId(), e.getMessage());
            throw e; // Re-throw to trigger transaction rollback
        }
    }

    // ===== TIKTOK-SPECIFIC UPSERT METHODS (same pattern as Shopee) =====

    private void processCustomerUpsert(TikTokOrderDto orderDto) {
        try {
            String phoneHash = HashUtil.hashPhone(extractPhoneFromTikTok(orderDto));

            Optional<Customer> existingCustomer = customerRepository.findByPhoneHash(phoneHash);

            if (existingCustomer.isPresent()) {
                // UPDATE existing customer metrics
                Customer customer = existingCustomer.get();
                updateCustomerMetrics(customer, orderDto);
                customerRepository.save(customer);
                log.debug("Updated TikTok customer: {}", customer.getCustomerId());
            } else {
                // INSERT new customer
                Customer newCustomer = createCustomerFromTikTokOrder(orderDto, phoneHash);
                customerRepository.save(newCustomer);
                log.debug("Created new TikTok customer: {}", newCustomer.getCustomerId());
            }
        } catch (Exception e) {
            log.error("Failed to process TikTok customer upsert: {}", e.getMessage());
            throw e;
        }
    }

    private void processOrderEntityUpsert(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            Optional<Order> existingOrder = orderRepository.findById(orderId);

            if (existingOrder.isPresent()) {
                // UPDATE existing order
                Order order = existingOrder.get();
                updateOrderFromTikTok(order, orderDto);
                orderRepository.save(order);
                log.debug("Updated TikTok order: {}", orderId);
            } else {
                // INSERT new order
                Order newOrder = createOrderFromTikTok(orderDto);
                orderRepository.save(newOrder);
                log.debug("Created new TikTok order: {}", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to process TikTok order entity upsert: {}", e.getMessage());
            throw e;
        }
    }

    private void processOrderItemsUpsert(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();

            // DELETE existing items for this order (same as Shopee)
            List<OrderItem> existingItems = orderItemRepository.findByOrderIdOrderByItemSequence(orderId);
            orderItemRepository.deleteAll(existingItems);
            log.debug("Deleted existing TikTok order items for order: {}", orderId);

            // INSERT new items
            List<TikTokItemDto> items = orderDto.getData().getLineItems();
            if (items != null && !items.isEmpty()) {
                for (int i = 0; i < items.size(); i++) {
                    TikTokItemDto item = items.get(i);
                    OrderItem orderItem = createOrderItemFromTikTok(orderId, item, i + 1);
                    orderItemRepository.save(orderItem);
                }
                log.debug("Created {} TikTok order items for order: {}", items.size(), orderId);
            }
        } catch (Exception e) {
            log.error("Failed to process TikTok order items upsert: {}", e.getMessage());
            throw e;
        }
    }

    private void processProductsUpsert(TikTokOrderDto orderDto) {
        try {
            List<TikTokItemDto> items = orderDto.getData().getLineItems();
            if (items == null || items.isEmpty()) return;

            for (TikTokItemDto item : items) {
                String sku = item.getSkuId() != null ? item.getSkuId() : "SKU_" + item.getProductId();

                // FIX: Handle List<Product> instead of Optional<Product>
                List<Product> existingProducts = productRepository.findBySku(sku);

                if (!existingProducts.isEmpty()) {
                    // UPDATE existing product (take first one if multiple)
                    Product product = existingProducts.get(0);
                    updateProductFromTikTok(product, item);
                    productRepository.save(product);
                    log.debug("Updated TikTok product: {}", sku);
                } else {
                    // INSERT new product
                    Product newProduct = createProductFromTikTok(item, sku);
                    productRepository.save(newProduct);
                    log.debug("Created new TikTok product: {}", sku);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process TikTok products upsert: {}", e.getMessage());
            throw e;
        }
    }

    private void processGeographyInfoUpsert(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            Optional<GeographyInfo> existingGeography = geographyInfoRepository.findById(orderId);

            if (existingGeography.isPresent()) {
                // UPDATE existing geography
                GeographyInfo geography = existingGeography.get();
                updateGeographyFromTikTok(geography, orderDto);
                geographyInfoRepository.save(geography);
                log.debug("Updated TikTok geography for order: {}", orderId);
            } else {
                // INSERT new geography
                GeographyInfo newGeography = createGeographyFromTikTok(orderDto);
                geographyInfoRepository.save(newGeography);
                log.debug("Created new TikTok geography for order: {}", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to process TikTok geography upsert: {}", e.getMessage());
            throw e;
        }
    }

    // ===== ADDITIONAL TABLE PROCESSING (same pattern as Shopee) =====

    private void processDateInfoUpsert(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            log.debug("Processing TikTok date info for order: {}", orderId);

            Optional<ProcessingDateInfo> existingDateInfo = processingDateInfoRepository.findById(orderId);

            if (existingDateInfo.isPresent()) {
                ProcessingDateInfo existing = existingDateInfo.get();
                ProcessingDateInfo newDateInfo = createProcessingDateInfoEntityFromTikTok(orderDto);

                if (newDateInfo != null) {
                    updateDateInfoFields(existing, newDateInfo);
                    processingDateInfoRepository.save(existing);
                    log.debug("Updated TikTok date info for order: {}", orderId);
                }
            } else {
                ProcessingDateInfo newDateInfo = createProcessingDateInfoEntityFromTikTok(orderDto);
                if (newDateInfo != null) {
                    processingDateInfoRepository.save(newDateInfo);
                    log.debug("Created new TikTok date info for order: {}", orderId);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process TikTok date info for order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    private void processPaymentInfoUpsert(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            log.debug("Processing TikTok payment info for order: {}", orderId);

            Optional<PaymentInfo> existingPaymentInfo = paymentInfoRepository.findById(orderId);

            if (existingPaymentInfo.isPresent()) {
                PaymentInfo existing = existingPaymentInfo.get();
                PaymentInfo newPaymentInfo = createPaymentInfoEntityFromTikTok(orderDto);

                if (newPaymentInfo != null) {
                    updatePaymentInfoFields(existing, newPaymentInfo);
                    paymentInfoRepository.save(existing);
                    log.debug("Updated TikTok payment info for order: {}", orderId);
                }
            } else {
                PaymentInfo newPaymentInfo = createPaymentInfoEntityFromTikTok(orderDto);
                if (newPaymentInfo != null) {
                    paymentInfoRepository.save(newPaymentInfo);
                    log.debug("Created new TikTok payment info for order: {}", orderId);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process TikTok payment info for order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    private void processShippingInfoUpsert(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            log.debug("Processing TikTok shipping info for order: {}", orderId);

            Optional<ShippingInfo> existingShippingInfo = shippingInfoRepository.findById(orderId);

            if (existingShippingInfo.isPresent()) {
                ShippingInfo existing = existingShippingInfo.get();
                ShippingInfo newShippingInfo = createShippingInfoEntityFromTikTok(orderDto);

                if (newShippingInfo != null) {
                    updateShippingInfoFields(existing, newShippingInfo);
                    shippingInfoRepository.save(existing);
                    log.debug("Updated TikTok shipping info for order: {}", orderId);
                }
            } else {
                ShippingInfo newShippingInfo = createShippingInfoEntityFromTikTok(orderDto);
                if (newShippingInfo != null) {
                    shippingInfoRepository.save(newShippingInfo);
                    log.debug("Created new TikTok shipping info for order: {}", orderId);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process TikTok shipping info for order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    private void processStatusInfoUpsert(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            String tiktokStatus = orderDto.getStatus();
            log.debug("Processing TikTok status info for order: {} with status: {}", orderId, tiktokStatus);

            Optional<Status> existingStatus = statusRepository.findByPlatformAndPlatformStatusCode(
                    platformName, tiktokStatus);

            if (existingStatus.isPresent()) {
                // UPDATE existing status
                Status existing = existingStatus.get();
                existing.setStandardStatusName(mapTikTokStatusToStandard(tiktokStatus));
                existing.setStatusCategory(determineStatusCategoryFromTikTok(tiktokStatus));
                statusRepository.save(existing);
                log.debug("Updated TikTok status mapping for platform {} status {}", platformName, tiktokStatus);
            } else {
                // CREATE new status mapping automatically
                Status newStatus = createStatusEntityFromTikTok(orderDto);
                if (newStatus != null) {
                    statusRepository.save(newStatus);
                    log.info("✅ Created TikTok status mapping: {} -> {}", tiktokStatus, newStatus.getStandardStatusName());
                }
            }

        } catch (Exception e) {
            log.error("Failed to process TikTok status info for order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    private void processOrderStatusTransition(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            log.debug("Processing TikTok order status transition for order: {}", orderId);

            String tiktokStatus = orderDto.getStatus();
            Optional<Status> statusEntity = statusRepository.findByPlatformAndPlatformStatusCode(
                    platformName, tiktokStatus);

            if (!statusEntity.isPresent()) {
                log.warn("TikTok status entity not found for platform {} status {}, skipping transition",
                        platformName, tiktokStatus);
                return;
            }

            Long statusKey = statusEntity.get().getStatusKey();
            Optional<OrderStatus> existingTransition = orderStatusRepository.findByStatusKeyAndOrderId(
                    statusKey, orderId);

            if (existingTransition.isPresent()) {
                OrderStatus existing = existingTransition.get();
                OrderStatus newTransition = createOrderStatusEntityFromTikTok(orderDto, statusKey);

                if (newTransition != null) {
                    updateOrderStatusFields(existing, newTransition);
                    orderStatusRepository.save(existing);
                    log.debug("Updated TikTok status transition for order: {} to status: {}", orderId, tiktokStatus);
                }
            } else {
                OrderStatus newTransition = createOrderStatusEntityFromTikTok(orderDto, statusKey);
                if (newTransition != null) {
                    orderStatusRepository.save(newTransition);
                    log.debug("Created new TikTok status transition for order: {} to status: {}", orderId, tiktokStatus);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process TikTok order status transition for order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    private void processOrderStatusDetailUpsert(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            log.debug("Processing TikTok order status detail for order: {}", orderId);

            String tiktokStatus = orderDto.getStatus();
            Optional<Status> statusEntity = statusRepository.findByPlatformAndPlatformStatusCode(
                    platformName, tiktokStatus);

            if (!statusEntity.isPresent()) {
                log.warn("TikTok status entity not found for platform {} status {}, skipping status detail",
                        platformName, tiktokStatus);
                return;
            }

            Long statusKey = statusEntity.get().getStatusKey();
            Optional<OrderStatusDetail> existingDetail = orderStatusDetailRepository.findByStatusKeyAndOrderId(
                    statusKey, orderId);

            if (existingDetail.isPresent()) {
                OrderStatusDetail existing = existingDetail.get();
                OrderStatusDetail newDetail = createOrderStatusDetailEntityFromTikTok(orderDto, statusKey);

                if (newDetail != null) {
                    updateOrderStatusDetailFields(existing, newDetail);
                    orderStatusDetailRepository.save(existing);
                    log.debug("Updated TikTok status detail for order: {} status: {}", orderId, tiktokStatus);
                }
            } else {
                OrderStatusDetail newDetail = createOrderStatusDetailEntityFromTikTok(orderDto, statusKey);
                if (newDetail != null) {
                    orderStatusDetailRepository.save(newDetail);
                    log.debug("Created new TikTok status detail for order: {} status: {}", orderId, tiktokStatus);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process TikTok order status detail for order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    // ===== TIKTOK-SPECIFIC ENTITY CREATION METHODS =====

    private Customer createCustomerFromTikTokOrder(TikTokOrderDto orderDto, String phoneHash) {
        String email = orderDto.getData().getBuyerEmail();
        String emailHash = email != null ? HashUtil.hashEmail(email) : null;
        String customerId = HashUtil.generateCustomerId(platformName, phoneHash);

        return Customer.builder()
                .customerId(customerId)
                .customerKey(customerRepository.findNextCustomerKey()) // or generate
                .platformCustomerId(orderDto.getData().getUserId())
                .phoneHash(phoneHash)
                .emailHash(emailHash)
                .gender(null) // TikTok doesn't provide
                .ageGroup(null)
                .customerSegment("REGULAR")
                .customerTier("STANDARD")
                .acquisitionChannel("TIKTOK")
                .firstOrderDate(convertTimestamp(orderDto.getCreateTime()))
                .lastOrderDate(convertTimestamp(orderDto.getCreateTime()))
                .totalOrders(1)
                .totalSpent(parseAmount(orderDto.getData().getPayment().getTotalAmount()))
                .averageOrderValue(parseAmount(orderDto.getData().getPayment().getTotalAmount()))
                .totalItemsPurchased(1)
                .daysSinceFirstOrder(0)
                .daysSinceLastOrder(0)
                .purchaseFrequencyDays(0.0)
                .returnRate(0.0)
                .cancellationRate(0.0)
                .codPreferenceRate(orderDto.getData().getIsCod() ? 1.0 : 0.0)
                .favoriteCategory(null)
                .favoriteBrand(null)
                .preferredPaymentMethod(detectTikTokPaymentMethod(orderDto))
                .preferredPlatform("TIKTOK")
                .primaryShippingProvince(extractProvinceFromTikTok(orderDto))
                .shipsToMultipleProvinces(false)
                .loyaltyPoints(0)
                .referralCount(0)
                .isReferrer(false)
                .build();
    }

    // ===== ORDER ENTITY - FIXED =====
    private Order createOrderFromTikTok(TikTokOrderDto orderDto) {
        String phoneHash = HashUtil.hashPhone(extractPhoneFromTikTok(orderDto));
        String customerId = HashUtil.generateCustomerId(platformName, phoneHash);

        return Order.builder()
                .orderId(orderDto.getOrderId())
                .customerId(customerId)
                .shopId(null) // TikTok doesn't provide shop concept
                .orderCount(1)
                .itemQuantity(1) // TikTok line items typically 1
                .totalItemsInOrder(orderDto.getData().getLineItems() != null ?
                        orderDto.getData().getLineItems().size() : 1)
                .grossRevenue(parseAmount(orderDto.getData().getPayment().getTotalAmount()))
                .netRevenue(parseAmount(orderDto.getData().getPayment().getTotalAmount()))
                .shippingFee(parseAmount(orderDto.getData().getPayment().getShippingFee()))
                .taxAmount(0.0) // TikTok doesn't provide tax info
                .discountAmount(0.0) // TikTok doesn't provide discount
                .codAmount(orderDto.getData().getIsCod() ?
                        parseAmount(orderDto.getData().getPayment().getTotalAmount()) : 0.0)
                .platformFee(0.0) // TikTok doesn't provide platform fee
                .sellerDiscount(0.0)
                .platformDiscount(0.0)
                .originalPrice(parseAmount(orderDto.getData().getPayment().getTotalAmount()))
                .estimatedShippingFee(parseAmount(orderDto.getData().getPayment().getShippingFee()))
                .actualShippingFee(parseAmount(orderDto.getData().getPayment().getShippingFee()))
                .shippingWeightGram(500) // Default weight
                .daysToShip(2) // Default 2 days
                .isDelivered(orderDto.getStatus().equalsIgnoreCase("DELIVERED"))
                .isCancelled(orderDto.getStatus().equalsIgnoreCase("CANCELLED"))
                .isReturned(false) // TikTok doesn't provide return info
                .isCod(orderDto.getData().getIsCod())
                .isNewCustomer(true) // Assume new for TikTok
                .isRepeatCustomer(false)
                .isBulkOrder(false)
                .isPromotionalOrder(false)
                .isSameDayDelivery(false)
                .orderToShipHours(48) // Default 48 hours
                .shipToDeliveryHours(48) // Default 48 hours
                .totalFulfillmentHours(96) // Default 96 hours
                .customerOrderSequence(1)
                .customerLifetimeOrders(1)
                .customerLifetimeValue(parseAmount(orderDto.getData().getPayment().getTotalAmount()))
                .daysSinceLastOrder(0)
                .promotionImpact(0.0)
                .adRevenue(0.0)
                .organicRevenue(parseAmount(orderDto.getData().getPayment().getTotalAmount()))
                .aov(parseAmount(orderDto.getData().getPayment().getTotalAmount()))
                .shippingCostRatio(calculateShippingRatio(orderDto))
                .createdAt(convertTimestamp(orderDto.getCreateTime()))
                .build();
    }

    private OrderItem createOrderItemFromTikTok(String orderId, TikTokItemDto itemDto, int sequence) {
        String sku = itemDto.getSkuId() != null ? itemDto.getSkuId() : "SKU_" + itemDto.getProductId();

        return OrderItem.builder()
                .orderId(orderId)
                .sku(sku)
                .platformProductId(platformName)
                .quantity(1) // TikTok line items typically have quantity 1
                .unitPrice(parseAmount(itemDto.getSalePrice()))
                .totalPrice(parseAmount(itemDto.getSalePrice()))
                .itemDiscount(calculateItemDiscount(itemDto))
                .promotionType(null) // TikTok doesn't provide promotion details
                .promotionCode(null)
                .itemStatus("ACTIVE")
                .itemSequence(sequence)
                .opId(System.currentTimeMillis() + sequence)
                .build();
    }

    // ===== PRODUCT ENTITY - FIXED =====
    private Product createProductFromTikTok(TikTokItemDto itemDto, String sku) {
        return Product.builder()
                .sku(sku)
                .platformProductId(platformName)
                .productId(String.valueOf(itemDto.getProductId()))
                .variationId(itemDto.getSkuId())
                .barcode(null) // TikTok doesn't provide barcode
                .productName(itemDto.getProductName())
                .productDescription(null) // TikTok doesn't provide description
                .brand(null) // TikTok doesn't provide brand
                .model(null)
                .categoryLevel1(null) // TikTok doesn't provide category
                .categoryLevel2(null)
                .categoryLevel3(null)
                .categoryPath(null)
                .color(null) // TikTok doesn't provide color
                .size(null)
                .material(null)
                .weightGram(500) // Default weight
                .dimensions(null)
                .costPrice(0.0) // TikTok doesn't provide cost
                .retailPrice(parseAmount(itemDto.getSalePrice()))
                .originalPrice(parseAmount(itemDto.getOriginalPrice()))
                .priceRange(calculatePriceRange(parseAmount(itemDto.getSalePrice())))
                .isActive(true)
                .isFeatured(false)
                .isSeasonal(false)
                .isNewArrival(false)
                .isBestSeller(false)
                .primaryImageUrl(itemDto.getSkuImage())
                .imageCount(itemDto.getSkuImage() != null ? 1 : 0)
                .seoTitle(null)
                .seoKeywords(null)
                .build();
    }

    // ===== GEOGRAPHY INFO ENTITY - FIXED =====
    private GeographyInfo createGeographyFromTikTok(TikTokOrderDto orderDto) {
        var address = orderDto.getData().getRecipientAddress();

        return GeographyInfo.builder()
                .orderId(orderDto.getOrderId())
                .geographyKey(System.currentTimeMillis()) // Generate key
                .countryCode("VN") // Default Vietnam
                .countryName("Vietnam")
                .regionCode(null) // TikTok doesn't provide region
                .regionName(extractRegionFromTikTok(address))
                .provinceCode(null)
                .provinceName(extractProvinceFromTikTok(address))
                .provinceType("PROVINCE")
                .districtCode(null)
                .districtName(extractDistrictFromTikTok(address))
                .districtType("DISTRICT")
                .wardCode(null)
                .wardName(extractWardFromTikTok(address))
                .wardType("WARD")
                .isUrban(true) // Default urban
                .isMetropolitan(false)
                .isCoastal(false)
                .isBorder(false)
                .economicTier("TIER_2") // Default tier 2
                .populationDensity("MEDIUM")
                .incomeLevel("MIDDLE")
                .shippingZone("DOMESTIC")
                .deliveryComplexity("STANDARD")
                .standardDeliveryDays(2)
                .expressDeliveryAvailable(true)
                .latitude(0.0) // Default coordinates
                .longitude(0.0)
                .build();
    }

    // ===== ADDITIONAL TABLE ENTITY CREATION =====

    private ProcessingDateInfo createProcessingDateInfoEntityFromTikTok(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();

            Long createTimeStamp = orderDto.getCreateTime();
            LocalDateTime createTime = createTimeStamp != null ?
                    LocalDateTime.ofInstant(Instant.ofEpochSecond(createTimeStamp), ZoneId.systemDefault()) :
                    LocalDateTime.now();

            LocalDate orderDate = createTime.toLocalDate();

            return ProcessingDateInfo.builder()
                    .orderId(orderId)
                    .fullDate(orderDate)
                    .year(orderDate.getYear())
                    .month(orderDate.getMonthValue())
                    .day(orderDate.getDayOfMonth())
                    .quarter((orderDate.getMonthValue() - 1) / 3 + 1)
                    .dayOfWeek(orderDate.getDayOfWeek().getValue())
                    .dayOfYear(orderDate.getDayOfYear())
                    .weekOfYear(orderDate.get(java.time.temporal.WeekFields.ISO.weekOfYear()))
                    .isWeekend(orderDate.getDayOfWeek().getValue() >= 6)
                    .isHoliday(false)
                    .holidayName(null)
                    .seasonName(determineSeason(orderDate.getMonthValue()))
                    .businessQuarter("Q" + ((orderDate.getMonthValue() - 1) / 3 + 1))
                    .fiscalYear(orderDate.getYear())
                    .hour(createTime.getHour())
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to create ProcessingDateInfo for TikTok order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
            return null;
        }
    }

    private PaymentInfo createPaymentInfoEntityFromTikTok(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();

            Boolean isCod = orderDto.getData().getIsCod();
            String paymentMethod = detectTikTokPaymentMethod(orderDto);
            Double totalAmount = parseAmount(orderDto.getData().getPayment().getTotalAmount());

            return PaymentInfo.builder()
                    .orderId(orderId)
                    .paymentMethod(paymentMethod)
                    .paymentCategory(categorizeTikTokPaymentMethod(paymentMethod))
                    .isCod(isCod != null ? isCod : false)
                    .currency(orderDto.getData().getPayment().getCurrency() != null ?
                            orderDto.getData().getPayment().getCurrency() : "VND")
                    .totalAmount(totalAmount)
                    .transactionFee(0.0)
                    .paymentStatus("COMPLETED")
                    .paymentGateway(detectTikTokPaymentGateway(paymentMethod))
                    .riskScore(0.0)
                    .isVerified(true)
                    .processingTime(0)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to create PaymentInfo for TikTok order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
            return null;
        }
    }

    private ShippingInfo createShippingInfoEntityFromTikTok(TikTokOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();

            String shippingProvider = orderDto.getData().getShippingProvider() != null ?
                    orderDto.getData().getShippingProvider() : "TikTok Default";

            return ShippingInfo.builder()
                    .orderId(orderId)
                    .shippingProvider(shippingProvider)
                    .shippingProviderId(generateTikTokProviderInfo(shippingProvider))
                    .trackingNumber(orderDto.getData().getTrackingNumber())
                    .shippingMethod("STANDARD")
                    .shippingZone(detectTikTokShippingZone(orderDto))
                    .weightGram(500) // Default: 500g
                    .dimensionCm("20x15x10") // Default dimensions
                    .shippingCost(0.0) // Default: free shipping
                    .deliveryDays(2.0) // Estimate: 2 days
                    .averageDeliveryDays(2.0)
                    .onTimeDeliveryRate(0.85) // Estimate: 85% on-time rate
                    .shippingCategory("STANDARD")
                    .isExpressDelivery(false)
                    .isInternational(false)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to create ShippingInfo for TikTok order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
            return null;
        }
    }

    private Status createStatusEntityFromTikTok(TikTokOrderDto orderDto) {
        try {
            String tiktokStatus = orderDto.getStatus();
            String standardStatus = mapTikTokStatusToStandard(tiktokStatus);
            String statusCategory = determineStatusCategoryFromTikTok(tiktokStatus);

            return Status.builder()
                    .platform(platformName)
                    .platformStatusCode(tiktokStatus)
                    .standardStatusName(standardStatus)
                    .statusCategory(statusCategory)
                    .isActive(true)
                    .sortOrder(getDefaultSortOrder(tiktokStatus))
                    .description("Auto-created TikTok status mapping for " + tiktokStatus)
                    .statusColor(getStatusColor(standardStatus))
                    .allowedTransitions(getAllowedTransitions(standardStatus))
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to create Status entity for TikTok order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
            return null;
        }
    }

    private OrderStatus createOrderStatusEntityFromTikTok(TikTokOrderDto orderDto, Long statusKey) {
        try {
            String orderId = orderDto.getOrderId();
            Long updateTime = orderDto.getUpdateTime();

            LocalDateTime statusTime = updateTime != null ?
                    LocalDateTime.ofInstant(Instant.ofEpochSecond(updateTime), ZoneId.systemDefault()) :
                    LocalDateTime.now();

            return OrderStatus.builder()
                    .statusKey(statusKey)
                    .orderId(orderId)
                    .statusTimestamp(statusTime)
                    .previousStatus(null)
                    .statusDuration(0)
                    .isCurrentStatus(true)
                    .statusSource("TIKTOK_API")
                    .statusReason("Order status from TikTok platform")
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to create OrderStatus for TikTok order {} with statusKey {}: {}",
                    orderDto.getOrderId(), statusKey, e.getMessage());
            return null;
        }
    }

    private OrderStatusDetail createOrderStatusDetailEntityFromTikTok(TikTokOrderDto orderDto, Long statusKey) {
        try {
            String orderId = orderDto.getOrderId();
            String tiktokStatus = orderDto.getStatus();
            String standardStatus = mapTikTokStatusToStandard(tiktokStatus);

            return OrderStatusDetail.builder()
                    .statusKey(statusKey)
                    .orderId(orderId)
                    .statusDescription("TikTok order status: " + tiktokStatus)
                    .statusRules(generateTikTokStatusRules(standardStatus))
                    .canCancel(isStatusCancellable(standardStatus))
                    .canReturn(isStatusReturnable(standardStatus))
                    .canModify(isStatusModifiable(standardStatus))
                    .estimatedDelivery(estimateDeliveryFromStatus(orderDto, standardStatus))
                    .customerNotified(true)
                    .internalNotes("Status updated via TikTok ETL")
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to create OrderStatusDetail for TikTok order {} with statusKey {}: {}",
                    orderDto.getOrderId(), statusKey, e.getMessage());
            return null;
        }
    }

    // ===== HELPER METHODS FOR TIKTOK DATA EXTRACTION =====

    private double calculateShippingRatio(TikTokOrderDto orderDto) {
        double totalAmount = parseAmount(orderDto.getData().getPayment().getTotalAmount());
        double shippingFee = parseAmount(orderDto.getData().getPayment().getShippingFee());
        return totalAmount > 0 ? shippingFee / totalAmount : 0.0;
    }

    private double calculateItemDiscount(TikTokItemDto itemDto) {
        double originalPrice = parseAmount(itemDto.getOriginalPrice());
        double salePrice = parseAmount(itemDto.getSalePrice());
        return originalPrice - salePrice;
    }

    private String extractPhoneFromTikTok(TikTokOrderDto orderDto) {
        return orderDto.getData().getRecipientAddress().getPhoneNumber();
    }

    private String extractProvinceFromTikTok(Object address) {
        // TikTok has district_info array, extract province from it
        return "Default Province"; // Implement based on TikTok address structure
    }

    private String extractDistrictFromTikTok(Object address) {
        return "Default District"; // Implement based on TikTok address structure
    }

    private String extractWardFromTikTok(Object address) {
        return "Default Ward"; // Implement based on TikTok address structure
    }

    private String extractRegionFromTikTok(Object address) {
        return "Default Region"; // Implement based on TikTok address structure
    }

    // ===== UPDATE METHODS - FIXED =====

    private void updateCustomerMetrics(Customer customer, TikTokOrderDto orderDto) {
        customer.setLastOrderDate(convertTimestamp(orderDto.getCreateTime()));
        customer.setTotalOrders(customer.getTotalOrders() + 1);
        customer.setTotalSpent(customer.getTotalSpent() + parseAmount(orderDto.getData().getPayment().getTotalAmount()));
        customer.setAverageOrderValue(customer.getTotalSpent() / customer.getTotalOrders());
        customer.setTotalItemsPurchased(customer.getTotalItemsPurchased() + 1);
        customer.setDaysSinceLastOrder(0);
        // Update COD preference rate
        double codOrders = orderDto.getData().getIsCod() ? 1 : 0;
        customer.setCodPreferenceRate((customer.getCodPreferenceRate() * (customer.getTotalOrders() - 1) + codOrders) / customer.getTotalOrders());
    }

    private void updateOrderFromTikTok(Order order, TikTokOrderDto orderDto) {
        order.setGrossRevenue(parseAmount(orderDto.getData().getPayment().getTotalAmount()));
        order.setNetRevenue(parseAmount(orderDto.getData().getPayment().getTotalAmount()));
        order.setShippingFee(parseAmount(orderDto.getData().getPayment().getShippingFee()));
        order.setActualShippingFee(parseAmount(orderDto.getData().getPayment().getShippingFee()));
        order.setIsDelivered(orderDto.getStatus().equalsIgnoreCase("DELIVERED"));
        order.setIsCancelled(orderDto.getStatus().equalsIgnoreCase("CANCELLED"));
        order.setIsCod(orderDto.getData().getIsCod());
        order.setCodAmount(orderDto.getData().getIsCod() ?
                parseAmount(orderDto.getData().getPayment().getTotalAmount()) : 0.0);
        order.setShippingCostRatio(calculateShippingRatio(orderDto));
    }

    private void updateProductFromTikTok(Product product, TikTokItemDto item) {
        product.setProductName(item.getProductName());
        product.setRetailPrice(parseAmount(item.getSalePrice()));
        product.setOriginalPrice(parseAmount(item.getOriginalPrice()));
        product.setPriceRange(calculatePriceRange(parseAmount(item.getSalePrice())));
        product.setPrimaryImageUrl(item.getSkuImage());
        product.setImageCount(item.getSkuImage() != null ? 1 : 0);
    }

    private void updateGeographyFromTikTok(GeographyInfo geography, TikTokOrderDto orderDto) {
        var address = orderDto.getData().getRecipientAddress();
        geography.setProvinceName(extractProvinceFromTikTok(address));
        geography.setDistrictName(extractDistrictFromTikTok(address));
        geography.setWardName(extractWardFromTikTok(address));
        geography.setRegionName(extractRegionFromTikTok(address));
    }

    // ===== UTILITY METHODS FOR TIKTOK PROCESSING =====

    private String determineSeason(int month) {
        if (month >= 3 && month <= 5) return "SPRING";
        if (month >= 6 && month <= 8) return "SUMMER";
        if (month >= 9 && month <= 11) return "AUTUMN";
        return "WINTER";
    }

    private String detectTikTokPaymentMethod(TikTokOrderDto orderDto) {
        Boolean isCod = orderDto.getData().getIsCod();
        if (isCod != null && isCod) {
            return "COD";
        }
        return "ONLINE"; // Default for TikTok
    }

    private String categorizeTikTokPaymentMethod(String paymentMethod) {
        switch (paymentMethod.toUpperCase()) {
            case "COD": return "CASH_ON_DELIVERY";
            case "ONLINE": return "DIGITAL_PAYMENT";
            default: return "OTHER";
        }
    }

    private String detectTikTokPaymentGateway(String paymentMethod) {
        if ("COD".equals(paymentMethod)) return "CASH";
        return "TIKTOK_PAY"; // Default TikTok payment gateway
    }

    private Double parseAmount(String amountStr) {
        try {
            return amountStr != null ? Double.parseDouble(amountStr) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String generateTikTokProviderInfo(String provider) {
        return "TIKTOK_" + provider.toUpperCase().replaceAll("\\s+", "_");
    }

    private String detectTikTokShippingZone(TikTokOrderDto orderDto) {
        return "DOMESTIC"; // Default for TikTok
    }

    private String mapTikTokStatusToStandard(String tiktokStatus) {
        if (tiktokStatus == null) return "OTHER";

        switch (tiktokStatus.toUpperCase()) {
            case "DELIVERED":
            case "COMPLETED": return "COMPLETED";
            case "CANCELLED":
            case "CANCELED": return "CANCELLED";
            case "AWAITING_SHIPMENT":
            case "READY_TO_SHIP": return "READY_TO_SHIP";
            case "IN_TRANSIT":
            case "SHIPPING": return "IN_TRANSIT";
            case "PENDING":
            case "AWAITING_PAYMENT": return "PENDING_PAYMENT";
            case "PROCESSING": return "PROCESSING";
            default:
                log.warn("Unknown TikTok status: {}, mapping to OTHER", tiktokStatus);
                return "OTHER";
        }
    }

    private String determineStatusCategoryFromTikTok(String tiktokStatus) {
        String standardStatus = mapTikTokStatusToStandard(tiktokStatus);
        switch (standardStatus) {
            case "COMPLETED":
            case "CANCELLED": return "FINAL";
            case "IN_TRANSIT":
            case "READY_TO_SHIP": return "IN_PROGRESS";
            case "PENDING_PAYMENT":
            case "PROCESSING": return "INITIAL";
            default: return "OTHER";
        }
    }

    private int getDefaultSortOrder(String tiktokStatus) {
        String standardStatus = mapTikTokStatusToStandard(tiktokStatus);
        switch (standardStatus) {
            case "PENDING_PAYMENT": return 1;
            case "PROCESSING": return 2;
            case "READY_TO_SHIP": return 3;
            case "IN_TRANSIT": return 4;
            case "COMPLETED": return 5;
            case "CANCELLED": return 6;
            default: return 99;
        }
    }

    private String getStatusColor(String standardStatus) {
        switch (standardStatus) {
            case "COMPLETED": return "#28a745"; // Green
            case "CANCELLED": return "#dc3545"; // Red
            case "IN_TRANSIT": return "#007bff"; // Blue
            case "READY_TO_SHIP": return "#ffc107"; // Yellow
            case "PENDING_PAYMENT": return "#6c757d"; // Gray
            default: return "#17a2b8"; // Teal
        }
    }

    private String getAllowedTransitions(String standardStatus) {
        switch (standardStatus) {
            case "PENDING_PAYMENT": return "PROCESSING,CANCELLED";
            case "PROCESSING": return "READY_TO_SHIP,CANCELLED";
            case "READY_TO_SHIP": return "IN_TRANSIT,CANCELLED";
            case "IN_TRANSIT": return "COMPLETED,CANCELLED";
            case "COMPLETED": return ""; // No transitions from completed
            case "CANCELLED": return ""; // No transitions from cancelled
            default: return "";
        }
    }

    private String generateTikTokStatusRules(String standardStatus) {
        switch (standardStatus) {
            case "COMPLETED": return "Order delivered successfully";
            case "CANCELLED": return "Order has been cancelled";
            case "IN_TRANSIT": return "Order is being delivered";
            case "READY_TO_SHIP": return "Order is ready for shipment";
            case "PENDING_PAYMENT": return "Waiting for payment confirmation";
            default: return "Standard order processing rules apply";
        }
    }

    private boolean isStatusCancellable(String standardStatus) {
        return !standardStatus.equals("COMPLETED") && !standardStatus.equals("CANCELLED");
    }

    private boolean isStatusReturnable(String standardStatus) {
        return standardStatus.equals("COMPLETED");
    }

    private boolean isStatusModifiable(String standardStatus) {
        return standardStatus.equals("PENDING_PAYMENT") || standardStatus.equals("PROCESSING");
    }

    private LocalDateTime estimateDeliveryFromStatus(TikTokOrderDto orderDto, String standardStatus) {
        LocalDateTime now = LocalDateTime.now();
        switch (standardStatus) {
            case "PENDING_PAYMENT":
            case "PROCESSING": return now.plusDays(3);
            case "READY_TO_SHIP": return now.plusDays(2);
            case "IN_TRANSIT": return now.plusDays(1);
            case "COMPLETED": return now; // Already delivered
            default: return now.plusDays(3); // Default estimate
        }
    }

    private String calculatePriceRange(Double price) {
        if (price == null || price <= 0) return "UNDER_100K";
        if (price < 100000) return "UNDER_100K";
        if (price < 500000) return "100K_500K";
        if (price < 1000000) return "500K_1M";
        if (price < 2000000) return "1M_2M";
        return "OVER_2M";
    }

    private LocalDateTime convertTimestamp(Long timestamp) {
        if (timestamp == null) return LocalDateTime.now();
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
    }

    // ===== UPDATE FIELD HELPER METHODS =====

    private void updateDateInfoFields(ProcessingDateInfo existing, ProcessingDateInfo newInfo) {
        existing.setFullDate(newInfo.getFullDate());
        existing.setYear(newInfo.getYear());
        existing.setMonth(newInfo.getMonth());
        existing.setDay(newInfo.getDay());
        existing.setQuarter(newInfo.getQuarter());
        existing.setDayOfWeek(newInfo.getDayOfWeek());
        existing.setDayOfYear(newInfo.getDayOfYear());
        existing.setWeekOfYear(newInfo.getWeekOfYear());
        existing.setIsWeekend(newInfo.getIsWeekend());
        existing.setSeasonName(newInfo.getSeasonName());
        existing.setBusinessQuarter(newInfo.getBusinessQuarter());
        existing.setFiscalYear(newInfo.getFiscalYear());
        existing.setHour(newInfo.getHour());
    }

    private void updatePaymentInfoFields(PaymentInfo existing, PaymentInfo newInfo) {
        existing.setPaymentMethod(newInfo.getPaymentMethod());
        existing.setPaymentCategory(newInfo.getPaymentCategory());
        existing.setIsCod(newInfo.getIsCod());
        existing.setCurrency(newInfo.getCurrency());
        existing.setTotalAmount(newInfo.getTotalAmount());
        existing.setPaymentStatus(newInfo.getPaymentStatus());
        existing.setPaymentGateway(newInfo.getPaymentGateway());
    }

    private void updateShippingInfoFields(ShippingInfo existing, ShippingInfo newInfo) {
        existing.setShippingProvider(newInfo.getShippingProvider());
        existing.setShippingProviderId(newInfo.getShippingProviderId());
        existing.setTrackingNumber(newInfo.getTrackingNumber());
        existing.setShippingMethod(newInfo.getShippingMethod());
        existing.setShippingZone(newInfo.getShippingZone());
        existing.setShippingCost(newInfo.getShippingCost());
        existing.setDeliveryDays(newInfo.getDeliveryDays());
    }

    private void updateOrderStatusFields(OrderStatus existing, OrderStatus newInfo) {
        existing.setStatusTimestamp(newInfo.getStatusTimestamp());
        existing.setIsCurrentStatus(newInfo.getIsCurrentStatus());
        existing.setStatusSource(newInfo.getStatusSource());
        existing.setStatusReason(newInfo.getStatusReason());
    }

    private void updateOrderStatusDetailFields(OrderStatusDetail existing, OrderStatusDetail newInfo) {
        existing.setStatusDescription(newInfo.getStatusDescription());
        existing.setStatusRules(newInfo.getStatusRules());
        existing.setCanCancel(newInfo.getCanCancel());
        existing.setCanReturn(newInfo.getCanReturn());
        existing.setCanModify(newInfo.getCanModify());
        existing.setEstimatedDelivery(newInfo.getEstimatedDelivery());
        existing.setCustomerNotified(newInfo.getCustomerNotified());
        existing.setInternalNotes(newInfo.getInternalNotes());
    }

    // ===== ETL RESULT CLASSES (same as ShopeeEtlService) =====

    public static class EtlResult {
        private boolean success;
        private int totalOrders;
        private int ordersProcessed;
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private long durationMs;
        private List<FailedOrder> failedOrders = new ArrayList<>();

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public int getOrdersProcessed() { return ordersProcessed; }
        public void setOrdersProcessed(int ordersProcessed) { this.ordersProcessed = ordersProcessed; }
        public void incrementProcessed() { this.ordersProcessed++; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public long getDurationMs() { return durationMs; }
        public void calculateDuration() {
            if (startTime != null && endTime != null) {
                this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
            }
        }

        public List<FailedOrder> getFailedOrders() { return failedOrders; }
        public void addFailedOrder(FailedOrder failedOrder) { this.failedOrders.add(failedOrder); }
    }

    public static class FailedOrder {
        private String orderId;
        private String errorMessage;

        public FailedOrder(String orderId, String errorMessage) {
            this.orderId = orderId;
            this.errorMessage = errorMessage;
        }

        public String getOrderId() { return orderId; }
        public String getErrorMessage() { return errorMessage; }
    }
}