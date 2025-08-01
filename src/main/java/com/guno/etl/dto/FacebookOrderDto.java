// FacebookOrderDto.java - Facebook Order DTO (FULL STRUCTURE)
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FacebookOrderDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("nhanh_app_id")
    private Integer nhanhAppId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("data")
    private FacebookOrderData data;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getNhanhAppId() { return nhanhAppId; }
    public void setNhanhAppId(Integer nhanhAppId) { this.nhanhAppId = nhanhAppId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Integer getShopId() { return shopId; }
    public void setShopId(Integer shopId) { this.shopId = shopId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public FacebookOrderData getData() { return data; }
    public void setData(FacebookOrderData data) { this.data = data; }

    public static class FacebookOrderData {

        @JsonProperty("id")
        private Integer id;

        @JsonProperty("cod")
        private Integer cod;

        @JsonProperty("tax")
        private Integer tax;

        @JsonProperty("cash")
        private Integer cash;

        @JsonProperty("link")
        private String link;

        @JsonProperty("note")
        private String note;

        @JsonProperty("page")
        private FacebookPageDto page;

        @JsonProperty("tags")
        private List<FacebookTagDto> tags;

        @JsonProperty("type")
        private String type;

        @JsonProperty("ad_id")
        private String adId;

        @JsonProperty("items")
        private List<FacebookItemDto> items;

        @JsonProperty("customer")
        private FacebookCustomerDto customer;

        @JsonProperty("history")
        private List<FacebookHistoryDto> history;

        @JsonProperty("pke_mkter")
        private String pkeMkter;

        @JsonProperty("surcharge")
        private Integer surcharge;

        @JsonProperty("system_id")
        private Integer systemId;

        @JsonProperty("ads_source")
        private String adsSource;

        @JsonProperty("bill_email")
        private String billEmail;

        @JsonProperty("event_type")
        private String eventType;

        @JsonProperty("note_image")
        private String noteImage;

        @JsonProperty("note_print")
        private String notePrint;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;

        @JsonProperty("inserted_at")
        private String insertedAt;

        @JsonProperty("time_close")
        private String timeClose;

        @JsonProperty("time_confirm")
        private String timeConfirm;

        @JsonProperty("order_source")
        private String orderSource;

        @JsonProperty("partner_fee")
        private Integer partnerFee;

        @JsonProperty("customer_pay_fee")
        private Boolean customerPayFee;

        @JsonProperty("extend_code")
        private Long extendCode;

        @JsonProperty("partner_id")
        private Integer partnerId;

        @JsonProperty("shop_partner_id")
        private Integer shopPartnerId;

        @JsonProperty("time_send_partner")
        private String timeSendPartner;

        @JsonProperty("user_send_partner")
        private String userSendPartner;

        @JsonProperty("printed_form")
        private String printedForm;

        // Getters and setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public Integer getCod() { return cod; }
        public void setCod(Integer cod) { this.cod = cod; }

        public Integer getTax() { return tax; }
        public void setTax(Integer tax) { this.tax = tax; }

        public Integer getCash() { return cash; }
        public void setCash(Integer cash) { this.cash = cash; }

        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }

        public FacebookPageDto getPage() { return page; }
        public void setPage(FacebookPageDto page) { this.page = page; }

        public List<FacebookTagDto> getTags() { return tags; }
        public void setTags(List<FacebookTagDto> tags) { this.tags = tags; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getAdId() { return adId; }
        public void setAdId(String adId) { this.adId = adId; }

        public List<FacebookItemDto> getItems() { return items; }
        public void setItems(List<FacebookItemDto> items) { this.items = items; }

        public FacebookCustomerDto getCustomer() { return customer; }
        public void setCustomer(FacebookCustomerDto customer) { this.customer = customer; }

        public List<FacebookHistoryDto> getHistory() { return history; }
        public void setHistory(List<FacebookHistoryDto> history) { this.history = history; }

        public String getPkeMkter() { return pkeMkter; }
        public void setPkeMkter(String pkeMkter) { this.pkeMkter = pkeMkter; }

        public Integer getSurcharge() { return surcharge; }
        public void setSurcharge(Integer surcharge) { this.surcharge = surcharge; }

        public Integer getSystemId() { return systemId; }
        public void setSystemId(Integer systemId) { this.systemId = systemId; }

        public String getAdsSource() { return adsSource; }
        public void setAdsSource(String adsSource) { this.adsSource = adsSource; }

        public String getBillEmail() { return billEmail; }
        public void setBillEmail(String billEmail) { this.billEmail = billEmail; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public String getNoteImage() { return noteImage; }
        public void setNoteImage(String noteImage) { this.noteImage = noteImage; }

        public String getNotePrint() { return notePrint; }
        public void setNotePrint(String notePrint) { this.notePrint = notePrint; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

        public String getInsertedAt() { return insertedAt; }
        public void setInsertedAt(String insertedAt) { this.insertedAt = insertedAt; }

        public String getTimeClose() { return timeClose; }
        public void setTimeClose(String timeClose) { this.timeClose = timeClose; }

        public String getTimeConfirm() { return timeConfirm; }
        public void setTimeConfirm(String timeConfirm) { this.timeConfirm = timeConfirm; }

        public String getOrderSource() { return orderSource; }
        public void setOrderSource(String orderSource) { this.orderSource = orderSource; }

        public Integer getPartnerFee() { return partnerFee; }
        public void setPartnerFee(Integer partnerFee) { this.partnerFee = partnerFee; }

        public Boolean getCustomerPayFee() { return customerPayFee; }
        public void setCustomerPayFee(Boolean customerPayFee) { this.customerPayFee = customerPayFee; }

        public Long getExtendCode() { return extendCode; }
        public void setExtendCode(Long extendCode) { this.extendCode = extendCode; }

        public Integer getPartnerId() { return partnerId; }
        public void setPartnerId(Integer partnerId) { this.partnerId = partnerId; }

        public Integer getShopPartnerId() { return shopPartnerId; }
        public void setShopPartnerId(Integer shopPartnerId) { this.shopPartnerId = shopPartnerId; }

        public String getTimeSendPartner() { return timeSendPartner; }
        public void setTimeSendPartner(String timeSendPartner) { this.timeSendPartner = timeSendPartner; }

        public String getUserSendPartner() { return userSendPartner; }
        public void setUserSendPartner(String userSendPartner) { this.userSendPartner = userSendPartner; }

        public String getPrintedForm() { return printedForm; }
        public void setPrintedForm(String printedForm) { this.printedForm = printedForm; }
    }

    // Supporting DTOs for nested objects
    public static class FacebookTagDto {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class FacebookHistoryDto {
        @JsonProperty("tags")
        private FacebookTagChangeDto tags;

        @JsonProperty("status")
        private FacebookStatusChangeDto status;

        @JsonProperty("sub_status")
        private FacebookStatusChangeDto subStatus;

        @JsonProperty("editor_id")
        private String editorId;

        @JsonProperty("updated_at")
        private String updatedAt;

        @JsonProperty("printed_form")
        private String printedForm;

        @JsonProperty("note")
        private String note;

        @JsonProperty("type")
        private String type;

        @JsonProperty("partners")
        private List<String> partners;

        @JsonProperty("report_id")
        private Integer reportId;

        @JsonProperty("type_print")
        private String typePrint;

        @JsonProperty("name_of_printer")
        private String nameOfPrinter;

        @JsonProperty("printed_form_report")
        private String printedFormReport;

        @JsonProperty("partner_id")
        private FacebookChangeValue<Integer> partnerId;

        @JsonProperty("extend_code")
        private FacebookChangeValue<Long> extendCode;

        @JsonProperty("partner_fee")
        private FacebookChangeValue<Integer> partnerFee;

        @JsonProperty("partner_count")
        private FacebookChangeValue<List<FacebookPartnerCount>> partnerCount;

        @JsonProperty("shop_partner_id")
        private FacebookChangeValue<Integer> shopPartnerId;

        @JsonProperty("customer_pay_fee")
        private FacebookChangeValue<Boolean> customerPayFee;

        @JsonProperty("time_send_partner")
        private FacebookChangeValue<String> timeSendPartner;

        @JsonProperty("user_send_partner")
        private FacebookChangeValue<String> userSendPartner;

        // Getters and setters
        public FacebookTagChangeDto getTags() { return tags; }
        public void setTags(FacebookTagChangeDto tags) { this.tags = tags; }

        public FacebookStatusChangeDto getStatus() { return status; }
        public void setStatus(FacebookStatusChangeDto status) { this.status = status; }

        public FacebookStatusChangeDto getSubStatus() { return subStatus; }
        public void setSubStatus(FacebookStatusChangeDto subStatus) { this.subStatus = subStatus; }

        public String getEditorId() { return editorId; }
        public void setEditorId(String editorId) { this.editorId = editorId; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

        public String getPrintedForm() { return printedForm; }
        public void setPrintedForm(String printedForm) { this.printedForm = printedForm; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public List<String> getPartners() { return partners; }
        public void setPartners(List<String> partners) { this.partners = partners; }

        public Integer getReportId() { return reportId; }
        public void setReportId(Integer reportId) { this.reportId = reportId; }

        public String getTypePrint() { return typePrint; }
        public void setTypePrint(String typePrint) { this.typePrint = typePrint; }

        public String getNameOfPrinter() { return nameOfPrinter; }
        public void setNameOfPrinter(String nameOfPrinter) { this.nameOfPrinter = nameOfPrinter; }

        public String getPrintedFormReport() { return printedFormReport; }
        public void setPrintedFormReport(String printedFormReport) { this.printedFormReport = printedFormReport; }

        public FacebookChangeValue<Integer> getPartnerId() { return partnerId; }
        public void setPartnerId(FacebookChangeValue<Integer> partnerId) { this.partnerId = partnerId; }

        public FacebookChangeValue<Long> getExtendCode() { return extendCode; }
        public void setExtendCode(FacebookChangeValue<Long> extendCode) { this.extendCode = extendCode; }

        public FacebookChangeValue<Integer> getPartnerFee() { return partnerFee; }
        public void setPartnerFee(FacebookChangeValue<Integer> partnerFee) { this.partnerFee = partnerFee; }

        public FacebookChangeValue<List<FacebookPartnerCount>> getPartnerCount() { return partnerCount; }
        public void setPartnerCount(FacebookChangeValue<List<FacebookPartnerCount>> partnerCount) { this.partnerCount = partnerCount; }

        public FacebookChangeValue<Integer> getShopPartnerId() { return shopPartnerId; }
        public void setShopPartnerId(FacebookChangeValue<Integer> shopPartnerId) { this.shopPartnerId = shopPartnerId; }

        public FacebookChangeValue<Boolean> getCustomerPayFee() { return customerPayFee; }
        public void setCustomerPayFee(FacebookChangeValue<Boolean> customerPayFee) { this.customerPayFee = customerPayFee; }

        public FacebookChangeValue<String> getTimeSendPartner() { return timeSendPartner; }
        public void setTimeSendPartner(FacebookChangeValue<String> timeSendPartner) { this.timeSendPartner = timeSendPartner; }

        public FacebookChangeValue<String> getUserSendPartner() { return userSendPartner; }
        public void setUserSendPartner(FacebookChangeValue<String> userSendPartner) { this.userSendPartner = userSendPartner; }
    }

    public static class FacebookTagChangeDto {
        @JsonProperty("new")
        private List<FacebookTagDto> newTags;

        @JsonProperty("old")
        private List<FacebookTagDto> oldTags;

        public List<FacebookTagDto> getNewTags() { return newTags; }
        public void setNewTags(List<FacebookTagDto> newTags) { this.newTags = newTags; }

        public List<FacebookTagDto> getOldTags() { return oldTags; }
        public void setOldTags(List<FacebookTagDto> oldTags) { this.oldTags = oldTags; }
    }

    public static class FacebookStatusChangeDto {
        @JsonProperty("new")
        private Integer newStatus;

        @JsonProperty("old")
        private Integer oldStatus;

        public Integer getNewStatus() { return newStatus; }
        public void setNewStatus(Integer newStatus) { this.newStatus = newStatus; }

        public Integer getOldStatus() { return oldStatus; }
        public void setOldStatus(Integer oldStatus) { this.oldStatus = oldStatus; }
    }

    public static class FacebookChangeValue<T> {
        @JsonProperty("new")
        private T newValue;

        @JsonProperty("old")
        private T oldValue;

        public T getNewValue() { return newValue; }
        public void setNewValue(T newValue) { this.newValue = newValue; }

        public T getOldValue() { return oldValue; }
        public void setOldValue(T oldValue) { this.oldValue = oldValue; }
    }

    public static class FacebookPartnerCount {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("value")
        private Integer value;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
    }
}