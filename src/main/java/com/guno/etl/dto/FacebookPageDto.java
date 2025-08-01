// FacebookPageDto.java - Facebook Page DTO
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FacebookPageDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("username")
    private String username;

    @JsonProperty("category")
    private String category;

    @JsonProperty("category_list")
    private List<FacebookPageCategory> categoryList;

    @JsonProperty("about")
    private String about;

    @JsonProperty("description")
    private String description;

    @JsonProperty("website")
    private String website;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("location")
    private FacebookPageLocation location;

    @JsonProperty("hours")
    private FacebookPageHours hours;

    @JsonProperty("cover")
    private FacebookPageCover cover;

    @JsonProperty("picture")
    private FacebookPagePicture picture;

    @JsonProperty("fan_count")
    private Integer fanCount;

    @JsonProperty("followers_count")
    private Integer followersCount;

    @JsonProperty("checkins")
    private Integer checkins;

    @JsonProperty("rating_count")
    private Integer ratingCount;

    @JsonProperty("overall_star_rating")
    private Double overallStarRating;

    @JsonProperty("verification_status")
    private String verificationStatus;

    @JsonProperty("is_verified")
    private Boolean isVerified;

    @JsonProperty("is_published")
    private Boolean isPublished;

    @JsonProperty("is_unclaimed")
    private Boolean isUnclaimed;

    @JsonProperty("is_community_page")
    private Boolean isCommunityPage;

    @JsonProperty("has_whatsapp_number")
    private Boolean hasWhatsappNumber;

    @JsonProperty("whatsapp_number")
    private String whatsappNumber;

    @JsonProperty("instagram_business_account")
    private FacebookInstagramAccount instagramBusinessAccount;

    @JsonProperty("engagement")
    private FacebookPageEngagement engagement;

    @JsonProperty("page_token")
    private String pageToken;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("permissions")
    private List<String> permissions;

    @JsonProperty("tasks")
    private List<String> tasks;

    @JsonProperty("country_page_likes")
    private List<FacebookCountryLikes> countryPageLikes;

    @JsonProperty("page_impressions")
    private Integer pageImpressions;

    @JsonProperty("page_impressions_unique")
    private Integer pageImpressionsUnique;

    @JsonProperty("page_reach")
    private Integer pageReach;

    @JsonProperty("page_engaged_users")
    private Integer pageEngagedUsers;

    @JsonProperty("created_time")
    private String createdTime;

    @JsonProperty("updated_time")
    private String updatedTime;

    @JsonProperty("link")
    private String link;

    @JsonProperty("parent_page")
    private String parentPage;

    @JsonProperty("best_page")
    private FacebookBestPage bestPage;

    @JsonProperty("store_location_descriptor")
    private String storeLocationDescriptor;

    @JsonProperty("supports_instant_articles")
    private Boolean supportsInstantArticles;

    @JsonProperty("messenger_ads_default_icebreakers")
    private List<String> messengerAdsDefaultIcebreakers;

    @JsonProperty("messenger_ads_default_quick_replies")
    private List<FacebookQuickReply> messengerAdsDefaultQuickReplies;

    @JsonProperty("supported_platforms")
    private List<String> supportedPlatforms;

    @JsonProperty("business_id")
    private String businessId;

    @JsonProperty("business_name")
    private String businessName;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<FacebookPageCategory> getCategoryList() { return categoryList; }
    public void setCategoryList(List<FacebookPageCategory> categoryList) { this.categoryList = categoryList; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public FacebookPageLocation getLocation() { return location; }
    public void setLocation(FacebookPageLocation location) { this.location = location; }

    public FacebookPageHours getHours() { return hours; }
    public void setHours(FacebookPageHours hours) { this.hours = hours; }

    public FacebookPageCover getCover() { return cover; }
    public void setCover(FacebookPageCover cover) { this.cover = cover; }

    public FacebookPagePicture getPicture() { return picture; }
    public void setPicture(FacebookPagePicture picture) { this.picture = picture; }

    public Integer getFanCount() { return fanCount; }
    public void setFanCount(Integer fanCount) { this.fanCount = fanCount; }

    public Integer getFollowersCount() { return followersCount; }
    public void setFollowersCount(Integer followersCount) { this.followersCount = followersCount; }

    public Integer getCheckins() { return checkins; }
    public void setCheckins(Integer checkins) { this.checkins = checkins; }

    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }

    public Double getOverallStarRating() { return overallStarRating; }
    public void setOverallStarRating(Double overallStarRating) { this.overallStarRating = overallStarRating; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }

    public Boolean getIsUnclaimed() { return isUnclaimed; }
    public void setIsUnclaimed(Boolean isUnclaimed) { this.isUnclaimed = isUnclaimed; }

    public Boolean getIsCommunityPage() { return isCommunityPage; }
    public void setIsCommunityPage(Boolean isCommunityPage) { this.isCommunityPage = isCommunityPage; }

    public Boolean getHasWhatsappNumber() { return hasWhatsappNumber; }
    public void setHasWhatsappNumber(Boolean hasWhatsappNumber) { this.hasWhatsappNumber = hasWhatsappNumber; }

    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }

    public FacebookInstagramAccount getInstagramBusinessAccount() { return instagramBusinessAccount; }
    public void setInstagramBusinessAccount(FacebookInstagramAccount instagramBusinessAccount) { this.instagramBusinessAccount = instagramBusinessAccount; }

    public FacebookPageEngagement getEngagement() { return engagement; }
    public void setEngagement(FacebookPageEngagement engagement) { this.engagement = engagement; }

    public String getPageToken() { return pageToken; }
    public void setPageToken(String pageToken) { this.pageToken = pageToken; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public List<String> getTasks() { return tasks; }
    public void setTasks(List<String> tasks) { this.tasks = tasks; }

    public List<FacebookCountryLikes> getCountryPageLikes() { return countryPageLikes; }
    public void setCountryPageLikes(List<FacebookCountryLikes> countryPageLikes) { this.countryPageLikes = countryPageLikes; }

    public Integer getPageImpressions() { return pageImpressions; }
    public void setPageImpressions(Integer pageImpressions) { this.pageImpressions = pageImpressions; }

    public Integer getPageImpressionsUnique() { return pageImpressionsUnique; }
    public void setPageImpressionsUnique(Integer pageImpressionsUnique) { this.pageImpressionsUnique = pageImpressionsUnique; }

    public Integer getPageReach() { return pageReach; }
    public void setPageReach(Integer pageReach) { this.pageReach = pageReach; }

    public Integer getPageEngagedUsers() { return pageEngagedUsers; }
    public void setPageEngagedUsers(Integer pageEngagedUsers) { this.pageEngagedUsers = pageEngagedUsers; }

    public String getCreatedTime() { return createdTime; }
    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }

    public String getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(String updatedTime) { this.updatedTime = updatedTime; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getParentPage() { return parentPage; }
    public void setParentPage(String parentPage) { this.parentPage = parentPage; }

    public FacebookBestPage getBestPage() { return bestPage; }
    public void setBestPage(FacebookBestPage bestPage) { this.bestPage = bestPage; }

    public String getStoreLocationDescriptor() { return storeLocationDescriptor; }
    public void setStoreLocationDescriptor(String storeLocationDescriptor) { this.storeLocationDescriptor = storeLocationDescriptor; }

    public Boolean getSupportsInstantArticles() { return supportsInstantArticles; }
    public void setSupportsInstantArticles(Boolean supportsInstantArticles) { this.supportsInstantArticles = supportsInstantArticles; }

    public List<String> getMessengerAdsDefaultIcebreakers() { return messengerAdsDefaultIcebreakers; }
    public void setMessengerAdsDefaultIcebreakers(List<String> messengerAdsDefaultIcebreakers) { this.messengerAdsDefaultIcebreakers = messengerAdsDefaultIcebreakers; }

    public List<FacebookQuickReply> getMessengerAdsDefaultQuickReplies() { return messengerAdsDefaultQuickReplies; }
    public void setMessengerAdsDefaultQuickReplies(List<FacebookQuickReply> messengerAdsDefaultQuickReplies) { this.messengerAdsDefaultQuickReplies = messengerAdsDefaultQuickReplies; }

    public List<String> getSupportedPlatforms() { return supportedPlatforms; }
    public void setSupportedPlatforms(List<String> supportedPlatforms) { this.supportedPlatforms = supportedPlatforms; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    // Supporting nested classes
    public static class FacebookPageCategory {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class FacebookPageLocation {
        @JsonProperty("city")
        private String city;

        @JsonProperty("country")
        private String country;

        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;

        @JsonProperty("state")
        private String state;

        @JsonProperty("street")
        private String street;

        @JsonProperty("zip")
        private String zip;

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        public String getZip() { return zip; }
        public void setZip(String zip) { this.zip = zip; }
    }

    public static class FacebookPageHours {
        @JsonProperty("mon_1_open")
        private String mon1Open;

        @JsonProperty("mon_1_close")
        private String mon1Close;

        @JsonProperty("tue_1_open")
        private String tue1Open;

        @JsonProperty("tue_1_close")
        private String tue1Close;

        @JsonProperty("wed_1_open")
        private String wed1Open;

        @JsonProperty("wed_1_close")
        private String wed1Close;

        @JsonProperty("thu_1_open")
        private String thu1Open;

        @JsonProperty("thu_1_close")
        private String thu1Close;

        @JsonProperty("fri_1_open")
        private String fri1Open;

        @JsonProperty("fri_1_close")
        private String fri1Close;

        @JsonProperty("sat_1_open")
        private String sat1Open;

        @JsonProperty("sat_1_close")
        private String sat1Close;

        @JsonProperty("sun_1_open")
        private String sun1Open;

        @JsonProperty("sun_1_close")
        private String sun1Close;

        // Getters and setters
        public String getMon1Open() { return mon1Open; }
        public void setMon1Open(String mon1Open) { this.mon1Open = mon1Open; }

        public String getMon1Close() { return mon1Close; }
        public void setMon1Close(String mon1Close) { this.mon1Close = mon1Close; }

        public String getTue1Open() { return tue1Open; }
        public void setTue1Open(String tue1Open) { this.tue1Open = tue1Open; }

        public String getTue1Close() { return tue1Close; }
        public void setTue1Close(String tue1Close) { this.tue1Close = tue1Close; }

        public String getWed1Open() { return wed1Open; }
        public void setWed1Open(String wed1Open) { this.wed1Open = wed1Open; }

        public String getWed1Close() { return wed1Close; }
        public void setWed1Close(String wed1Close) { this.wed1Close = wed1Close; }

        public String getThu1Open() { return thu1Open; }
        public void setThu1Open(String thu1Open) { this.thu1Open = thu1Open; }

        public String getThu1Close() { return thu1Close; }
        public void setThu1Close(String thu1Close) { this.thu1Close = thu1Close; }

        public String getFri1Open() { return fri1Open; }
        public void setFri1Open(String fri1Open) { this.fri1Open = fri1Open; }

        public String getFri1Close() { return fri1Close; }
        public void setFri1Close(String fri1Close) { this.fri1Close = fri1Close; }

        public String getSat1Open() { return sat1Open; }
        public void setSat1Open(String sat1Open) { this.sat1Open = sat1Open; }

        public String getSat1Close() { return sat1Close; }
        public void setSat1Close(String sat1Close) { this.sat1Close = sat1Close; }

        public String getSun1Open() { return sun1Open; }
        public void setSun1Open(String sun1Open) { this.sun1Open = sun1Open; }

        public String getSun1Close() { return sun1Close; }
        public void setSun1Close(String sun1Close) { this.sun1Close = sun1Close; }
    }

    public static class FacebookPageCover {
        @JsonProperty("cover_id")
        private String coverId;

        @JsonProperty("source")
        private String source;

        @JsonProperty("offset_x")
        private Integer offsetX;

        @JsonProperty("offset_y")
        private Integer offsetY;

        public String getCoverId() { return coverId; }
        public void setCoverId(String coverId) { this.coverId = coverId; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public Integer getOffsetX() { return offsetX; }
        public void setOffsetX(Integer offsetX) { this.offsetX = offsetX; }

        public Integer getOffsetY() { return offsetY; }
        public void setOffsetY(Integer offsetY) { this.offsetY = offsetY; }
    }

    public static class FacebookPagePicture {
        @JsonProperty("data")
        private FacebookPictureData data;

        public FacebookPictureData getData() { return data; }
        public void setData(FacebookPictureData data) { this.data = data; }

        public static class FacebookPictureData {
            @JsonProperty("height")
            private Integer height;

            @JsonProperty("width")
            private Integer width;

            @JsonProperty("is_silhouette")
            private Boolean isSilhouette;

            @JsonProperty("url")
            private String url;

            public Integer getHeight() { return height; }
            public void setHeight(Integer height) { this.height = height; }

            public Integer getWidth() { return width; }
            public void setWidth(Integer width) { this.width = width; }

            public Boolean getIsSilhouette() { return isSilhouette; }
            public void setIsSilhouette(Boolean isSilhouette) { this.isSilhouette = isSilhouette; }

            public String getUrl() { return url; }
            public void setUrl(String url) { this.url = url; }
        }
    }

    public static class FacebookInstagramAccount {
        @JsonProperty("id")
        private String id;

        @JsonProperty("username")
        private String username;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    public static class FacebookPageEngagement {
        @JsonProperty("count")
        private Integer count;

        @JsonProperty("social_sentence")
        private String socialSentence;

        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }

        public String getSocialSentence() { return socialSentence; }
        public void setSocialSentence(String socialSentence) { this.socialSentence = socialSentence; }
    }

    public static class FacebookCountryLikes {
        @JsonProperty("country")
        private String country;

        @JsonProperty("likes")
        private Integer likes;

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public Integer getLikes() { return likes; }
        public void setLikes(Integer likes) { this.likes = likes; }
    }

    public static class FacebookBestPage {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class FacebookQuickReply {
        @JsonProperty("content_type")
        private String contentType;

        @JsonProperty("title")
        private String title;

        @JsonProperty("payload")
        private String payload;

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getPayload() { return payload; }
        public void setPayload(String payload) { this.payload = payload; }
    }
}