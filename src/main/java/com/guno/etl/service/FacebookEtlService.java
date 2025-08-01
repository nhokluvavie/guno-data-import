// FacebookEtlService.java - Facebook ETL Service
package com.guno.etl.service;

import com.guno.etl.dto.FacebookApiResponse;
import com.guno.etl.dto.FacebookOrderDto;
import com.guno.etl.dto.FacebookItemDto;
import com.guno.etl.dto.FacebookCustomerDto;
import com.guno.etl.dto.FacebookPageDto;
import com.guno.etl.entity.*;
import com.guno.etl.repository.*;
import com.guno.etl.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FacebookEtlService {

    private static final Logger log = LoggerFactory.getLogger(FacebookEtlService.class);

    @Value("${etl.platforms.facebook.name:FACEBOOK}")
    private String platformName;

    @Autowired
    private FacebookApiService facebookApiService;

    // All 11 repositories for 9-table processing
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

    // ===== MAIN ETL METHODS =====

    /**
     * Process updated Facebook orders
     */
    @Transactional
    public EtlResult processUpdatedOrders() {
        log.info("=== Starting Facebook ETL process ===");

        EtlResult result = new EtlResult();
        result.setStartTime(LocalDateTime.now());

        try {
            // Fetch data from Facebook API
            FacebookApiResponse response = facebookApiService.fetchUpdatedOrders();

            if (response == null || response.getStatus() != 1) {
                result.setSuccess(false);
                result.setErrorMessage("Facebook API call failed");
                return result;
            }

            List<FacebookOrderDto> orders = response.getData().getOrders();
            if (orders == null || orders.isEmpty()) {
                log.info("No Facebook orders to process");
                result.setSuccess(true);
                result.setTotalOrders(0);
                return result;
            }

            // Process each order
            int successCount = 0;
            for (FacebookOrderDto orderDto : orders) {
                try {
                    processOrderUpsert(orderDto);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to process Facebook order: {}", orderDto.getOrderId(), e);
                    result.addFailedOrder(orderDto.getOrderId(), e.getMessage());
                }
            }

            result.setSuccess(true);
            result.setTotalOrders(orders.size());
            result.setOrdersProcessed(successCount);
            result.setEndTime(LocalDateTime.now());

            log.info("Facebook ETL completed: {}/{} orders processed", successCount, orders.size());

        } catch (Exception e) {
            log.error("Facebook ETL failed: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        return result;
    }

    /**
     * Process single Facebook order through all 9 tables
     */
    @Transactional
    public void processOrderUpsert(FacebookOrderDto orderDto) {
        String orderId = orderDto.getOrderId();
        log.debug("Processing Facebook order: {}", orderId);

        try {
            // Process all 9 tables (same structure as Shopee/TikTok)
            processCustomerEntityUpsert(orderDto);
            processOrderEntityUpsert(orderDto);
            processOrderItemsUpsert(orderDto);
            processProductsUpsert(orderDto);
            processGeographyInfoUpsert(orderDto);
            processStatusInfoUpsert(orderDto);
            processOrderStatusTransition(orderDto);
            processOrderStatusDetailUpsert(orderDto);
            processPaymentInfoUpsert(orderDto);
            processShippingInfoUpsert(orderDto);
            processDateInfoUpsert(orderDto);

            log.debug("Successfully processed Facebook order: {}", orderId);

        } catch (Exception e) {
            log.error("Failed to process Facebook order {}: {}", orderId, e.getMessage(), e);
            throw e;
        }
    }

    // ===== 9-TABLE PROCESSING METHODS =====

    /**
     * 1. Process Customer entity
     */
    private void processCustomerEntityUpsert(FacebookOrderDto orderDto) {
        try {
            FacebookCustomerDto customerDto = orderDto.getData().getCustomer();
            if (customerDto == null) return;

            // Generate customer ID from phone or fb_id
            String customerId = generateCustomerId(customerDto);

            Optional<Customer> existingCustomer = customerRepository.findById(customerId);

            if (existingCustomer.isPresent()) {
                updateExistingCustomer(existingCustomer.get(), customerDto, orderDto);
            } else {
                createNewCustomer(customerDto, orderDto, customerId);
            }

        } catch (Exception e) {
            log.error("Failed to process customer for Facebook order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    /**
     * 2. Process Order entity
     */
    private void processOrderEntityUpsert(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();

            Optional<Order> existingOrder = orderRepository.findById(orderId);

            if (existingOrder.isPresent()) {
                updateExistingOrder(existingOrder.get(), orderDto);
            } else {
                createNewOrder(orderDto);
            }

        } catch (Exception e) {
            log.error("Failed to process order entity for Facebook order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    /**
     * 3. Process Order Items
     */
    private void processOrderItemsUpsert(FacebookOrderDto orderDto) {
        try {
            List<FacebookItemDto> items = orderDto.getData().getItems();
            if (items == null || items.isEmpty()) return;

            for (int i = 0; i < items.size(); i++) {
                FacebookItemDto item = items.get(i);

                OrderItem orderItem = OrderItem.builder()
                        .orderId(orderDto.getOrderId())
                        .itemSequence(i + 1)
                        .platformProductId(platformName)
                        .sku(extractSku(item))
                        .quantity(item.getQuantity())
                        .unitPrice(item.getSalePrice() != null ? item.getSalePrice().doubleValue() : 0.0)
                        .totalPrice(calculateTotalPrice(item))
                        .itemDiscount(calculateItemDiscount(item))
                        .itemStatus(mapFacebookStatusToString(orderDto.getStatus()))
                        .build();

                orderItemRepository.save(orderItem);
            }

        } catch (Exception e) {
            log.error("Failed to process order items for Facebook order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    /**
     * 4. Process Products
     */
    private void processProductsUpsert(FacebookOrderDto orderDto) {
        try {
            List<FacebookItemDto> items = orderDto.getData().getItems();
            if (items == null || items.isEmpty()) return;

            for (FacebookItemDto item : items) {
                String sku = extractSku(item);

                List<Product> existingProducts = productRepository.findBySku(sku);
                Optional<Product> existingProduct = existingProducts.stream()
                        .filter(p -> platformName.equals(p.getPlatformProductId()))
                        .findFirst();

                if (existingProduct.isPresent()) {
                    updateExistingProduct(existingProduct.get(), item);
                } else {
                    createNewProduct(item, orderDto);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process products for Facebook order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    /**
     * 5. Process Geography Info
     */
    private void processGeographyInfoUpsert(FacebookOrderDto orderDto) {
        try {
            FacebookCustomerDto customer = orderDto.getData().getCustomer();

            if (customer != null && customer.getShopCustomerAddresses() != null
                    && !customer.getShopCustomerAddresses().isEmpty()) {

                // Use customer address as shipping address
                var customerAddress = customer.getShopCustomerAddresses().get(0);

                GeographyInfo geography = GeographyInfo.builder()
                        .orderId(orderDto.getOrderId())
                        .recipientName(customer.getName())
                        .recipientPhone(getFirstPhoneNumber(customer.getPhoneNumbers()))
                        .fullAddress(customerAddress.getAddress())
                        .province(customerAddress.getProvince())
                        .district(customerAddress.getDistrict())
                        .ward(customerAddress.getWard())
                        .countryCode("VN")
                        .regionType(determineRegionType(customerAddress.getProvince()))
                        .economicTier(determineEconomicTier(customerAddress.getProvince()))
                        .isUrbanArea(isUrbanArea(customerAddress.getProvince()))
                        .build();

                geographyInfoRepository.save(geography);
            } else {
                // Create minimal geography record
                createMinimalGeographyInfo(orderDto);
            }

        } catch (Exception e) {
            log.error("Failed to process geography info for Facebook order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    /**
     * 6. Process Status Info
     */
    private void processStatusInfoUpsert(FacebookOrderDto orderDto) {
        try {
            Integer facebookStatus = orderDto.getStatus();
            String standardStatus = mapFacebookStatusToString(facebookStatus);

            Optional<Status> statusEntity = statusRepository.findByPlatformAndPlatformStatusCode(
                    platformName, facebookStatus.toString());

            if (!statusEntity.isPresent()) {
                // Auto-create status mapping
                Status status = Status.builder()
                        .platform(platformName)
                        .platformStatusCode(facebookStatus.toString())
                        .platformStatusName(standardStatus)
                        .standardStatusCode(standardStatus)
                        .standardStatusName(standardStatus)
                        .statusCategory(getStatusCategory(standardStatus))
                        .build();
                statusRepository.save(status);
                log.info("Created Facebook status mapping: {} -> {}", facebookStatus, standardStatus);
            }

        } catch (Exception e) {
            log.error("Failed to process status info for Facebook order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    /**
     * 7. Process Order Status Transition
     */
    private void processOrderStatusTransition(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            Integer facebookStatus = orderDto.getStatus();
            String standardStatus = mapFacebookStatusToString(facebookStatus);

            // Get status key
            Optional<Status> statusEntity = statusRepository.findByPlatformAndPlatformStatusCode(
                    platformName, facebookStatus.toString());

            if (statusEntity.isPresent()) {
                Long statusKey = statusEntity.get().getStatusKey();

                OrderStatus orderStatus = OrderStatus.builder()
                        .statusKey(statusKey)
                        .orderId(orderId)
                        .transitionTimestamp(LocalDateTime.now())
                        .durationInPreviousStatusHours(24) // Default
                        .transitionReason("Facebook order status change")
                        .transitionTrigger("SYSTEM")
                        .changedBy("FACEBOOK_API")
                        .isOnTimeTransition(true)
                        .isExpectedTransition(true)
                        .historyKey(System.currentTimeMillis() % 1000000L)
                        .build();

                orderStatusRepository.save(orderStatus);
            }

        } catch (Exception e) {
            log.error("Failed to process order status transition for Facebook order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    /**
     * 8. Process Order Status Detail
     */
    private void processOrderStatusDetailUpsert(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            Integer facebookStatus = orderDto.getStatus();

            Optional<Status> statusEntity = statusRepository.findByPlatformAndPlatformStatusCode(
                    platformName, facebookStatus.toString());

            if (statusEntity.isPresent()) {
                Long statusKey = statusEntity.get().getStatusKey();

                OrderStatusDetail statusDetail = OrderStatusDetail.builder()
                        .statusKey(statusKey)
                        .orderId(orderId)
                        .statusDescription("Facebook order status: " + facebookStatus)
                        .statusRules("Facebook platform rules")
                        .isAutomatedTransition(true)
                        .requiresCustomerAction(false)
                        .allowsCancellation(facebookStatus == 1) // Pending allows cancellation
                        .estimatedCompletionTime(LocalDateTime.now().plusDays(2))
                        .build();

                orderStatusDetailRepository.save(statusDetail);
            }

        } catch (Exception e) {
            log.error("Failed to process order status detail for Facebook order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    /**
     * 9. Process Payment Info
     */
    private void processPaymentInfoUpsert(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            Integer codAmount = orderDto.getData().getCod();

            PaymentInfo paymentInfo = PaymentInfo.builder()
                    .orderId(orderId)
                    .paymentMethod("COD")
                    .paymentProvider("FACEBOOK_COD")
                    .paymentGateway("Facebook")
                    .paymentStatus("PENDING")
                    .currencyCode("VND")
                    .originalAmount(codAmount != null ? codAmount.doubleValue() : 0.0)
                    .processedAmount(codAmount != null ? codAmount.doubleValue() : 0.0)
                    .paymentFee(0.0)
                    .isCod(true)
                    .refundableAmount(codAmount != null ? codAmount.doubleValue() : 0.0)
                    .build();

            paymentInfoRepository.save(paymentInfo);

        } catch (Exception e) {
            log.error("Failed to process payment info for Facebook order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    /**
     * 10. Process Shipping Info
     */
    private void processShippingInfoUpsert(FacebookOrderDto orderDto) {
        try {
            String orderId = orderDto.getOrderId();
            Integer codAmount = orderDto.getData().getCod();

            ShippingInfo shippingInfo = ShippingInfo.builder()
                    .orderId(orderId)
                    .shippingProvider("FACEBOOK_DEFAULT")
                    .shippingProviderId("FB_SHIPPING")
                    .shippingProviderName("Facebook Delivery")
                    .trackingNumber(generateFacebookTrackingNumber(orderDto))
                    .estimatedDeliveryDays(estimateDeliveryDays(codAmount))
                    .actualDeliveryDays(null)
                    .onTimeDeliveryRate(0.85)
                    .averageDeliveryDays(2.5)
                    .deliveryPerformanceScore(8.5)
                    .build();

            shippingInfoRepository.save(shippingInfo);

        } catch (Exception e) {
            log.error("Failed to process shipping info for Facebook order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    /**
     * 11. Process Date Info
     */
    private void processDateInfoUpsert(FacebookOrderDto orderDto) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Integer dateKey = generateDateKey(now.toLocalDate());

            Optional<ProcessingDateInfo> existingDate = processingDateInfoRepository.findById(dateKey);

            if (!existingDate.isPresent()) {
                ProcessingDateInfo dateInfo = ProcessingDateInfo.builder()
                        .dateKey(dateKey)
                        .fullDate(now.toLocalDate())
                        .year(now.getYear())
                        .month(now.getMonthValue())
                        .day(now.getDayOfMonth())
                        .quarterOfYear((now.getMonthValue() - 1) / 3 + 1)
                        .weekOfYear(now.getDayOfYear() / 7 + 1)
                        .dayOfWeek(now.getDayOfWeek().getValue())
                        .isWeekend(now.getDayOfWeek().getValue() >= 6)
                        .isHoliday(false)
                        .seasonName(getSeason(now.getMonthValue()))
                        .build();

                processingDateInfoRepository.save(dateInfo);
            }

        } catch (Exception e) {
            log.error("Failed to process date info for Facebook order {}: {}",
                    orderDto.getOrderId(), e.getMessage());
        }
    }

    // ===== HELPER METHODS =====

    private String generateCustomerId(FacebookCustomerDto customerDto) {
        if (customerDto.getPhoneNumbers() != null && !customerDto.getPhoneNumbers().isEmpty()) {
            return HashUtil.hashPhone(customerDto.getPhoneNumbers().get(0));
        } else if (customerDto.getFbId() != null) {
            return "FB_" + customerDto.getFbId().hashCode();
        } else if (customerDto.getCustomerId() != null) {
            return "FB_CUST_" + customerDto.getCustomerId();
        } else {
            return "FB_UNKNOWN_" + System.currentTimeMillis();
        }
    }

    private String mapFacebookStatusToString(Integer facebookStatus) {
        if (facebookStatus == null) return "UNKNOWN";
        switch (facebookStatus) {
            case 1: return "PENDING";
            case 2: return "DELIVERED";
            case 3: return "SHIPPED";
            case 9: return "CANCELLED";
            case 10: return "RETURNED";
            default: return "UNKNOWN";
        }
    }

    private String extractSku(FacebookItemDto item) {
        if (item.getVariationId() != null) {
            return item.getVariationId();
        } else if (item.getProductId() != null) {
            return item.getProductId();
        } else {
            return "FB_" + item.getId();
        }
    }

    private Double calculateTotalPrice(FacebookItemDto item) {
        if (item.getSalePrice() != null && item.getQuantity() != null) {
            return item.getSalePrice().doubleValue() * item.getQuantity();
        }
        return 0.0;
    }

    private Double calculateItemDiscount(FacebookItemDto item) {
        if (item.getRetailPrice() != null && item.getSalePrice() != null) {
            return item.getRetailPrice().doubleValue() - item.getSalePrice().doubleValue();
        }
        return 0.0;
    }

    private String getFirstPhoneNumber(List<String> phoneNumbers) {
        return phoneNumbers != null && !phoneNumbers.isEmpty() ? phoneNumbers.get(0) : null;
    }

    private String determineRegionType(String province) {
        if (province == null) return "UNKNOWN";
        if (province.contains("Hà Nội") || province.contains("TP.HCM")) return "METRO";
        return "PROVINCE";
    }

    private String determineEconomicTier(String province) {
        if (province == null) return "TIER_2";
        if (province.contains("Hà Nội") || province.contains("TP.HCM")) return "TIER_1";
        return "TIER_2";
    }

    private Boolean isUrbanArea(String province) {
        return province != null && (province.contains("Hà Nội") || province.contains("TP.HCM"));
    }

    private void createMinimalGeographyInfo(FacebookOrderDto orderDto) {
        GeographyInfo geography = GeographyInfo.builder()
                .orderId(orderDto.getOrderId())
                .recipientName("Facebook Customer")
                .fullAddress("Address not provided")
                .province("Unknown")
                .countryCode("VN")
                .regionType("UNKNOWN")
                .economicTier("TIER_2")
                .isUrbanArea(false)
                .build();

        geographyInfoRepository.save(geography);
    }

    private String getStatusCategory(String standardStatus) {
        switch (standardStatus) {
            case "DELIVERED": case "CANCELLED": case "RETURNED":
                return "FINAL";
            case "SHIPPED":
                return "SHIPPING";
            case "PENDING":
                return "PENDING";
            default:
                return "PROCESSING";
        }
    }

    private String generateFacebookTrackingNumber(FacebookOrderDto orderDto) {
        return "FB" + orderDto.getOrderId() + "_" + System.currentTimeMillis() % 10000;
    }

    private Double estimateDeliveryDays(Integer codAmount) {
        if (codAmount == null) return 3.0;
        if (codAmount > 1000000) return 1.0; // Express for high value
        if (codAmount > 500000) return 2.0;  // Fast delivery
        return 3.0; // Standard
    }

    private Integer generateDateKey(java.time.LocalDate date) {
        return date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth();
    }

    private String getSeason(int month) {
        if (month >= 3 && month <= 5) return "SPRING";
        if (month >= 6 && month <= 8) return "SUMMER";
        if (month >= 9 && month <= 11) return "AUTUMN";
        return "WINTER";
    }

    private void createNewCustomer(FacebookCustomerDto customerDto, FacebookOrderDto orderDto, String customerId) {
        Customer customer = Customer.builder()
                .customerId(customerId)
                .phoneHash(getPhoneHash(customerDto))
                .emailHash(getEmailHash(customerDto))
                .firstOrderDate(LocalDateTime.now())
                .lastOrderDate(LocalDateTime.now())
                .totalLifetimeValue(orderDto.getData().getCod() != null ? orderDto.getData().getCod().doubleValue() : 0.0)
                .totalOrderCount(1)
                .averageOrderValue(orderDto.getData().getCod() != null ? orderDto.getData().getCod().doubleValue() : 0.0)
                .customerSegment(determineCustomerSegment(customerDto))
                .isVip(customerDto.getOrderCount() != null && customerDto.getOrderCount() > 10)
                .platform(platformName)
                .build();

        customerRepository.save(customer);
    }

    private void updateExistingCustomer(Customer existing, FacebookCustomerDto customerDto, FacebookOrderDto orderDto) {
        existing.setLastOrderDate(LocalDateTime.now());
        existing.setTotalOrderCount(existing.getTotalOrderCount() + 1);

        Double orderValue = orderDto.getData().getCod() != null ? orderDto.getData().getCod().doubleValue() : 0.0;
        existing.setTotalLifetimeValue(existing.getTotalLifetimeValue() + orderValue);
        existing.setAverageOrderValue(existing.getTotalLifetimeValue() / existing.getTotalOrderCount());

        customerRepository.save(existing);
    }

    private void createNewOrder(FacebookOrderDto orderDto) {
        FacebookCustomerDto customer = orderDto.getData().getCustomer();
        String customerId = customer != null ? generateCustomerId(customer) : "UNKNOWN";

        Order order = Order.builder()
                .orderId(orderDto.getOrderId())
                .customerId(customerId)
                .shopId(orderDto.getShopId() != null ? orderDto.getShopId().toString() : "UNKNOWN")
                .grossRevenue(orderDto.getData().getCod() != null ? orderDto.getData().getCod().doubleValue() : 0.0)
                .platform(platformName)
                .orderStatus(mapFacebookStatusToString(orderDto.getStatus()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isCod(true)
                .notes(buildOrderNotes(orderDto))
                .build();

        orderRepository.save(order);
    }

    private void updateExistingOrder(Order existing, FacebookOrderDto orderDto) {
        existing.setOrderStatus(mapFacebookStatusToString(orderDto.getStatus()));
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setGrossRevenue(orderDto.getData().getCod() != null ? orderDto.getData().getCod().doubleValue() : existing.getGrossRevenue());
        orderRepository.save(existing);
    }

    private void createNewProduct(FacebookItemDto item, FacebookOrderDto orderDto) {
        String sku = extractSku(item);

        Product product = Product.builder()
                .sku(sku)
                .platformProductId(platformName)
                .productName(item.getProductName() != null ? item.getProductName() : "Facebook Product")
                .brand(item.getProductBrandName() != null ? item.getProductBrandName() : "Facebook Brand")
                .category(item.getProductCategoryName() != null ? item.getProductCategoryName() : "General")
                .subcategory("Facebook Items")
                .retailPrice(item.getRetailPrice() != null ? item.getRetailPrice().doubleValue() : 0.0)
                .salePrice(item.getSalePrice() != null ? item.getSalePrice().doubleValue() : 0.0)
                .weightGram(item.getProductWeight() != null ? item.getProductWeight().longValue() : 500L)
                .build();

        productRepository.save(product);
    }

    private void updateExistingProduct(Product existing, FacebookItemDto item) {
        existing.setRetailPrice(item.getRetailPrice() != null ? item.getRetailPrice().doubleValue() : existing.getRetailPrice());
        existing.setSalePrice(item.getSalePrice() != null ? item.getSalePrice().doubleValue() : existing.getSalePrice());
        productRepository.save(existing);
    }

    private String getPhoneHash(FacebookCustomerDto customerDto) {
        if (customerDto.getPhoneNumbers() != null && !customerDto.getPhoneNumbers().isEmpty()) {
            return HashUtil.hashPhone(customerDto.getPhoneNumbers().get(0));
        }
        return null;
    }

    private String getEmailHash(FacebookCustomerDto customerDto) {
        if (customerDto.getEmails() != null && !customerDto.getEmails().isEmpty()) {
            return HashUtil.hashEmail(customerDto.getEmails().get(0));
        }
        return null;
    }

    private String determineCustomerSegment(FacebookCustomerDto customerDto) {
        Integer orderCount = customerDto.getOrderCount();
        if (orderCount == null) return "NEW";

        if (orderCount >= 10) return "VIP";
        else if (orderCount >= 5) return "REGULAR";
        else if (orderCount >= 2) return "RETURNING";
        else return "NEW";
    }

    private String buildOrderNotes(FacebookOrderDto orderDto) {
        FacebookPageDto page = orderDto.getData().getPage();
        if (page != null) {
            return String.format("Facebook Order from Page: %s (ID: %s)", page.getName(), page.getId());
        }
        return "Facebook Order";
    }

    public static class EtlResult {
        private boolean success;
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int totalOrders;
        private int ordersProcessed;
        private List<FailedOrder> failedOrders = new ArrayList<>();

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public int getOrdersProcessed() { return ordersProcessed; }
        public void setOrdersProcessed(int ordersProcessed) { this.ordersProcessed = ordersProcessed; }

        public List<FailedOrder> getFailedOrders() { return failedOrders; }
        public void setFailedOrders(List<FailedOrder> failedOrders) { this.failedOrders = failedOrders; }

        public void addFailedOrder(String orderId, String errorMessage) {
            failedOrders.add(new FailedOrder(orderId, errorMessage));
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
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}