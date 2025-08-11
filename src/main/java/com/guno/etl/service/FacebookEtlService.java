// FacebookEtlService.java - Complete Clean Implementation
// Copy EXACT structure từ ShopeeEtlService và TikTokEtlService đã chạy ổn định
package com.guno.etl.service;

import com.guno.etl.dto.*;
import com.guno.etl.entity.*;
import com.guno.etl.repository.*;
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

    // ===== EXACT SAME REPOSITORIES AS SHOPEE/TIKTOK =====
    @Autowired private FacebookApiService facebookApiService;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private GeographyInfoRepository geographyInfoRepository;
    @Autowired private ProcessingDateInfoRepository processingDateInfoRepository;
    @Autowired private PaymentInfoRepository paymentInfoRepository;
    @Autowired private ShippingInfoRepository shippingInfoRepository;
    @Autowired private StatusRepository statusRepository;
    @Autowired private OrderStatusRepository orderStatusRepository;
    @Autowired private OrderStatusDetailRepository orderStatusDetailRepository;

    @Value("${etl.platforms.facebook.name:FACEBOOK}")
    private String platformName;

    // ===== EXACT SAME MAIN ETL FLOW AS SHOPEE/TIKTOK =====

    @Transactional
    public EtlResult processUpdatedOrders() {
        log.info("🚀 Starting Facebook ETL process...");

        EtlResult result = new EtlResult();
        List<FailedOrder> failedOrders = new ArrayList<>();

        try {
            FacebookApiResponse response = facebookApiService.fetchUpdatedOrders();

            if (response == null || response.getData() == null || response.getData().getOrders() == null) {
                log.warn("⚠️ No Facebook orders found to process");
                return result.success(0, failedOrders);
            }

            List<FacebookOrderDto> orders = response.getData().getOrders();
            log.info("📦 Processing {} Facebook orders", orders.size());

            for (FacebookOrderDto orderDto : orders) {
                try {
                    processOrderUpsert(orderDto);
                    result.incrementProcessed();
                } catch (Exception e) {
                    log.error("❌ Failed to process Facebook order {}: {}", orderDto.getOrderId(), e.getMessage());
                    failedOrders.add(new FailedOrder(orderDto.getOrderId(), e.getMessage()));
                    result.incrementFailed();
                }
            }

            log.info("✅ Facebook ETL completed - Processed: {}, Failed: {}",
                    result.getOrdersProcessed(), result.getOrdersFailed());

            return result.success(result.getOrdersProcessed(), failedOrders);

        } catch (Exception e) {
            log.error("💥 Facebook ETL process failed: {}", e.getMessage());
            return result.failure(e.getMessage(), failedOrders);
        }
    }

    // ===== EXACT SAME 9-TABLE PROCESSING ORDER AS SHOPEE/TIKTOK =====

    private void processOrderUpsert(FacebookOrderDto orderDto) {
        String orderId = orderDto.getOrderId();
        log.debug("Processing Facebook order: {}", orderId);

        // EXACT SAME TABLE PROCESSING ORDER
        try { processCustomerUpsert(orderDto); } catch (Exception e) { log.error("Customer upsert failed for order {}", orderId, e); }
        try { processOrderEntityUpsert(orderDto); } catch (Exception e) { log.error("Order upsert failed for order {}", orderId, e); }
        try { processProductsUpsert(orderDto); } catch (Exception e) { log.error("Products upsert failed for order {}", orderId, e); }
        try { processOrderItemsUpsert(orderDto); } catch (Exception e) { log.error("Order items upsert failed for order {}", orderId, e); }
        try { processGeographyUpsert(orderDto); } catch (Exception e) { log.error("Geography upsert failed for order {}", orderId, e); }
        try { processDateInfoUpsert(orderDto); } catch (Exception e) { log.error("Date info upsert failed for order {}", orderId, e); }
        try { processPaymentInfoUpsert(orderDto); } catch (Exception e) { log.error("Payment info upsert failed for order {}", orderId, e); }
        try { processShippingInfoUpsert(orderDto); } catch (Exception e) { log.error("Shipping info upsert failed for order {}", orderId, e); }
        try { processStatusInfoUpsert(orderDto); } catch (Exception e) { log.error("Status info upsert failed for order {}", orderId, e); }
    }

    // ===== CUSTOMER PROCESSING - SAME PATTERN AS SHOPEE/TIKTOK =====

    private void processCustomerUpsert(FacebookOrderDto orderDto) {
        try {
            FacebookCustomerDto customerDto = orderDto.getData().getCustomer();
            if (customerDto == null) {
                log.warn("No customer data in Facebook order {}", orderDto.getOrderId());
                return;
            }

            // Use Facebook customer ID directly (not generated like Shopee/TikTok)
            String customerId = customerDto.getId();
            if (customerId == null || customerId.trim().isEmpty()) {
                log.warn("No customer ID in Facebook order {}", orderDto.getOrderId());
                return;
            }

            Optional<Customer> existingCustomer = customerRepository.findById(customerId);

            if (existingCustomer.isPresent()) {
                // UPDATE existing customer - same pattern as Shopee/TikTok
                Customer customer = existingCustomer.get();
                // Update minimal fields từ Facebook API
                if (customerDto.getTotalOrders() != null) {
                    customer.setTotalOrders(customerDto.getTotalOrders());
                }
                if (customerDto.getPurchasedAmount() != null) {
                    customer.setTotalSpent(customerDto.getPurchasedAmount().doubleValue());
                }
                customerRepository.save(customer);
                log.debug("Facebook customer {} updated", customerId);
            } else {
                // INSERT new customer - same pattern as Shopee/TikTok
                Customer newCustomer = createCustomerFromFacebookOrder(orderDto, customerId);
                customerRepository.save(newCustomer);
                log.debug("Facebook customer {} created", customerId);
            }

        } catch (Exception e) {
            log.error("Error processing Facebook customer for order {}: {}", orderDto.getOrderId(), e.getMessage());
        }
    }

    private Customer createCustomerFromFacebookOrder(FacebookOrderDto orderDto, String customerId) {
        FacebookCustomerDto customerDto = orderDto.getData().getCustomer();

        // Use consistent timestamp
        LocalDateTime currentTime = LocalDateTime.now();

        return Customer.builder()
                .customerId(customerId)
                .customerKey(System.currentTimeMillis() % 1000000000L) // Positive number
                .platformCustomerId(customerDto.getFbId())
                .phoneHash(extractPhoneHash(customerDto))
                .emailHash(extractEmailHash(customerDto))
                .gender(null)
                .ageGroup(null)
                .customerSegment("REGULAR")
                .customerTier("STANDARD")
                .acquisitionChannel("FACEBOOK")
                .firstOrderDate(currentTime)
                .lastOrderDate(currentTime)
                .totalOrders(customerDto.getTotalOrders() != null ? customerDto.getTotalOrders() : 1)
                .totalSpent(customerDto.getPurchasedAmount() != null ? customerDto.getPurchasedAmount().doubleValue() : 0.0)
                .averageOrderValue(customerDto.getAverageOrderValue() != null ? customerDto.getAverageOrderValue() : 0.0)
                .totalItemsPurchased(1)
                .daysSinceFirstOrder(0)
                .daysSinceLastOrder(0)
                .purchaseFrequencyDays(0.0)
                .returnRate(0.0)
                .cancellationRate(0.0)
                .codPreferenceRate(1.0)
                .favoriteCategory(null)
                .favoriteBrand(null)
                .preferredPaymentMethod("COD")
                .preferredPlatform("FACEBOOK")
                .primaryShippingProvince("Hồ Chí Minh")
                .shipsToMultipleProvinces(false)
                .loyaltyPoints(0)
                .referralCount(0)
                .isReferrer(false)
                .build();
    }

    private String extractPhoneHash(FacebookCustomerDto customerDto) {
        try {
            if (customerDto.getPhoneNumbers() != null && !customerDto.getPhoneNumbers().isEmpty()) {
                String phone = customerDto.getPhoneNumbers().get(0);
                return "FB_PHONE_" + Math.abs(phone.hashCode());
            }
            return "FB_NO_PHONE_" + System.currentTimeMillis();
        } catch (Exception e) {
            return "FB_PHONE_ERROR_" + System.currentTimeMillis();
        }
    }

    private String extractEmailHash(FacebookCustomerDto customerDto) {
        try {
            if (customerDto.getEmails() != null && !customerDto.getEmails().isEmpty()) {
                String email = customerDto.getEmails().get(0);
                return "FB_EMAIL_" + Math.abs(email.hashCode());
            }
            return "FB_NO_EMAIL_" + System.currentTimeMillis();
        } catch (Exception e) {
            return "FB_EMAIL_ERROR_" + System.currentTimeMillis();
        }
    }

    // ===== ORDER PROCESSING - SAME PATTERN AS SHOPEE/TIKTOK =====

    private void processOrderEntityUpsert(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();

            Optional<Order> existingOrder = orderRepository.findById(orderId);

            if (existingOrder.isPresent()) {
                // UPDATE existing order - same pattern as Shopee/TikTok
                Order order = existingOrder.get();
                updateOrderFromFacebook(order, orderDto);
                orderRepository.save(order);
                log.debug("Facebook order {} updated", orderId);
            } else {
                // INSERT new order - same pattern as Shopee/TikTok
                Order newOrder = createOrderFromFacebook(orderDto);
                orderRepository.save(newOrder);
                log.debug("Facebook order {} created", orderId);
            }

        } catch (Exception e) {
            log.error("Error processing Facebook order entity for {}: {}", orderDto.getOrderId(), e.getMessage());
        }
    }

    private Order createOrderFromFacebook(FacebookOrderDto orderDto) {
        String customerId = orderDto.getData().getCustomer() != null ?
                orderDto.getData().getCustomer().getId() :
                "FB_UNKNOWN_" + orderDto.getOrderId();

        FacebookOrderDto.FacebookOrderData data = orderDto.getData();

        return Order.builder()
                .orderId(orderDto.getOrderId())
                .customerId(customerId)
                .shopId(data.getPage() != null ? data.getPage().getId() : null)

                // ✅ DIRECT mapping từ Facebook API - KHÔNG dùng công thức
                .grossRevenue(data.getCod() != null ? data.getCod().doubleValue() : null)
                .netRevenue(data.getCash() != null ? data.getCash().doubleValue() : null)
                .codAmount(data.getCod() != null ? data.getCod().doubleValue() : null)
                .platformFee(null) // Facebook không cung cấp
                .shippingFee(data.getSurcharge() != null ? data.getSurcharge().doubleValue() : null)
                .taxAmount(data.getTax() != null ? data.getTax().doubleValue() : null)
                .sellerDiscount(null) // Facebook không cung cấp
                .platformDiscount(null) // Facebook không cung cấp

                // ✅ Boolean logic từ Facebook API
                .isDelivered(orderDto.getStatus() != null && orderDto.getStatus() == 2)
                .isCancelled(orderDto.getStatus() != null && orderDto.getStatus() == 9)
                .isReturned(false) // Facebook không có return status
                .isCod(data.getCod() != null && data.getCod() > 0)
                .isNewCustomer(null) // Sẽ được calculate ở customer logic

                // ✅ Default values chỉ khi cần thiết
                .orderToShipHours(null) // Facebook không cung cấp
                .shipToDeliveryHours(null) // Facebook không cung cấp
                .totalFulfillmentHours(null) // Facebook không cung cấp
                .customerOrderSequence(null) // Sẽ được calculate
                .customerLifetimeOrders(null) // Sẽ được calculate
                .customerLifetimeValue(null) // Sẽ được calculate
                .daysSinceLastOrder(null) // Sẽ được calculate
                .promotionImpact(null) // Facebook không cung cấp
                .adRevenue(null) // Facebook không cung cấp
                .organicRevenue(data.getCod() != null ? data.getCod().doubleValue() : null)
                .aov(null) // Sẽ được calculate
                .shippingCostRatio(null) // Sẽ được calculate
                .createdAt(LocalDateTime.now())
                .rawData(null) // Facebook không cung cấp
                .platformSpecificData(null) // Facebook không cung cấp
                .build();
    }

    private void updateOrderFromFacebook(Order order, FacebookOrderDto orderDto) {
        // Update basic fields from Facebook API
        if (orderDto.getStatus() != null) {
            order.setIsDelivered(orderDto.getStatus() == 2);
            order.setIsCancelled(orderDto.getStatus() == 9);
        }

        if (orderDto.getData().getCod() != null) {
            Double codAmount = orderDto.getData().getCod().doubleValue();
            order.setCodAmount(codAmount);
            order.setGrossRevenue(codAmount);
            order.setNetRevenue(codAmount);
        }
    }

    // ===== PRODUCTS PROCESSING - SAME PATTERN AS SHOPEE/TIKTOK =====

    private void processProductsUpsert(FacebookOrderDto orderDto) {
        try {
            if (orderDto.getData() == null || orderDto.getData().getItems() == null) {
                return;
            }

            for (FacebookItemDto item : orderDto.getData().getItems()) {
                processProductFromFacebookItem(item);
            }

        } catch (Exception e) {
            log.error("Error processing Facebook products for order {}: {}", orderDto.getOrderId(), e.getMessage());
        }
    }

    private void processProductFromFacebookItem(FacebookItemDto item) {
        try {
            String sku = extractSkuFromFacebookItem(item);
            String platformProductId = item.getProductId();

            List<Product> existingProducts = productRepository.findBySku(sku);
            Optional<Product> existingProduct = existingProducts.stream()
                    .filter(p -> platformName.equals(p.getPlatformProductId()))
                    .findFirst();

            if (existingProduct.isPresent()) {
                // UPDATE existing product
                Product product = existingProduct.get();
                updateProductFromFacebookItem(product, item);
                productRepository.save(product);
                log.debug("Facebook product {} updated", sku);
            } else {
                // INSERT new product
                Product newProduct = createProductFromFacebookItem(item, sku);
                productRepository.save(newProduct);
                log.debug("Facebook product {} created", sku);
            }

        } catch (Exception e) {
            log.error("Error processing Facebook product: {}", e.getMessage());
        }
    }

    private String extractSkuFromFacebookItem(FacebookItemDto item) {
        if (item.getVariationId() != null) {
            return "FB_VAR_" + item.getVariationId();
        }
        if (item.getProductId() != null) {
            return "FB_PROD_" + item.getProductId();
        }
        return "FB_ITEM_" + System.currentTimeMillis();
    }

    private Product createProductFromFacebookItem(FacebookItemDto item, String sku) {
        return Product.builder()
                .sku(sku)
                .platformProductId(platformName)
                .productId(item.getProductId())
                .variationId(item.getVariationId())
                .barcode(null) // Facebook không cung cấp
                .productName(item.getProductName())
                .productDescription(item.getProductDescription())
                .brand(null) // Facebook không cung cấp
                .model(null) // Facebook không cung cấp
                .categoryLevel1(null) // Facebook không cung cấp
                .categoryLevel2(null) // Facebook không cung cấp
                .categoryLevel3(null) // Facebook không cung cấp
                .categoryPath(null) // Facebook không cung cấp
                .color(null) // Facebook không cung cấp
                .size(null) // Facebook không cung cấp
                .material(null) // Facebook không cung cấp
                .weightGram(null) // Facebook không cung cấp - ĐÚNG FIELD NAME
                .dimensions(null) // Facebook không cung cấp
                .costPrice(item.getWholesalePrice() != null ? item.getWholesalePrice().doubleValue() : null)
                .retailPrice(item.getRetailPrice() != null ? item.getRetailPrice().doubleValue() : null)
                .originalPrice(item.getProductPrice() != null ? item.getProductPrice().doubleValue() : null)
                .priceRange(null) // Facebook không cung cấp
                .isActive(item.getIsActive() != null ? item.getIsActive() : null)
                .isFeatured(null) // Facebook không cung cấp
                .isSeasonal(null) // Facebook không cung cấp
                .isNewArrival(null) // Facebook không cung cấp
                .isBestSeller(null) // Facebook không cung cấp
                .primaryImageUrl(item.getProductImage())
                .imageCount(item.getProductImage() != null ? 1 : null)
                .seoTitle(item.getProductName()) // Fallback to product name
                .seoKeywords(null) // Facebook không cung cấp
                .build();
    }

    private void updateProductFromFacebookItem(Product product, FacebookItemDto item) {
        // Update pricing from Facebook
        if (item.getRetailPrice() != null) {
            product.setRetailPrice(item.getRetailPrice().doubleValue());
        }
        if (item.getWholesalePrice() != null) {
            product.setCostPrice(item.getWholesalePrice().doubleValue());
        }
        if (item.getIsActive() != null) {
            product.setIsActive(item.getIsActive());
        }
    }

    // ===== ORDER ITEMS PROCESSING - SAME PATTERN AS SHOPEE/TIKTOK =====

    private void processOrderItemsUpsert(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();

            // DELETE existing items first (same as Shopee/TikTok)
            List<OrderItem> existingItems = orderItemRepository.findByOrderIdOrderByItemSequence(orderId);
            if (!existingItems.isEmpty()) {
                orderItemRepository.deleteAll(existingItems);
                log.debug("Deleted {} existing items for order: {}", existingItems.size(), orderId);
            }

            // INSERT new items
            if (orderDto.getData() != null && orderDto.getData().getItems() != null) {
                for (int i = 0; i < orderDto.getData().getItems().size(); i++) {
                    FacebookItemDto item = orderDto.getData().getItems().get(i);
                    OrderItem orderItem = createOrderItemFromFacebookItem(orderDto, item, i + 1);
                    orderItemRepository.save(orderItem);
                    log.debug("Created order item for order: {} - sequence: {}", orderId, i + 1);
                }
            }

        } catch (Exception e) {
            log.error("Error processing Facebook order items for {}: {}", orderDto.getOrderId(), e.getMessage());
        }
    }

    private OrderItem createOrderItemFromFacebookItem(FacebookOrderDto orderDto, FacebookItemDto item, int sequence) {
        String sku = extractSkuFromFacebookItem(item);

        return OrderItem.builder()
                .orderId(orderDto.getOrderId())
                .sku(sku)
                .platformProductId(platformName)
                .quantity(item.getQuantity() != null ? item.getQuantity() : 1)
                .unitPrice(item.getRetailPrice() != null ? item.getRetailPrice().doubleValue() : 0.0)
                .totalPrice(item.getRetailPrice() != null ? item.getRetailPrice().doubleValue() : 0.0)
                .itemDiscount(0.0)
                .itemSequence(sequence)
                .itemStatus("ACTIVE")
                .opId(null)
                .promotionCode(null)
                .promotionType(null)
                .build();
    }

    // ===== GEOGRAPHY PROCESSING - SAME PATTERN AS SHOPEE/TIKTOK =====

    private void processGeographyUpsert(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();

            Optional<GeographyInfo> existingGeography = geographyInfoRepository.findById(orderId);

            if (existingGeography.isPresent()) {
                log.debug("Geography info already exists for order {}", orderId);
                return; // Geography rarely changes
            } else {
                GeographyInfo newGeography = createGeographyInfoFromFacebook(orderDto);
                geographyInfoRepository.save(newGeography);
                log.debug("Geography info created for order: {}", orderId);
            }

        } catch (Exception e) {
            log.error("Error processing Facebook geography for {}: {}", orderDto.getOrderId(), e.getMessage());
        }
    }

    private GeographyInfo createGeographyInfoFromFacebook(FacebookOrderDto orderDto) {
        FacebookOrderDto.FacebookOrderData data = orderDto.getData();

        // Variables for address components
        String fullAddress = "";
        String recipientName = "";
        String recipientPhone = "";
        String ward = "";
        String district = "";
        String province = "";
        String country = "Vietnam";
        String postalCode = "";
        Double latitude = 0.0;
        Double longitude = 0.0;

        // PRIORITY 1: Use shipping_address if available
        if (data.getShippingAddress() != null) {
            FacebookShippingAddressDto shipping = data.getShippingAddress();

            fullAddress = shipping.getFullAddress() != null ? shipping.getFullAddress() : "";
            recipientName = shipping.getRecipientName() != null ? shipping.getRecipientName() : "";
            recipientPhone = shipping.getRecipientPhone() != null ? shipping.getRecipientPhone() : "";
            ward = shipping.getWard() != null ? shipping.getWard() : "";
            district = shipping.getDistrict() != null ? shipping.getDistrict() : "";
            province = shipping.getProvince() != null ? shipping.getProvince() : "";
            country = shipping.getCountry() != null ? shipping.getCountry() : "Vietnam";
            postalCode = shipping.getPostalCode() != null ? shipping.getPostalCode() : "";
            latitude = shipping.getLatitude() != null ? shipping.getLatitude() : 0.0;
            longitude = shipping.getLongitude() != null ? shipping.getLongitude() : 0.0;
        }
        // PRIORITY 2: Fallback to customer address if no shipping address
        else if (data.getCustomer() != null) {
            FacebookCustomerDto customer = data.getCustomer();

            // Get recipient info from customer
            recipientName = customer.getName() != null ? customer.getName() : "";
            recipientPhone = (customer.getPhoneNumbers() != null && !customer.getPhoneNumbers().isEmpty())
                    ? customer.getPhoneNumbers().get(0) : "";

            // Get address from customer addresses array (shop_customer_addresses)
            if (customer.getShopCustomerAddresses() != null && !customer.getShopCustomerAddresses().isEmpty()) {
                FacebookCustomerDto.FacebookCustomerAddressDto customerAddr = customer.getShopCustomerAddresses().get(0);

                fullAddress = customerAddr.getAddress() != null ? customerAddr.getAddress() : "";
                ward = customerAddr.getWard() != null ? customerAddr.getWard() : "";
                district = customerAddr.getDistrict() != null ? customerAddr.getDistrict() : "";
                province = customerAddr.getProvince() != null ? customerAddr.getProvince() : "";
                country = customerAddr.getCountry() != null ? customerAddr.getCountry() : "Vietnam";
                postalCode = customerAddr.getPostalCode() != null ? customerAddr.getPostalCode() : "";
            }
            // No fallback to customer.getAddress() - field doesn't exist
        }

        return GeographyInfo.builder()
                // Primary key
                .orderId(orderDto.getOrderId())

                // Generated key
                .geographyKey(0L)

                // Country level
                .countryCode("VN")
                .countryName(country)

                // Region level - empty, no data from API
                .regionCode("")
                .regionName("")

                // Province level
                .provinceCode("")  // Facebook doesn't provide codes
                .provinceName(province)
                .provinceType("")

                // District level
                .districtCode("")
                .districtName(district)
                .districtType("")

                // Ward level
                .wardCode("")
                .wardName(ward)
                .wardType("")

                // Boolean flags - all false, no calculation
                .isUrban(false)
                .isMetropolitan(false)
                .isCoastal(false)
                .isBorder(false)

                // Economic data - empty, no calculation
                .economicTier("")
                .populationDensity("")
                .incomeLevel("")

                // Shipping data - empty, no calculation
                .shippingZone("")
                .deliveryComplexity("")

                // Delivery days - default values
                .standardDeliveryDays(3)
                .expressDeliveryAvailable(false)

                // Coordinates from API if available
                .latitude(latitude)
                .longitude(longitude)

                .build();
    }

    // ===== OTHER PROCESSING METHODS - SAME PATTERN AS SHOPEE/TIKTOK =====

    private void processDateInfoUpsert(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();

            Optional<ProcessingDateInfo> existing = processingDateInfoRepository.findById(orderId);

            if (!existing.isPresent()) {
                ProcessingDateInfo dateInfo = createProcessingDateInfoFromFacebook(orderDto);
                processingDateInfoRepository.save(dateInfo);
                log.debug("Date info created for order: {}", orderId);
            }

        } catch (Exception e) {
            log.error("Error processing Facebook date info for {}: {}", orderDto.getOrderId(), e.getMessage());
        }
    }

    private ProcessingDateInfo createProcessingDateInfoFromFacebook(FacebookOrderDto orderDto) {
        LocalDate currentDate = LocalDate.now();

        return ProcessingDateInfo.builder()
                .orderId(orderDto.getOrderId())
                .dateKey(System.currentTimeMillis() % 1000000000L)
                .fullDate(currentDate.atStartOfDay())
                .dayOfWeek(currentDate.getDayOfWeek().getValue())
                .dayOfWeekName(currentDate.getDayOfWeek().name())
                .dayOfMonth(currentDate.getDayOfMonth())
                .dayOfYear(currentDate.getDayOfYear())
                .weekOfYear(currentDate.format(DateTimeFormatter.ofPattern("w")).equals("1") ? 1 :
                        Integer.parseInt(currentDate.format(DateTimeFormatter.ofPattern("w"))))
                .monthOfYear(currentDate.getMonthValue())
                .monthName(currentDate.getMonth().name())
                .quarterOfYear((currentDate.getMonthValue() - 1) / 3 + 1)
                .year(currentDate.getYear())
                .isWeekend(currentDate.getDayOfWeek().getValue() >= 6)
                .isHoliday(false)
                .seasonName("SPRING")
                .build();
    }

    private void processPaymentInfoUpsert(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();

            Optional<PaymentInfo> existing = paymentInfoRepository.findById(orderId);

            if (!existing.isPresent()) {
                PaymentInfo paymentInfo = createPaymentInfoFromFacebook(orderDto);
                paymentInfoRepository.save(paymentInfo);
                log.debug("Payment info created for order: {}", orderId);
            }

        } catch (Exception e) {
            log.error("Error processing Facebook payment info for {}: {}", orderDto.getOrderId(), e.getMessage());
        }
    }

    private PaymentInfo createPaymentInfoFromFacebook(FacebookOrderDto orderDto) {
        return PaymentInfo.builder()
                .orderId(orderDto.getOrderId())
                .paymentKey(System.currentTimeMillis() % 1000000000L)
                .paymentMethod("COD")
                .paymentCategory("CASH_ON_DELIVERY")
                .paymentProvider("FACEBOOK_COD")
                .isCod(true)
                .isPrepaid(false)
                .isInstallment(false)
                .installmentMonths(0)
                .supportsRefund(true)
                .supportsPartialRefund(false)
                .refundProcessingDays(7)
                .riskLevel("LOW")
                .requiresVerification(false)
                .fraudScore(0.1)
                .transactionFeeRate(0.0)
                .processingFee(0.0)
                .paymentProcessingTimeMinutes(0)
                .settlementDays(1)
                .build();
    }

    private void processShippingInfoUpsert(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();

            Optional<ShippingInfo> existing = shippingInfoRepository.findById(orderId);

            if (!existing.isPresent()) {
                ShippingInfo shippingInfo = createShippingInfoFromFacebook(orderDto);
                shippingInfoRepository.save(shippingInfo);
                log.debug("Shipping info created for order: {}", orderId);
            }

        } catch (Exception e) {
            log.error("Error processing Facebook shipping info for {}: {}", orderDto.getOrderId(), e.getMessage());
        }
    }

    private ShippingInfo createShippingInfoFromFacebook(FacebookOrderDto orderDto) {
        FacebookOrderDto.FacebookOrderData data = orderDto.getData();

        // Check if shipping address exists to determine delivery type
        boolean hasShippingAddress = data.getShippingAddress() != null;
        String deliveryType = hasShippingAddress ? "HOME_DELIVERY" : "PICKUP";

        // Get shipping fee from API
        Double shippingFee = data.getShippingFee() != null ? data.getShippingFee() : 0.0;

        return ShippingInfo.builder()
                // Primary key
                .orderId(orderDto.getOrderId())

                // Generated key
                .shippingKey(0L)

                // Provider info
                .providerId("")
                .providerName(hasShippingAddress ? "STANDARD_DELIVERY" : "SELF_PICKUP")
                .providerType(deliveryType)
                .providerTier("BASIC")

                // Service info
                .serviceType(deliveryType)
                .serviceTier("STANDARD")
                .deliveryCommitment("3-5_DAYS")
                .shippingMethod(hasShippingAddress ? "MOTORCYCLE" : "PICKUP")
                .pickupType(hasShippingAddress ? "DOOR" : "STORE")
                .deliveryType(deliveryType)

                // Fees - from API
                .baseFee(shippingFee)
                .weightBasedFee(0.0)
                .distanceBasedFee(0.0)
                .codFee(data.getCod() != null && data.getCod() > 0 ? shippingFee * 0.01 : 0.0)
                .insuranceFee(0.0)

                // Support flags
                .supportsCod(true)
                .supportsInsurance(false)
                .supportsFragile(false)
                .supportsRefrigerated(false)
                .providesTracking(hasShippingAddress)
                .providesSmsUpdates(false)

                // Performance metrics - defaults
                .averageDeliveryDays(hasShippingAddress ? 3.0 : 0.0)
                .onTimeDeliveryRate(0.85)
                .successDeliveryRate(0.95)
                .damageRate(0.01)

                // Coverage
                .coverageProvinces("ALL")
                .coverageNationwide(true)
                .coverageInternational(false)

                .build();
    }

    private void processStatusInfoUpsert(FacebookOrderDto orderDto) {
        try {
            Integer facebookStatus = orderDto.getStatus();
            String facebookStatusString = String.valueOf(facebookStatus);

            Optional<Status> existingStatus = statusRepository.findByPlatformAndPlatformStatusCode(
                    platformName, facebookStatusString);

            if (!existingStatus.isPresent()) {
                Status newStatus = createStatusFromFacebook(orderDto);
                statusRepository.save(newStatus);
                log.debug("Facebook status mapping created: {} -> {}", facebookStatusString,
                        mapFacebookStatusToStandard(facebookStatus));
            }

        } catch (Exception e) {
            log.error("Error processing Facebook status info for {}: {}", orderDto.getOrderId(), e.getMessage());
        }
    }

    private Status createStatusFromFacebook(FacebookOrderDto orderDto) {
        Integer facebookStatus = orderDto.getStatus();
        String facebookStatusString = String.valueOf(facebookStatus);
        String standardStatus = mapFacebookStatusToStandard(facebookStatus);
        String statusCategory = determineStatusCategory(standardStatus);

        return Status.builder()
                .platform(platformName)
                .platformStatusCode(facebookStatusString)
                .platformStatusName(mapFacebookStatusToString(facebookStatus))
                .standardStatusCode(standardStatus)
                .standardStatusName(standardStatus)
                .statusCategory(statusCategory)
                .build();
    }

    // ===== FACEBOOK STATUS MAPPING HELPERS =====

    private String mapFacebookStatusToStandard(Integer facebookStatus) {
        if (facebookStatus == null) return "PENDING";

        switch (facebookStatus) {
            case 1: return "PENDING";
            case 2: return "COMPLETED";
            case 3: return "PROCESSING";
            case 9: return "CANCELLED";
            default: return "PENDING";
        }
    }

    private String mapFacebookStatusToString(Integer facebookStatus) {
        if (facebookStatus == null) return "PENDING";

        switch (facebookStatus) {
            case 1: return "PENDING";
            case 2: return "DELIVERED";
            case 3: return "PROCESSING";
            case 9: return "CANCELLED";
            default: return "PENDING";
        }
    }

    private String determineStatusCategory(String standardStatus) {
        if (standardStatus == null) return "OTHER";

        switch (standardStatus.toUpperCase()) {
            case "PENDING": return "INITIAL";
            case "PROCESSING": return "PROCESSING";
            case "COMPLETED":
            case "CANCELLED": return "FINAL";
            default: return "OTHER";
        }
    }

    // ===== RESULT CLASSES - EXACT SAME AS SHOPEE/TIKTOK =====

    public static class EtlResult {
        private boolean success;
        private int ordersProcessed;
        private int ordersFailed;
        private String errorMessage;
        private List<FailedOrder> failedOrders;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public EtlResult() {
            this.startTime = LocalDateTime.now();
            this.ordersProcessed = 0;
            this.ordersFailed = 0;
            this.failedOrders = new ArrayList<>();
        }

        public EtlResult success(int processed, List<FailedOrder> failed) {
            this.success = true;
            this.ordersProcessed = processed;
            this.ordersFailed = failed.size();
            this.failedOrders = failed;
            this.endTime = LocalDateTime.now();
            return this;
        }

        public EtlResult failure(String errorMessage, List<FailedOrder> failed) {
            this.success = false;
            this.errorMessage = errorMessage;
            this.failedOrders = failed;
            this.endTime = LocalDateTime.now();
            return this;
        }

        public void incrementProcessed() {
            this.ordersProcessed++;
        }

        public void incrementFailed() {
            this.ordersFailed++;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public int getOrdersProcessed() { return ordersProcessed; }
        public int getOrdersFailed() { return ordersFailed; }
        public String getErrorMessage() { return errorMessage; }
        public List<FailedOrder> getFailedOrders() { return failedOrders; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
    }

    public static class FailedOrder {
        private String orderId;
        private String errorMessage;
        private LocalDateTime failureTime;

        public FailedOrder(String orderId, String errorMessage) {
            this.orderId = orderId;
            this.errorMessage = errorMessage;
            this.failureTime = LocalDateTime.now();
        }

        // Getters
        public String getOrderId() { return orderId; }
        public String getErrorMessage() { return errorMessage; }
        public LocalDateTime getFailureTime() { return failureTime; }
    }
}