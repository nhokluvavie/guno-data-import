// FacebookPageDto.java - OPTIMIZED with String IDs
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookPageDto {

    @JsonProperty("id")
    private String id;  // ✅ Fixed: String for large Facebook Page IDs

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

    @JsonProperty("talking_about_count")
    private Integer talkingAboutCount;

    @JsonProperty("were_here_count")
    private Integer wereHereCount;

    @JsonProperty("overall_star_rating")
    private Double overallStarRating;

    @JsonProperty("rating_count")
    private Integer ratingCount;

    @JsonProperty("is_verified")
    private Boolean isVerified;

    @JsonProperty("is_published")
    private Boolean isPublished;

    @JsonProperty("can_post")
    private Boolean canPost;

    @JsonProperty("can_checkin")
    private Boolean canCheckin;

    @JsonProperty("is_community_page")
    private Boolean isCommunityPage;

    @JsonProperty("is_unclaimed")
    private Boolean isUnclaimed;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("instagram_business_account")
    private FacebookInstagramAccount instagramBusinessAccount;

    @JsonProperty("whatsapp_number")
    private String whatsappNumber;

    @JsonProperty("engagement")
    private FacebookPageEngagement engagement;

    @JsonProperty("created_time")
    private String createdTime;

    @JsonProperty("link")
    private String link;

    @JsonProperty("business_id")
    private String businessId;  // ✅ Fixed: String for large Business IDs

    @JsonProperty("business_name")
    private String businessName;

    // ===== NESTED DTOs =====

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookPageCategory {
        @JsonProperty("id")
        private String id;  // ✅ Fixed: String for large IDs

        @JsonProperty("name")
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
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
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
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
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookPageCover {
        @JsonProperty("cover_id")
        private String coverId;  // ✅ Fixed: String for large IDs

        @JsonProperty("source")
        private String source;

        @JsonProperty("offset_y")
        private Integer offsetY;

        @JsonProperty("offset_x")
        private Integer offsetX;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookPagePicture {
        @JsonProperty("data")
        private FacebookPagePictureData data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookPagePictureData {
        @JsonProperty("height")
        private Integer height;

        @JsonProperty("is_silhouette")
        private Boolean isSilhouette;

        @JsonProperty("url")
        private String url;

        @JsonProperty("width")
        private Integer width;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookInstagramAccount {
        @JsonProperty("id")
        private String id;  // ✅ Fixed: String for large Instagram IDs

        @JsonProperty("username")
        private String username;

        @JsonProperty("name")
        private String name;

        @JsonProperty("profile_picture_url")
        private String profilePictureUrl;

        @JsonProperty("followers_count")
        private Integer followersCount;

        @JsonProperty("follows_count")
        private Integer followsCount;

        @JsonProperty("media_count")
        private Integer mediaCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookPageEngagement {
        @JsonProperty("count")
        private Integer count;

        @JsonProperty("social_sentence")
        private String socialSentence;
    }
}