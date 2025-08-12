    // ShopeeEtlService.java - Complete version with all 9 tables processing
    package com.guno.etl.service;

    import com.guno.etl.dto.ShopeeApiResponse;
    import com.guno.etl.dto.ShopeeOrderDto;
    import com.guno.etl.dto.ShopeeItemDto;
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

    import java.text.SimpleDateFormat;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.time.Instant;
    import java.time.format.DateTimeFormatter;
    import java.util.List;
    import java.util.Optional;
    import java.util.ArrayList;

    @Service
    public class ShopeeEtlService {

        private static final Logger log = LoggerFactory.getLogger(ShopeeEtlService.class);

        @Autowired
        private ShopeeApiService shopeeApiService;

        // Original 5 table repositories
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

        // New 6 table repositories
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

        // Platform configuration
        @Value("${etl.platforms.shopee.name:SHOPEE}")
        private String platformName;

        // ===== MAIN ETL METHODS FOR UPDATE MODE =====

        /**
         * Process updated orders from API (main method for scheduler)
         */
        public EtlResult processUpdatedOrders() {
            log.info("=== Starting ETL process for updated orders ===");

            EtlResult result = new EtlResult();
            result.setStartTime(LocalDateTime.now());

            try {
                // Fetch updated orders from API
                ShopeeApiResponse response = shopeeApiService.fetchUpdatedOrders();

                if (response == null || response.getStatus() != 1) {
                    String error = "API call failed: " + (response != null ? response.getMessage() : "null response");
                    log.error(error);
                    result.setSuccess(false);
                    result.setErrorMessage(error);
                    return result;
                }

                List<ShopeeOrderDto> orders = response.getData().getOrders();
                log.info("Retrieved {} updated orders from API", orders.size());

                if (orders.isEmpty()) {
                    log.info("No updated orders to process");
                    result.setSuccess(true);
                    result.setOrdersProcessed(0);
                    return result;
                }

                // Process each order individually with error isolation
                result = processOrdersWithErrorHandling(orders);

            } catch (Exception e) {
                log.error("Fatal error in ETL process: {}", e.getMessage(), e);
                result.setSuccess(false);
                result.setErrorMessage("Fatal ETL error: " + e.getMessage());
            } finally {
                result.setEndTime(LocalDateTime.now());
                result.calculateDuration();

                log.info("=== ETL process completed ===");
                log.info("Total orders processed: {}/{}", result.getOrdersProcessed(), result.getTotalOrders());
                log.info("Success rate: {}%", result.getSuccessRate());
                log.info("Duration: {} ms", result.getDurationMs());

                if (result.getFailedOrders().size() > 0) {
                    log.warn("Failed orders: {}", result.getFailedOrders().size());
                    result.getFailedOrders().forEach(failedOrder ->
                            log.warn("Failed order: {} - Reason: {}", failedOrder.getOrderId(), failedOrder.getErrorMessage())
                    );
                }
            }

            return result;
        }

        /**
         * Process orders with error isolation per order
         */
        private EtlResult processOrdersWithErrorHandling(List<ShopeeOrderDto> orders) {
            EtlResult result = new EtlResult();
            result.setTotalOrders(orders.size());

            for (ShopeeOrderDto orderDto : orders) {
                try {
                    log.debug("Processing order: {}", orderDto.getOrderId());

                    // Process single order in isolated transaction
                    boolean success = processOrderUpsert(orderDto);

                    if (success) {
                        result.incrementProcessed();
                        log.debug("Order {} processed successfully", orderDto.getOrderId());
                    } else {
                        result.addFailedOrder(orderDto.getOrderId(), "UPSERT operation failed");
                        log.error("Order {} failed during UPSERT", orderDto.getOrderId());
                    }

                } catch (Exception e) {
                    // Log error and continue with next order
                    String errorMsg = "Error processing order " + orderDto.getOrderId() + ": " + e.getMessage();
                    log.error(errorMsg, e);

                    result.addFailedOrder(orderDto.getOrderId(), e.getMessage());
                }
            }

            result.setSuccess(result.getOrdersProcessed() > 0); // Success if at least 1 order processed
            return result;
        }

        /**
         * Process single order with UPSERT logic (isolated transaction) - NOW PROCESSES ALL 9 TABLES
         */
        @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
        public boolean processOrderUpsert(ShopeeOrderDto orderDto) {
            try {
                log.debug("Starting UPSERT for order: {}", orderDto.getOrderId());

                // 1. Process Customer (UPSERT)
                String customerId = processCustomerUpsert(orderDto);
                log.debug("Customer processed: {}", customerId);

                // 2. Process Order (UPSERT)
                processOrderEntityUpsert(orderDto, customerId);
                log.debug("Order entity processed: {}", orderDto.getOrderId());

                // 3. Process Order Items (DELETE + INSERT)
                processOrderItemsUpsert(orderDto);
                log.debug("Order items processed for order: {}", orderDto.getOrderId());

                // 4. Process Geography Info (UPSERT)
                processGeographyInfoUpsert(orderDto);
                log.debug("Geography info processed for order: {}", orderDto.getOrderId());

                // 5. Process Date Info (UPSERT)
                processDateInfoUpsert(orderDto);
                log.debug("Date info processed for order: {}", orderDto.getOrderId());

                // 6. Process Payment Info (UPSERT)
                processPaymentInfoUpsert(orderDto);
                log.debug("Payment info processed for order: {}", orderDto.getOrderId());

                // 7. Process Shipping Info (UPSERT)
                processShippingInfoUpsert(orderDto);
                log.debug("Shipping info processed for order: {}", orderDto.getOrderId());

                // 8. Process Status Info (UPSERT)
                processStatusInfoUpsert(orderDto);
                log.debug("Status info processed for order: {}", orderDto.getOrderId());

                // 9. Process Order Status Transition (UPSERT)
                processOrderStatusTransition(orderDto);
                log.debug("Order status transition processed for order: {}", orderDto.getOrderId());

                // 10. Process Order Status Detail (UPSERT)
                processOrderStatusDetailUpsert(orderDto);
                log.debug("Order status detail processed for order: {}", orderDto.getOrderId());

                log.info("Order {} UPSERT completed successfully - ALL 10 TABLES", orderDto.getOrderId());
                return true;

            } catch (Exception e) {
                log.error("Error in UPSERT for order {}: {}", orderDto.getOrderId(), e.getMessage(), e);
                throw e; // Re-throw to trigger transaction rollback
            }
        }

        // ===== ORIGINAL 4 TABLE UPSERT LOGIC =====

        /**
         * Customer UPSERT - Update existing or create new
         */
        private String processCustomerUpsert(ShopeeOrderDto orderDto) {
            try {
                // Generate customer ID from phone hash
                String phone = orderDto.getData().getRecipientAddress().getPhone() != null ? orderDto.getData().getRecipientAddress().getPhone() : "Unknown";
                String phoneHash = HashUtil.hashPhone(phone) != null ? HashUtil.hashPhone(phone) : "Unknown";
                String customerId = HashUtil.generateCustomerId("SHOPEE", phoneHash);

                Optional<Customer> existingCustomer = customerRepository.findById(customerId);

                if (existingCustomer.isPresent()) {
                    // UPDATE existing customer
                    Customer customer = existingCustomer.get();
                    updateCustomerMetrics(customer, orderDto);
                    customerRepository.save(customer);
                    log.debug("Customer {} updated", customerId);
                } else {
                    // INSERT new customer
                    Customer newCustomer = createNewCustomer(customerId, phoneHash, orderDto);
                    customerRepository.save(newCustomer);
                    log.debug("Customer {} created", customerId);
                }

                return customerId;

            } catch (Exception e) {
                log.error("Error processing customer for order {}: {}", orderDto.getOrderId(), e.getMessage());
                throw new RuntimeException("Customer UPSERT failed", e);
            }
        }

        /**
         * Order UPSERT - Update existing or create new
         */
        private void processOrderEntityUpsert(ShopeeOrderDto orderDto, String customerId) {
            try {
                String orderId = orderDto.getOrderId();
                Optional<Order> existingOrder = orderRepository.findById(orderId);

                if (existingOrder.isPresent()) {
                    // UPDATE existing order
                    Order order = existingOrder.get();
                    updateOrderFields(order, orderDto, customerId);
                    orderRepository.save(order);
                    log.debug("Order {} updated", orderId);
                } else {
                    // INSERT new order
                    Order newOrder = createNewOrder(orderDto, customerId);
                    orderRepository.save(newOrder);
                    log.debug("Order {} created", orderId);
                }

            } catch (Exception e) {
                log.error("Error processing order entity {}: {}", orderDto.getOrderId(), e.getMessage());
                throw new RuntimeException("Order UPSERT failed", e);
            }
        }

        /**
         * Order Items UPSERT - Delete existing items and insert new ones
         */
        private void processOrderItemsUpsert(ShopeeOrderDto orderDto) {
            try {
                String orderId = orderDto.getOrderId();

                // DELETE existing order items
                List<OrderItem> existingItems = orderItemRepository.findByOrderIdOrderByItemSequence(orderId);
                if (!existingItems.isEmpty()) {
                    orderItemRepository.deleteAll(existingItems);
                    log.debug("Deleted {} existing order items for order {}", existingItems.size(), orderId);
                }

                // INSERT new order items
                List<ShopeeItemDto> itemDtos = orderDto.getData().getItemList();
                for (int i = 0; i < itemDtos.size(); i++) {
                    ShopeeItemDto itemDto = itemDtos.get(i);

                    // Process product first (UPSERT)
                    processProductUpsert(itemDto);

                    // Create order item
                    OrderItem orderItem = createOrderItem(orderId, itemDto, i + 1);
                    orderItemRepository.save(orderItem);
                }

                log.debug("Inserted {} new order items for order {}", itemDtos.size(), orderId);

            } catch (Exception e) {
                log.error("Error processing order items for order {}: {}", orderDto.getOrderId(), e.getMessage());
                throw new RuntimeException("Order Items UPSERT failed", e);
            }
        }

        /**
         * Product UPSERT - Update existing or create new
         */
        private void processProductUpsert(ShopeeItemDto itemDto) {
            try {
                String sku = itemDto.getModelSku();
                String platformProductId = platformName; // Use configurable platform name

                if (sku == null || sku.trim().isEmpty()) {
                    sku = "SKU_" + itemDto.getItemId(); // Generate SKU if missing
                }

                ProductId productId = new ProductId(sku, platformProductId);
                Optional<Product> existingProduct = productRepository.findById(productId);

                if (existingProduct.isPresent()) {
                    // UPDATE existing product (prices might change)
                    Product product = existingProduct.get();
                    updateProductFields(product, itemDto);
                    productRepository.save(product);
                    log.debug("Product {}/{} updated", sku, platformProductId);
                } else {
                    // INSERT new product
                    Product newProduct = createNewProduct(itemDto, sku, platformProductId);
                    productRepository.save(newProduct);
                    log.debug("Product {}/{} created", sku, platformProductId);
                }

            } catch (Exception e) {
                log.error("Error processing product for item {}: {}", itemDto.getItemId(), e.getMessage());
                throw new RuntimeException("Product UPSERT failed", e);
            }
        }

        /**
         * Geography Info UPSERT - Update existing or create new
         */
        private void processGeographyInfoUpsert(ShopeeOrderDto orderDto) {
            try {
                String orderId = orderDto.getOrderId();
                Optional<GeographyInfo> existingGeo = geographyInfoRepository.findById(orderId);

                if (existingGeo.isPresent()) {
                    // UPDATE existing geography info
                    GeographyInfo geoInfo = existingGeo.get();
                    updateGeographyFields(geoInfo, orderDto);
                    geographyInfoRepository.save(geoInfo);
                    log.debug("Geography info {} updated", orderId);
                } else {
                    // INSERT new geography info
                    GeographyInfo newGeoInfo = createNewGeographyInfo(orderDto);
                    geographyInfoRepository.save(newGeoInfo);
                    log.debug("Geography info {} created", orderId);
                }

            } catch (Exception e) {
                log.error("Error processing geography info for order {}: {}", orderDto.getOrderId(), e.getMessage());
                throw new RuntimeException("Geography UPSERT failed", e);
            }
        }

        // ===== NEW: 6 MISSING TABLE PROCESSING METHODS =====

        /**
         * Date Info UPSERT - Update existing or create new
         */
        private void processDateInfoUpsert(ShopeeOrderDto orderDto) {
            try {
                String orderId = orderDto.getOrderId();
                log.debug("Processing date info for order: {}", orderId);

                // Check if date info already exists for this order
                Optional<ProcessingDateInfo> existingDateInfo = processingDateInfoRepository.findById(orderId);

                if (existingDateInfo.isPresent()) {
                    // UPDATE existing date info
                    ProcessingDateInfo existing = existingDateInfo.get();
                    ProcessingDateInfo newDateInfo = createProcessingDateInfoEntity(orderDto);

                    if (newDateInfo != null) {
                        // Update fields - direct mapping, no calculations
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

                        processingDateInfoRepository.save(existing);
                        log.debug("Updated date info for order: {}", orderId);
                    } else {
                        log.warn("Failed to create new date info entity for order: {}", orderId);
                    }
                } else {
                    // INSERT new date info
                    ProcessingDateInfo newDateInfo = createProcessingDateInfoEntity(orderDto);
                    if (newDateInfo != null) {
                        processingDateInfoRepository.save(newDateInfo);
                        log.debug("Created new date info for order: {}", orderId);
                    } else {
                        log.warn("Failed to create date info entity for order: {}", orderId);
                    }
                }

            } catch (Exception e) {
                // Individual table error isolation - log error and continue
                log.error("Failed to process date info for order {}: {}",
                        orderDto.getOrderId(), e.getMessage());
                // Don't throw exception - continue with other tables
            }
        }

        /**
         * Payment Info UPSERT - Update existing or create new
         */
        private void processPaymentInfoUpsert(ShopeeOrderDto orderDto) {
            try {
                String orderId = orderDto.getOrderId();
                log.debug("Processing payment info for order: {}", orderId);

                // Check if payment info already exists for this order
                Optional<PaymentInfo> existingPaymentInfo = paymentInfoRepository.findById(orderId);

                if (existingPaymentInfo.isPresent()) {
                    // UPDATE existing payment info
                    PaymentInfo existing = existingPaymentInfo.get();
                    PaymentInfo newPaymentInfo = createPaymentInfoEntity(orderDto);

                    if (newPaymentInfo != null) {
                        // Update fields - direct mapping, no calculations
                        existing.setPaymentMethod(newPaymentInfo.getPaymentMethod());
                        existing.setPaymentCategory(newPaymentInfo.getPaymentCategory());
                        existing.setPaymentProvider(newPaymentInfo.getPaymentProvider());
                        existing.setIsCod(newPaymentInfo.getIsCod());
                        existing.setIsPrepaid(newPaymentInfo.getIsPrepaid());
                        existing.setIsInstallment(newPaymentInfo.getIsInstallment());
                        existing.setInstallmentMonths(newPaymentInfo.getInstallmentMonths());
                        existing.setSupportsRefund(newPaymentInfo.getSupportsRefund());
                        existing.setSupportsPartialRefund(newPaymentInfo.getSupportsPartialRefund());
                        existing.setRefundProcessingDays(newPaymentInfo.getRefundProcessingDays());
                        existing.setRiskLevel(newPaymentInfo.getRiskLevel());
                        existing.setRequiresVerification(newPaymentInfo.getRequiresVerification());
                        existing.setFraudScore(newPaymentInfo.getFraudScore());
                        existing.setTransactionFeeRate(newPaymentInfo.getTransactionFeeRate());
                        existing.setProcessingFee(newPaymentInfo.getProcessingFee());
                        existing.setPaymentProcessingTimeMinutes(newPaymentInfo.getPaymentProcessingTimeMinutes());
                        existing.setSettlementDays(newPaymentInfo.getSettlementDays());

                        paymentInfoRepository.save(existing);
                        log.debug("Updated payment info for order: {}", orderId);
                    } else {
                        log.warn("Failed to create new payment info entity for order: {}", orderId);
                    }
                } else {
                    // INSERT new payment info
                    PaymentInfo newPaymentInfo = createPaymentInfoEntity(orderDto);
                    if (newPaymentInfo != null) {
                        paymentInfoRepository.save(newPaymentInfo);
                        log.debug("Created new payment info for order: {}", orderId);
                    } else {
                        log.warn("Failed to create payment info entity for order: {}", orderId);
                    }
                }

            } catch (Exception e) {
                // Individual table error isolation - log error and continue
                log.error("Failed to process payment info for order {}: {}",
                        orderDto.getOrderId(), e.getMessage());
                // Don't throw exception - continue with other tables
            }
        }

        /**
         * Shipping Info UPSERT - Update existing or create new
         */
        private void processShippingInfoUpsert(ShopeeOrderDto orderDto) {
            try {
                String orderId = orderDto.getOrderId();
                log.debug("Processing shipping info for order: {}", orderId);

                // Check if shipping info already exists for this order
                Optional<ShippingInfo> existingShippingInfo = shippingInfoRepository.findById(orderId);

                if (existingShippingInfo.isPresent()) {
                    // UPDATE existing shipping info
                    ShippingInfo existing = existingShippingInfo.get();
                    ShippingInfo newShippingInfo = createShippingInfoEntity(orderDto);

                    if (newShippingInfo != null) {
                        // Update fields - direct mapping, no calculations
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
                        existing.setBaseFee(newShippingInfo.getBaseFee());
                        existing.setWeightBasedFee(newShippingInfo.getWeightBasedFee());
                        existing.setDistanceBasedFee(newShippingInfo.getDistanceBasedFee());
                        existing.setCodFee(newShippingInfo.getCodFee());
                        existing.setInsuranceFee(newShippingInfo.getInsuranceFee());
                        existing.setSupportsCod(newShippingInfo.getSupportsCod());
                        existing.setSupportsInsurance(newShippingInfo.getSupportsInsurance());
                        existing.setSupportsFragile(newShippingInfo.getSupportsFragile());
                        existing.setSupportsRefrigerated(newShippingInfo.getSupportsRefrigerated());
                        existing.setProvidesTracking(newShippingInfo.getProvidesTracking());
                        existing.setProvidesSmsUpdates(newShippingInfo.getProvidesSmsUpdates());
                        existing.setAverageDeliveryDays(newShippingInfo.getAverageDeliveryDays());
                        existing.setOnTimeDeliveryRate(newShippingInfo.getOnTimeDeliveryRate());
                        existing.setSuccessDeliveryRate(newShippingInfo.getSuccessDeliveryRate());
                        existing.setDamageRate(newShippingInfo.getDamageRate());
                        existing.setCoverageProvinces(newShippingInfo.getCoverageProvinces());
                        existing.setCoverageNationwide(newShippingInfo.getCoverageNationwide());
                        existing.setCoverageInternational(newShippingInfo.getCoverageInternational());

                        shippingInfoRepository.save(existing);
                        log.debug("Updated shipping info for order: {}", orderId);
                    } else {
                        log.warn("Failed to create new shipping info entity for order: {}", orderId);
                    }
                } else {
                    // INSERT new shipping info
                    ShippingInfo newShippingInfo = createShippingInfoEntity(orderDto);
                    if (newShippingInfo != null) {
                        shippingInfoRepository.save(newShippingInfo);
                        log.debug("Created new shipping info for order: {}", orderId);
                    } else {
                        log.warn("Failed to create shipping info entity for order: {}", orderId);
                    }
                }

            } catch (Exception e) {
                // Individual table error isolation - log error and continue
                log.error("Failed to process shipping info for order {}: {}",
                        orderDto.getOrderId(), e.getMessage());
                // Don't throw exception - continue with other tables
            }
        }

        /**
         * Status Info UPSERT - Update existing or create new
         */
        private void processStatusInfoUpsert(ShopeeOrderDto orderDto) {
            try {
                String orderId = orderDto.getOrderId();
                log.debug("Processing status info for order: {}", orderId);

                // Map Shopee status to standard status
                String shopeeStatus = orderDto.getOrderStatus(); // Direct from API
                String standardStatus = mapShopeeStatusToStandard(shopeeStatus);

                // Check if status already exists for this platform + status combination
                Optional<Status> existingStatus = statusRepository.findByPlatformAndPlatformStatusCode(
                        platformName, shopeeStatus);

                if (existingStatus.isPresent()) {
                    // UPDATE existing status if needed
                    Status existing = existingStatus.get();
                    existing.setStandardStatusName(standardStatus);
                    existing.setStatusCategory(determineStatusCategory(standardStatus));

                    statusRepository.save(existing);
                    log.debug("Updated status mapping for platform {} status {}", platformName, shopeeStatus);
                } else {
                    // INSERT new status mapping
                    Status newStatus = createStatusEntity(orderDto);
                    if (newStatus != null) {
                        statusRepository.save(newStatus);
                        log.debug("Created new status mapping for platform {} status {}", platformName, shopeeStatus);
                    } else {
                        log.warn("Failed to create status entity for order: {}", orderId);
                    }
                }

            } catch (Exception e) {
                // Individual table error isolation - log error and continue
                log.error("Failed to process status info for order {}: {}",
                        orderDto.getOrderId(), e.getMessage());
                // Don't throw exception - continue with other tables
            }
        }

        /**
         * Order Status Transition UPSERT - Update existing or create new
         */
        private void processOrderStatusTransition(ShopeeOrderDto orderDto) {
            try {
                String orderId = orderDto.getOrderId();
                log.debug("Processing order status transition for order: {}", orderId);

                // Get status key for current order status
                String shopeeStatus = orderDto.getOrderStatus();
                Optional<Status> statusEntity = statusRepository.findByPlatformAndPlatformStatusCode(
                        platformName, shopeeStatus);

                if (!statusEntity.isPresent()) {
                    log.warn("Status entity not found for platform {} status {}, skipping transition",
                            platformName, shopeeStatus);
                    return;
                }

                Long statusKey = statusEntity.get().getStatusKey();

                // Check if this status transition already exists for this order
                Optional<OrderStatus> existingTransition = orderStatusRepository.findByStatusKeyAndOrderId(
                        statusKey, orderId);

                if (existingTransition.isPresent()) {
                    // UPDATE existing transition
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
                        log.debug("Updated status transition for order: {} to status: {}", orderId, shopeeStatus);
                    } else {
                        log.warn("Failed to create new status transition entity for order: {}", orderId);
                    }
                } else {
                    // INSERT new transition
                    OrderStatus newTransition = createOrderStatusEntity(orderDto, statusKey);
                    if (newTransition != null) {
                        orderStatusRepository.save(newTransition);
                        log.debug("Created new status transition for order: {} to status: {}", orderId, shopeeStatus);
                    } else {
                        log.warn("Failed to create status transition entity for order: {}", orderId);
                    }
                }

            } catch (Exception e) {
                // Individual table error isolation - log error and continue
                log.error("Failed to process order status transition for order {}: {}",
                        orderDto.getOrderId(), e.getMessage());
                // Don't throw exception - continue with other tables
            }
        }

        /**
         * Order Status Detail UPSERT - Update existing or create new
         */
        private void processOrderStatusDetailUpsert(ShopeeOrderDto orderDto) {
            try {
                String orderId = orderDto.getOrderId();
                log.debug("Processing order status detail for order: {}", orderId);

                // Get status key for current order status
                String shopeeStatus = orderDto.getOrderStatus();
                Optional<Status> statusEntity = statusRepository.findByPlatformAndPlatformStatusCode(
                        platformName, shopeeStatus);

                if (!statusEntity.isPresent()) {
                    log.warn("Status entity not found for platform {} status {}, skipping status detail",
                            platformName, shopeeStatus);
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
                        log.debug("Updated status detail for order: {} status: {}", orderId, shopeeStatus);
                    } else {
                        log.warn("Failed to create new status detail entity for order: {}", orderId);
                    }
                } else {
                    // INSERT new status detail
                    OrderStatusDetail newDetail = createOrderStatusDetailEntity(orderDto, statusKey);
                    if (newDetail != null) {
                        orderStatusDetailRepository.save(newDetail);
                        log.debug("Created new status detail for order: {} status: {}", orderId, shopeeStatus);
                    } else {
                        log.warn("Failed to create status detail entity for order: {}", orderId);
                    }
                }

            } catch (Exception e) {
                // Individual table error isolation - log error and continue
                log.error("Failed to process order status detail for order {}: {}",
                        orderDto.getOrderId(), e.getMessage());
                // Don't throw exception - continue with other tables
            }
        }

        // ===== ENTITY CREATION METHODS (ORIGINAL) =====

        private Customer createNewCustomer(String customerId, String phoneHash, ShopeeOrderDto orderDto) {
            String email = null; // Shopee doesn't provide email in current format
            String emailHash = email != null ? HashUtil.hashEmail(email) : null;

            return Customer.builder()
                    .customerId(customerId)
                    .customerKey(customerRepository.findNextCustomerKey())
                    .phoneHash(phoneHash)
                    .emailHash(emailHash)
                    .gender(null) // Not available in current format
                    .acquisitionChannel("SHOPEE")
                    .totalOrders(1)
                    .totalSpent(orderDto.getTotalAmount().doubleValue()) // Direct value, no division
                    .averageOrderValue(orderDto.getTotalAmount().doubleValue()) // Direct value
                    .totalItemsPurchased(orderDto.getData().getItemList().size())
                    .daysSinceFirstOrder(0)
                    .daysSinceLastOrder(0)
                    .purchaseFrequencyDays(0.0)
                    .returnRate(0.0)
                    .cancellationRate(0.0)
                    .codPreferenceRate(orderDto.getData().getCod() ? 1.0 : 0.0)
                    .preferredPlatform(platformName) // Use configurable platform name
                    .primaryShippingProvince(orderDto.getData().getRecipientAddress().getState())
                    .shipsToMultipleProvinces(false)
                    .loyaltyPoints(0)
                    .referralCount(0)
                    .isReferrer(false)
                    .build();
        }

        private Order createNewOrder(ShopeeOrderDto orderDto, String customerId) {
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

        private OrderItem createOrderItem(String orderId, ShopeeItemDto itemDto, int sequence) {
            String sku = itemDto.getModelSku();
            String platformProductId = platformName; // Use configurable platform name

            if (sku == null || sku.trim().isEmpty()) {
                sku = "SKU_" + itemDto.getItemId();
            }

            return OrderItem.builder()
                    .orderId(orderId)
                    .sku(sku)
                    .platformProductId(platformProductId)
                    .quantity(itemDto.getModelQuantityPurchased())
                    .unitPrice(itemDto.getModelDiscountedPrice().doubleValue()) // Direct value, no division
                    .totalPrice((double) (itemDto.getModelDiscountedPrice() * itemDto.getModelQuantityPurchased().longValue())) // Cast to long first
                    .itemDiscount((double) (itemDto.getModelOriginalPrice() - itemDto.getModelDiscountedPrice())) // Cast to long first
                    .promotionType(null) // Not provided in current format
                    .promotionCode(null) // Not provided in current format
                    .itemStatus("ACTIVE")
                    .itemSequence(sequence)
                    .opId(System.currentTimeMillis() + sequence) // Generate unique OP ID
                    .build();
        }

        private Product createNewProduct(ShopeeItemDto itemDto, String sku, String platformProductId) {
            return Product.builder()
                    .sku(sku)
                    .platformProductId(platformProductId)
                    .productId(String.valueOf(itemDto.getItemId())) // Item ID goes to productId
                    .productName(itemDto.getItemName())
                    .brand(null) // Not provided in current format
                    .categoryLevel1(null) // Not provided
                    .color(null) // Not provided
                    .size(null) // Not provided
                    .weightGram(0) // Not provided
                    .costPrice(0.0) // Not provided
                    .retailPrice(itemDto.getModelDiscountedPrice().doubleValue()) // Direct value, no division
                    .originalPrice(itemDto.getModelOriginalPrice().doubleValue()) // Direct value, no division
                    .priceRange(calculatePriceRange(itemDto.getModelDiscountedPrice().doubleValue())) // Use direct value
                    .isActive(true)
                    .isFeatured(false)
                    .isSeasonal(false)
                    .isNewArrival(false)
                    .isBestSeller(false)
                    .primaryImageUrl(itemDto.getImageInfo() != null ? itemDto.getImageInfo().getImageUrl() : null)
                    .imageCount(1)
                    .build();
        }

        private GeographyInfo createNewGeographyInfo(ShopeeOrderDto orderDto) {
            var address = orderDto.getData().getRecipientAddress();

            return GeographyInfo.builder()
                    .orderId(orderDto.getOrderId())
                    .geographyKey(geographyInfoRepository.findNextGeographyKey())
                    .countryCode("VN")
                    .countryName("Vietnam")
                    .provinceName(address.getState())
                    .districtName(address.getDistrict())
                    .isUrban(isUrbanArea(address.getState(), address.getDistrict()))
                    .isMetropolitan(isMetropolitanArea(address.getState()))
                    .isCoastal(false) // Would need lookup table
                    .isBorder(false) // Would need lookup table
                    .economicTier(calculateEconomicTier(address.getState()))
                    .populationDensity("MEDIUM") // Default
                    .incomeLevel("MEDIUM") // Default
                    .shippingZone("ZONE_1") // Default
                    .deliveryComplexity("STANDARD")
                    .standardDeliveryDays(orderDto.getData().getDaysToShip())
                    .expressDeliveryAvailable(true)
                    .latitude(0.0) // Would need geocoding
                    .longitude(0.0) // Would need geocoding
                    .build();
        }

        // ===== NEW: ENTITY CREATION METHODS FOR NEW TABLES =====

        private ProcessingDateInfo createProcessingDateInfoEntity(ShopeeOrderDto orderDto) {
            try {
                String orderId = orderDto.getOrderId();

                // Get create time from API (Unix timestamp)
                Long createTimeUnix = orderDto.getCreateTime();
                LocalDateTime createTime = createTimeUnix != null ?
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(createTimeUnix), java.time.ZoneId.systemDefault()) :
                        LocalDateTime.now(); // Default: current time

                LocalDate fullDate = createTime.toLocalDate();

                // Extract date components - NO CALCULATIONS, direct values
                int dayOfWeek = fullDate.getDayOfWeek().getValue(); // 1-7
                String dayOfWeekName = fullDate.getDayOfWeek().name(); // MONDAY, TUESDAY...
                int dayOfMonth = fullDate.getDayOfMonth(); // 1-31
                int dayOfYear = fullDate.getDayOfYear(); // 1-366
                int weekOfYear = fullDate.get(java.time.temporal.WeekFields.ISO.weekOfYear()); // 1-53
                int monthOfYear = fullDate.getMonthValue(); // 1-12
                String monthName = fullDate.getMonth().name(); // JANUARY, FEBRUARY...
                int quarterOfYear = (monthOfYear - 1) / 3 + 1; // 1-4, direct calculation
                String quarterName = "Q" + quarterOfYear; // Q1, Q2, Q3, Q4
                int year = fullDate.getYear(); // Direct value

                // Business day logic - simple rules
                boolean isWeekend = dayOfWeek == 6 || dayOfWeek == 7; // Saturday or Sunday
                boolean isHoliday = false; // Default: no holiday detection
                String holidayName = null; // Default: no holiday
                boolean isBusinessDay = !isWeekend && !isHoliday; // Simple logic

                // Fiscal year - same as calendar year (no special fiscal logic)
                int fiscalYear = year; // Direct mapping
                int fiscalQuarter = quarterOfYear; // Same as calendar quarter

                // Shopping season detection - simple rules
                boolean isShoppingSeason = monthOfYear == 11 || monthOfYear == 12; // Nov-Dec
                String seasonName = isShoppingSeason ? "HOLIDAY_SEASON" : "REGULAR"; // Simple mapping

                // Peak hour detection - based on create time hour
                int hour = createTime.getHour();
                boolean isPeakHour = hour >= 9 && hour <= 21; // 9AM-9PM

                // Generate unique date key
                Long dateKey = processingDateInfoRepository.findNextDateKey();

                return ProcessingDateInfo.builder()
                        .orderId(orderId)
                        .dateKey(dateKey)
                        .fullDate(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(createTime)) // Full timestamp
                        .dayOfWeek(dayOfWeek)
                        .dayOfWeekName(dayOfWeekName)
                        .dayOfMonth(dayOfMonth)
                        .dayOfYear(dayOfYear)
                        .weekOfYear(weekOfYear)
                        .monthOfYear(monthOfYear)
                        .monthName(monthName)
                        .quarterOfYear(quarterOfYear)
                        .quarterName(quarterName)
                        .year(year)
                        .isWeekend(isWeekend)
                        .isHoliday(isHoliday)
                        .holidayName(holidayName)
                        .isBusinessDay(isBusinessDay)
                        .fiscalYear(fiscalYear)
                        .fiscalQuarter(fiscalQuarter)
                        .isShoppingSeason(isShoppingSeason)
                        .seasonName(seasonName)
                        .isPeakHour(isPeakHour)
                        .build();

            } catch (Exception e) {
                log.error("Error creating ProcessingDateInfo entity for order {}: {}",
                        orderDto.getOrderId(), e.getMessage());
                return null;
            }
        }

        // Thay thế method hiện tại (dòng ~650)
        private PaymentInfo createPaymentInfoEntity(ShopeeOrderDto orderDto) {
            try {
                String orderId = orderDto.getOrderId();

                // Get payment data from API - direct mapping
                String paymentMethod = orderDto.getData().getPaymentMethod(); // Direct value
                Boolean isCod = orderDto.getData().getCod(); // Direct value

                // Set defaults if missing
                if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                    paymentMethod = isCod ? "Cash on Delivery" : "Unknown"; // Default
                }

                // Payment category detection - simple mapping
                String paymentCategory = detectPaymentCategory(paymentMethod, isCod);

                // Payment provider detection - extract from method name
                String paymentProvider = detectPaymentProvider(paymentMethod);

                // Generate unique payment key
                Long paymentKey = paymentInfoRepository.findNextPaymentKey();

                return PaymentInfo.builder()
                        .orderId(orderId)
                        .paymentKey(paymentKey)
                        .paymentMethod(paymentMethod) // Direct from API
                        .paymentCategory(paymentCategory) // Detected category
                        .paymentProvider(paymentProvider) // Detected provider
                        .isCod(isCod) // Direct value
                        .isPrepaid(!isCod) // Opposite of COD
                        .isInstallment(false) // Default: no installment
                        .installmentMonths(0) // Default: no installment
                        .supportsRefund(true) // Default: supports refund
                        .supportsPartialRefund(true) // Default: supports partial refund
                        .refundProcessingDays(7) // Default: 7 days
                        .riskLevel("LOW") // Default: low risk
                        .requiresVerification(false) // Default: no verification required
                        .fraudScore(0.0) // Default: no fraud detected
                        .transactionFeeRate(0.0) // Default: no fee (would need lookup)
                        .processingFee(0.0) // Default: no processing fee
                        .paymentProcessingTimeMinutes(5) // Default: 5 minutes
                        .settlementDays(1) // Default: next day settlement
                        .build();

            } catch (Exception e) {
                log.error("Error creating PaymentInfo entity for order {}: {}",
                        orderDto.getOrderId(), e.getMessage());
                return null;
            }
        }

        private ShippingInfo createShippingInfoEntity(ShopeeOrderDto orderDto) {
            try {
                String orderId = orderDto.getOrderId();

                // Get shipping data from API - direct mapping
                String providerName = orderDto.getData().getShippingCarrier(); // Direct value
                Double actualShippingFee = orderDto.getData().getActualShippingFee().doubleValue(); // Direct value
                Double estimatedShippingFee = orderDto.getData().getEstimatedShippingFee().doubleValue(); // Direct value
                Integer shippingWeight = orderDto.getData().getOrderChargeableWeightGram(); // Direct value
                Integer daysToShip = orderDto.getData().getDaysToShip(); // Direct value
                Boolean isCod = orderDto.getData().getCod(); // Direct value

                // Set defaults if missing
                if (providerName == null || providerName.trim().isEmpty()) {
                    providerName = "Unknown Carrier"; // Default
                }

                // Provider classification
                String providerId = generateProviderId(providerName);
                String providerType = classifyProviderType(providerName);
                String providerTier = classifyProviderTier(providerName);

                // Service classification based on delivery days
                String serviceTier = classifyServiceTier(daysToShip);
                String deliveryCommitment = generateDeliveryCommitment(daysToShip);

                // Generate unique shipping key
                Long shippingKey = shippingInfoRepository.findNextShippingKey();

                return ShippingInfo.builder()
                        .orderId(orderId)
                        .shippingKey(shippingKey)
                        .providerId(providerId) // Generated ID
                        .providerName(providerName) // Direct from API
                        .providerType(providerType) // Classified type
                        .providerTier(providerTier) // Classified tier
                        .serviceType("STANDARD") // Default service type
                        .serviceTier(serviceTier) // Based on delivery days
                        .deliveryCommitment(deliveryCommitment) // Based on days to ship
                        .shippingMethod("COURIER") // Default: courier delivery
                        .pickupType("SCHEDULED") // Default: scheduled pickup
                        .deliveryType("DOOR_TO_DOOR") // Default: door to door
                        .baseFee(actualShippingFee) // Direct value from API
                        .weightBasedFee(0.0) // Default: no separate weight fee
                        .distanceBasedFee(0.0) // Default: no separate distance fee
                        .codFee(isCod ? 5000.0 : 0.0) // Default COD fee if applicable
                        .insuranceFee(0.0) // Default: no insurance fee
                        .supportsCod(true) // Default: supports COD
                        .supportsInsurance(true) // Default: supports insurance
                        .supportsFragile(false) // Default: no special fragile handling
                        .supportsRefrigerated(false) // Default: no refrigerated transport
                        .providesTracking(true) // Default: provides tracking
                        .providesSmsUpdates(false) // Default: no SMS updates
                        .averageDeliveryDays(daysToShip.doubleValue()) // Direct from API
                        .onTimeDeliveryRate(0.85) // Default: 85% on-time rate
                        .successDeliveryRate(0.95) // Default: 95% success rate
                        .damageRate(0.01) // Default: 1% damage rate
                        .coverageProvinces("NATIONWIDE") // Default: nationwide coverage
                        .coverageNationwide(true) // Default: nationwide
                        .coverageInternational(false) // Default: domestic only
                        .build();

            } catch (Exception e) {
                log.error("Error creating ShippingInfo entity for order {}: {}",
                        orderDto.getOrderId(), e.getMessage());
                return null;
            }
        }

        private Status createStatusEntity(ShopeeOrderDto orderDto) {
            try {
                // Get status data from API - direct mapping
                String shopeeStatus = orderDto.getOrderStatus(); // Direct value
                String standardStatus = mapShopeeStatusToStandard(shopeeStatus);
                String statusCategory = determineStatusCategory(standardStatus);

                // Generate unique status key
                Long statusKey = statusRepository.findNextStatusKey();

                return Status.builder()
                        .statusKey(statusKey)
                        .platform(platformName) // Use configurable platform name
                        .platformStatusCode(shopeeStatus) // Direct from API
                        .platformStatusName(shopeeStatus) // Same as code for Shopee
                        .standardStatusCode(standardStatus.toUpperCase().replaceAll(" ", "_")) // Generate code
                        .standardStatusName(standardStatus) // Mapped standard name
                        .statusCategory(statusCategory) // Determined category
                        .build();

            } catch (Exception e) {
                log.error("Error creating Status entity for order {}: {}",
                        orderDto.getOrderId(), e.getMessage());
                return null;
            }
        }

        private OrderStatus createOrderStatusEntity(ShopeeOrderDto orderDto) {
            // This method needs statusKey parameter, so we create an overloaded version
            return createOrderStatusEntity(orderDto, null);
        }

        private OrderStatus createOrderStatusEntity(ShopeeOrderDto orderDto, Long statusKey) {
            try {
                String orderId = orderDto.getOrderId();

                // If statusKey not provided, look it up
                if (statusKey == null) {
                    String shopeeStatus = orderDto.getOrderStatus();
                    Optional<Status> statusEntity = statusRepository.findByPlatformAndPlatformStatusCode(
                            platformName, shopeeStatus);
                    if (statusEntity.isPresent()) {
                        statusKey = statusEntity.get().getStatusKey();
                    } else {
                        log.warn("Cannot find status key for order {}", orderId);
                        return null;
                    }
                }

                // Get timestamp data from API - direct mapping
                Long updateTimeUnix = orderDto.getUpdateTime(); // Direct value
                LocalDateTime transitionTime = updateTimeUnix != null ?
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(updateTimeUnix), java.time.ZoneId.systemDefault()) :
                        LocalDateTime.now(); // Default: current time

                // Generate date key for transition date
                Integer transitionDateKey = generateDateKey(transitionTime.toLocalDate());

                // Calculate duration from previous status (simplified)
                Integer durationHours = calculateDurationFromPrevious(orderId, transitionTime);

                // Generate unique history key
                Long historyKey = orderStatusRepository.findNextHistoryKey();

                return OrderStatus.builder()
                        .statusKey(statusKey) // From status lookup
                        .orderId(orderId) // Direct from API
                        .transitionDateKey(transitionDateKey) // Generated from timestamp
                        .transitionTimestamp(transitionTime) // Converted from Unix timestamp
                        .durationInPreviousStatusHours(durationHours) // Calculated duration
                        .transitionReason("STATUS_UPDATE") // Default reason
                        .transitionTrigger("SYSTEM") // Default trigger
                        .changedBy("SHOPEE_API") // Default changed by
                        .isOnTimeTransition(true) // Default: assume on time
                        .isExpectedTransition(true) // Default: assume expected
                        .historyKey(historyKey) // Generated unique key
                        .build();

            } catch (Exception e) {
                log.error("Error creating OrderStatus entity for order {}: {}",
                        orderDto.getOrderId(), e.getMessage());
                return null;
            }
        }

        private OrderStatusDetail createOrderStatusDetailEntity(ShopeeOrderDto orderDto) {
            // This method needs statusKey parameter, so we create an overloaded version
            return createOrderStatusDetailEntity(orderDto, null);
        }

        private OrderStatusDetail createOrderStatusDetailEntity(ShopeeOrderDto orderDto, Long statusKey) {
            try {
                String orderId = orderDto.getOrderId();
                String shopeeStatus = orderDto.getOrderStatus();

                // If statusKey not provided, look it up
                if (statusKey == null) {
                    Optional<Status> statusEntity = statusRepository.findByPlatformAndPlatformStatusCode(
                            platformName, shopeeStatus);
                    if (statusEntity.isPresent()) {
                        statusKey = statusEntity.get().getStatusKey();
                    } else {
                        log.warn("Cannot find status key for order {}", orderId);
                        return null;
                    }
                }

                // Determine status characteristics based on Shopee status
                boolean isActiveOrder = determineIfActiveOrder(shopeeStatus);
                boolean isCompletedOrder = determineIfCompletedOrder(shopeeStatus);
                boolean isRevenueRecognized = determineIfRevenueRecognized(shopeeStatus);
                boolean isRefundable = determineIfRefundable(shopeeStatus);
                boolean isCancellable = determineIfCancellable(shopeeStatus);
                boolean isTrackable = determineIfTrackable(shopeeStatus);
                boolean requiresManualAction = determineIfRequiresManualAction(shopeeStatus);
                boolean customerVisible = true; // Default: all statuses visible to customer

                // Generate next possible statuses
                String nextPossibleStatuses = generateNextPossibleStatuses(shopeeStatus);

                // Auto transition hours based on status
                Integer autoTransitionHours = determineAutoTransitionHours(shopeeStatus);

                // Status display properties
                String statusColor = determineStatusColor(shopeeStatus);
                String statusIcon = determineStatusIcon(shopeeStatus);
                String customerDescription = generateCustomerDescription(shopeeStatus);

                // Performance metrics (defaults - would need historical data for accuracy)
                Double averageDurationHours = getDefaultAverageDuration(shopeeStatus);
                Double successRate = getDefaultSuccessRate(shopeeStatus);

                return OrderStatusDetail.builder()
                        .statusKey(statusKey) // From status lookup
                        .orderId(orderId) // Direct from API
                        .isActiveOrder(isActiveOrder) // Determined from status
                        .isCompletedOrder(isCompletedOrder) // Determined from status
                        .isRevenueRecognized(isRevenueRecognized) // Determined from status
                        .isRefundable(isRefundable) // Determined from status
                        .isCancellable(isCancellable) // Determined from status
                        .isTrackable(isTrackable) // Determined from status
                        .nextPossibleStatuses(nextPossibleStatuses) // Generated list
                        .autoTransitionHours(autoTransitionHours) // Status-specific timing
                        .requiresManualAction(requiresManualAction) // Determined from status
                        .statusColor(statusColor) // Display color
                        .statusIcon(statusIcon) // Display icon
                        .customerVisible(customerVisible) // Visibility flag
                        .customerDescription(customerDescription) // Customer-friendly description
                        .averageDurationHours(averageDurationHours) // Default duration
                        .successRate(successRate) // Default success rate
                        .build();

            } catch (Exception e) {
                log.error("Error creating OrderStatusDetail entity for order {}: {}",
                        orderDto.getOrderId(), e.getMessage());
                return null;
            }
        }

        // ===== UPDATE METHODS =====

        private void updateCustomerMetrics(Customer customer, ShopeeOrderDto orderDto) {
            // Update metrics based on new order - direct values, no division
            customer.setTotalOrders(customer.getTotalOrders() + 1);
            customer.setTotalSpent(customer.getTotalSpent() + orderDto.getTotalAmount().doubleValue());
            customer.setTotalItemsPurchased(customer.getTotalItemsPurchased() + orderDto.getData().getItemList().size());

            // Recalculate derived fields
            customer.recalculateMetrics();
        }

        private void updateOrderFields(Order order, ShopeeOrderDto orderDto, String customerId) {
            // Update all order fields with new data - direct values, no division
            order.setGrossRevenue(orderDto.getTotalAmount().doubleValue());
            order.setNetRevenue((double) (orderDto.getTotalAmount() - orderDto.getData().getActualShippingFee()));
            order.setShippingFee(orderDto.getData().getActualShippingFee().doubleValue());
            order.setIsDelivered("COMPLETED".equals(orderDto.getOrderStatus()));
            order.setActualShippingFee(orderDto.getData().getActualShippingFee().doubleValue());
            order.setShippingWeightGram(orderDto.getData().getOrderChargeableWeightGram());
            // Add other fields as needed
        }

        private void updateProductFields(Product product, ShopeeItemDto itemDto) {
            // Update product pricing (prices might change) - direct values, no division
            product.setRetailPrice(itemDto.getModelDiscountedPrice().doubleValue());
            product.setOriginalPrice(itemDto.getModelOriginalPrice().doubleValue());
            product.setPriceRange(calculatePriceRange(itemDto.getModelDiscountedPrice().doubleValue()));

            // Update image if available
            if (itemDto.getImageInfo() != null && itemDto.getImageInfo().getImageUrl() != null) {
                product.setPrimaryImageUrl(itemDto.getImageInfo().getImageUrl());
            }
        }

        private void updateGeographyFields(GeographyInfo geoInfo, ShopeeOrderDto orderDto) {
            var address = orderDto.getData().getRecipientAddress();

            // Update address fields (might have changed)
            geoInfo.setProvinceName(address.getState());
            geoInfo.setDistrictName(address.getDistrict());
            geoInfo.setIsUrban(isUrbanArea(address.getState(), address.getDistrict()));
            geoInfo.setIsMetropolitan(isMetropolitanArea(address.getState()));
            geoInfo.setStandardDeliveryDays(orderDto.getData().getDaysToShip());
        }

        // ===== UTILITY METHODS =====

        private LocalDateTime convertTimestamp(Long timestamp) {
            if (timestamp == null || timestamp == 0) {
                return LocalDateTime.now();
            }
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), java.time.ZoneId.systemDefault());
        }

        private String calculatePriceRange(Double price) {
            // Use direct price values - no division needed
            if (price < 50000) return "UNDER_50K";
            if (price < 100000) return "50K_100K";
            if (price < 500000) return "100K_500K";
            if (price < 1000000) return "500K_1M";
            return "OVER_1M";
        }

        private boolean isUrbanArea(String province, String district) {
            // Simple logic - can be enhanced with lookup table
            return province != null && (
                    province.contains("Hà Nội") ||
                            province.contains("TP HCM") ||
                            province.contains("Đà Nẵng") ||
                            district != null && district.toLowerCase().contains("quận")
            );
        }

        private boolean isMetropolitanArea(String province) {
            return province != null && (
                    province.contains("Hà Nội") ||
                            province.contains("TP HCM") ||
                            province.contains("Đà Nẵng")
            );
        }

        private String calculateEconomicTier(String province) {
            if (province == null) return "TIER_3";

            if (province.contains("Hà Nội") || province.contains("TP HCM")) {
                return "TIER_1";
            } else if (province.contains("Đà Nẵng") || province.contains("Hải Phòng")) {
                return "TIER_2";
            }
            return "TIER_3";
        }

        // Thêm vào cuối section UTILITY METHODS (sau calculateEconomicTier method)

        private String detectPaymentCategory(String paymentMethod, Boolean isCod) {
            if (isCod != null && isCod) {
                return "CASH_ON_DELIVERY";
            }

            if (paymentMethod == null) {
                return "UNKNOWN";
            }

            String method = paymentMethod.toLowerCase();
            if (method.contains("shopeepay") || method.contains("wallet")) {
                return "DIGITAL_WALLET";
            } else if (method.contains("bank") || method.contains("transfer")) {
                return "BANK_TRANSFER";
            } else if (method.contains("credit") || method.contains("debit")) {
                return "CARD_PAYMENT";
            } else if (method.contains("installment")) {
                return "INSTALLMENT";
            } else {
                return "OTHER";
            }
        }

        private String detectPaymentProvider(String paymentMethod) {
            if (paymentMethod == null) {
                return "UNKNOWN";
            }

            String method = paymentMethod.toLowerCase();
            if (method.contains("shopeepay")) {
                return "SHOPEEPAY";
            } else if (method.contains("momo")) {
                return "MOMO";
            } else if (method.contains("zalopay")) {
                return "ZALOPAY";
            } else if (method.contains("vnpay")) {
                return "VNPAY";
            } else if (method.contains("visa")) {
                return "VISA";
            } else if (method.contains("mastercard")) {
                return "MASTERCARD";
            } else if (method.contains("bank")) {
                return "BANK";
            } else {
                return "OTHER";
            }
        }

        // ===== LEGACY METHODS (for backward compatibility) =====

        /**
         * Legacy method for processing orders by date
         */
        public EtlResult processOrdersForDate(String dateString) {
            log.info("Starting Shopee ETL process for date: {}", dateString);

            long startTime = System.currentTimeMillis();
            int totalOrders = 0;
            int ordersProcessed = 0;
            List<FailedOrder> failedOrders = new ArrayList<>();

            try {
                // Fetch orders for specific date from API
                ShopeeApiResponse response = shopeeApiService.fetchOrdersForDate(dateString);

                if (response == null || response.getData() == null) {
                    log.warn("No Shopee data received for date: {}", dateString);

                    // Create EtlResult using constructor
                    EtlResult result = new EtlResult();
                    result.success = true;
                    result.totalOrders = 0;
                    result.ordersProcessed = 0;
                    result.durationMs = System.currentTimeMillis() - startTime;
                    result.errorMessage = "No data available for the specified date";
                    result.failedOrders = failedOrders;
                    return result;
                }

                List<ShopeeOrderDto> orders = response.getData().getOrders();
                totalOrders = orders.size();

                log.info("Processing {} Shopee orders for date: {}", totalOrders, dateString);

                // Process each order
                for (ShopeeOrderDto orderDto : orders) {
                    try {
                        processOrderUpsert(orderDto);
                        ordersProcessed++;

                        if (ordersProcessed % 10 == 0) {
                            log.info("Processed {} / {} Shopee orders for date: {}",
                                    ordersProcessed, totalOrders, dateString);
                        }

                    } catch (Exception e) {
                        log.error("Failed to process Shopee order {} for date {}: {}",
                                orderDto.getOrderId(), dateString, e.getMessage());

                        failedOrders.add(new FailedOrder(orderDto.getOrderId(), e.getMessage()));
                    }
                }

                long durationMs = System.currentTimeMillis() - startTime;
                double successRate = totalOrders > 0 ? (double) ordersProcessed / totalOrders * 100 : 0;

                log.info("Completed Shopee ETL for date: {} - {}/{} orders processed ({}%) in {} ms",
                        dateString, ordersProcessed, totalOrders, String.format("%.1f", successRate), durationMs);

                // Create success result
                EtlResult result = new EtlResult();
                result.success = (ordersProcessed > 0 || totalOrders == 0);
                result.totalOrders = totalOrders;
                result.ordersProcessed = ordersProcessed;
                result.durationMs = durationMs;
                result.errorMessage = failedOrders.isEmpty() ? null :
                        String.format("%d orders failed processing", failedOrders.size());
                result.failedOrders = failedOrders;
                return result;

            } catch (Exception e) {
                long durationMs = System.currentTimeMillis() - startTime;
                log.error("Shopee ETL failed for date {}: {}", dateString, e.getMessage());

                // Create failure result
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

        // Thêm vào cuối section UTILITY METHODS

        private String generateProviderId(String providerName) {
            if (providerName == null) return "UNKNOWN";

            return providerName.toUpperCase()
                    .replaceAll("[^A-Z0-9]", "_") // Replace non-alphanumeric with underscore
                    .replaceAll("_{2,}", "_") // Replace multiple underscores with single
                    .replaceAll("^_|_$", ""); // Remove leading/trailing underscores
        }

        private String classifyProviderType(String providerName) {
            if (providerName == null) return "UNKNOWN";

            String name = providerName.toLowerCase();
            if (name.contains("shopee")) {
                return "PLATFORM_OWNED";
            } else if (name.contains("spx") || name.contains("j&t") || name.contains("ghn") ||
                    name.contains("best") || name.contains("viettel")) {
                return "THIRD_PARTY";
            } else if (name.contains("post") || name.contains("vnpost")) {
                return "POSTAL_SERVICE";
            } else {
                return "THIRD_PARTY"; // Default
            }
        }

        private String classifyProviderTier(String providerName) {
            if (providerName == null) return "STANDARD";

            String name = providerName.toLowerCase();
            if (name.contains("express") || name.contains("fast") || name.contains("speed")) {
                return "PREMIUM";
            } else if (name.contains("shopee") || name.contains("spx") || name.contains("j&t")) {
                return "STANDARD";
            } else if (name.contains("post")) {
                return "ECONOMY";
            } else {
                return "STANDARD"; // Default
            }
        }

        private String classifyServiceTier(Integer daysToShip) {
            if (daysToShip == null) return "STANDARD";

            if (daysToShip == 0) {
                return "SAME_DAY";
            } else if (daysToShip == 1) {
                return "NEXT_DAY";
            } else if (daysToShip <= 2) {
                return "EXPRESS";
            } else if (daysToShip <= 5) {
                return "STANDARD";
            } else {
                return "ECONOMY";
            }
        }

        private String generateDeliveryCommitment(Integer daysToShip) {
            if (daysToShip == null) return "STANDARD_DELIVERY";

            if (daysToShip == 0) {
                return "SAME_DAY_DELIVERY";
            } else if (daysToShip == 1) {
                return "NEXT_DAY_DELIVERY";
            } else {
                return daysToShip + "_DAY_DELIVERY";
            }
        }

        // Thêm vào cuối section UTILITY METHODS

        private String mapShopeeStatusToStandard(String shopeeStatus) {
            if (shopeeStatus == null) return "UNKNOWN";

            switch (shopeeStatus.toUpperCase()) {
                case "COMPLETED":
                    return "DELIVERED";
                case "CANCELLED":
                    return "CANCELLED";
                case "UNPAID":
                    return "PENDING_PAYMENT";
                case "TO_PROCESS":
                    return "PROCESSING";
                case "PROCESSED":
                    return "CONFIRMED";
                case "TO_SHIP":
                    return "READY_TO_SHIP";
                case "SHIPPED":
                    return "IN_TRANSIT";
                case "TO_RECEIVE":
                    return "OUT_FOR_DELIVERY";
                case "IN_CANCEL":
                    return "CANCELLATION_PENDING";
                case "INVOICE_PENDING":
                    return "PENDING_INVOICE";
                default:
                    return shopeeStatus; // Keep original if no mapping found
            }
        }

        private String determineStatusCategory(String standardStatus) {
            if (standardStatus == null) return "UNKNOWN";

            String status = standardStatus.toUpperCase();

            if (status.contains("PENDING") || status.contains("PROCESSING") || status.contains("CONFIRMED")) {
                return "PROCESSING";
            } else if (status.contains("SHIP") || status.contains("TRANSIT") || status.contains("DELIVERY")) {
                return "FULFILLMENT";
            } else if (status.equals("DELIVERED")) {
                return "COMPLETED";
            } else if (status.contains("CANCEL")) {
                return "CANCELLED";
            } else if (status.contains("RETURN") || status.contains("REFUND")) {
                return "RETURNED";
            } else {
                return "OTHER";
            }
        }

        // Thêm vào cuối section UTILITY METHODS

        private Integer generateDateKey(LocalDate date) {
            if (date == null) return null;

            // Generate date key in format YYYYMMDD
            return date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth();
        }

        private Integer calculateDurationFromPrevious(String orderId, LocalDateTime currentTransition) {
            try {
                // Find the most recent previous transition for this order
                List<OrderStatus> previousTransitions = orderStatusRepository.findByOrderIdOrderByTransitionTimestampDesc(orderId);

                if (previousTransitions.isEmpty()) {
                    return 0; // First transition, no previous duration
                }

                // Get the most recent transition (excluding current one being processed)
                OrderStatus previousTransition = previousTransitions.get(0);
                LocalDateTime previousTime = previousTransition.getTransitionTimestamp();

                if (previousTime == null) {
                    return 0; // No previous timestamp available
                }

                // Calculate duration in hours - direct calculation
                long durationHours = java.time.Duration.between(previousTime, currentTransition).toHours();
                return Math.max(0, (int) durationHours); // Ensure non-negative

            } catch (Exception e) {
                log.warn("Failed to calculate duration from previous status for order {}: {}", orderId, e.getMessage());
                return 0; // Default to 0 if calculation fails
            }
        }

        // Thêm vào cuối section UTILITY METHODS

        private boolean determineIfActiveOrder(String status) {
            return !("COMPLETED".equals(status) || "CANCELLED".equals(status));
        }

        private boolean determineIfCompletedOrder(String status) {
            return "COMPLETED".equals(status);
        }

        private boolean determineIfRevenueRecognized(String status) {
            return "COMPLETED".equals(status); // Revenue recognized only when completed
        }

        private boolean determineIfRefundable(String status) {
            return "COMPLETED".equals(status) || "CANCELLED".equals(status);
        }

        private boolean determineIfCancellable(String status) {
            return !("COMPLETED".equals(status) || "CANCELLED".equals(status) || "SHIPPED".equals(status));
        }

        private boolean determineIfTrackable(String status) {
            return "SHIPPED".equals(status) || "TO_RECEIVE".equals(status);
        }

        private boolean determineIfRequiresManualAction(String status) {
            return "TO_PROCESS".equals(status) || "IN_CANCEL".equals(status);
        }

        private String generateNextPossibleStatuses(String currentStatus) {
            switch (currentStatus) {
                case "UNPAID":
                    return "TO_PROCESS,CANCELLED";
                case "TO_PROCESS":
                    return "PROCESSED,CANCELLED";
                case "PROCESSED":
                    return "TO_SHIP,CANCELLED";
                case "TO_SHIP":
                    return "SHIPPED,CANCELLED";
                case "SHIPPED":
                    return "TO_RECEIVE,CANCELLED";
                case "TO_RECEIVE":
                    return "COMPLETED";
                case "COMPLETED":
                    return ""; // Final state
                case "CANCELLED":
                    return ""; // Final state
                default:
                    return "";
            }
        }

        private Integer determineAutoTransitionHours(String status) {
            switch (status) {
                case "UNPAID":
                    return 24; // 24 hours to pay
                case "TO_PROCESS":
                    return 12; // 12 hours to process
                case "PROCESSED":
                    return 24; // 24 hours to ship
                case "TO_SHIP":
                    return 48; // 48 hours shipping window
                case "SHIPPED":
                    return 72; // 72 hours for delivery
                case "TO_RECEIVE":
                    return 168; // 7 days to confirm receipt
                default:
                    return 0; // No auto transition
            }
        }

        private String determineStatusColor(String status) {
            switch (status) {
                case "COMPLETED":
                    return "GREEN";
                case "CANCELLED":
                    return "RED";
                case "SHIPPED":
                case "TO_RECEIVE":
                    return "BLUE";
                case "TO_PROCESS":
                case "PROCESSED":
                case "TO_SHIP":
                    return "ORANGE";
                case "UNPAID":
                    return "YELLOW";
                default:
                    return "GRAY";
            }
        }

        private String determineStatusIcon(String status) {
            switch (status) {
                case "COMPLETED":
                    return "check-circle";
                case "CANCELLED":
                    return "x-circle";
                case "SHIPPED":
                    return "truck";
                case "TO_RECEIVE":
                    return "package";
                case "TO_PROCESS":
                    return "clock";
                case "PROCESSED":
                    return "check";
                case "TO_SHIP":
                    return "box";
                case "UNPAID":
                    return "credit-card";
                default:
                    return "help-circle";
            }
        }

        private String generateCustomerDescription(String status) {
            switch (status) {
                case "UNPAID":
                    return "Chờ thanh toán";
                case "TO_PROCESS":
                    return "Đang xử lý đơn hàng";
                case "PROCESSED":
                    return "Đơn hàng đã được xác nhận";
                case "TO_SHIP":
                    return "Chuẩn bị giao hàng";
                case "SHIPPED":
                    return "Đang giao hàng";
                case "TO_RECEIVE":
                    return "Chờ xác nhận nhận hàng";
                case "COMPLETED":
                    return "Đơn hàng hoàn thành";
                case "CANCELLED":
                    return "Đơn hàng đã hủy";
                default:
                    return "Trạng thái không xác định";
            }
        }

        private Double getDefaultAverageDuration(String status) {
            switch (status) {
                case "UNPAID":
                    return 12.0; // 12 hours average
                case "TO_PROCESS":
                    return 6.0; // 6 hours average
                case "PROCESSED":
                    return 18.0; // 18 hours average
                case "TO_SHIP":
                    return 24.0; // 24 hours average
                case "SHIPPED":
                    return 48.0; // 48 hours average
                case "TO_RECEIVE":
                    return 72.0; // 72 hours average
                default:
                    return 24.0; // Default 24 hours
            }
        }

        private Double getDefaultSuccessRate(String status) {
            switch (status) {
                case "COMPLETED":
                    return 1.0; // 100% success
                case "CANCELLED":
                    return 0.0; // 0% success
                case "SHIPPED":
                case "TO_RECEIVE":
                    return 0.95; // 95% success rate
                case "TO_PROCESS":
                case "PROCESSED":
                case "TO_SHIP":
                    return 0.90; // 90% success rate
                case "UNPAID":
                    return 0.75; // 75% success rate
                default:
                    return 0.85; // Default 85% success rate
            }
        }

        // ===== ETL RESULT CLASSES =====

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
            public void addFailedOrder(String orderId, String error) {
                this.failedOrders.add(new FailedOrder(orderId, error));
            }

            public double getSuccessRate() {
                if (totalOrders == 0) return 0.0;
                return (double) ordersProcessed / totalOrders * 100.0;
            }
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