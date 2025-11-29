package com.servicedesk.monolith.channel.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookDto {

    private String object;
    private List<Entry> entry;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        private String id;
        private List<Change> changes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        private Value value;
        private String field;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        @JsonProperty("messaging_product")
        private String messagingProduct;

        private Metadata metadata;
        private List<Contact> contacts;
        private List<Message> messages;
        private List<Status> statuses;
        private List<Error> errors;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        @JsonProperty("display_phone_number")
        private String displayPhoneNumber;

        @JsonProperty("phone_number_id")
        private String phoneNumberId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {
        private Profile profile;

        @JsonProperty("wa_id")
        private String waId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String from;
        private String id;
        private String timestamp;
        private String type;

        private Text text;
        private Image image;
        private Audio audio;
        private Video video;
        private Document document;
        private Sticker sticker;
        private Location location;
        private List<ContactInfo> contacts;
        private Interactive interactive;
        private Button button;
        private Context context;
        private Reaction reaction;
        private Referral referral;
        private System system;
        private List<Error> errors;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Text {
        private String body;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {
        private String caption;
        @JsonProperty("mime_type")
        private String mimeType;
        private String sha256;
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Audio {
        @JsonProperty("mime_type")
        private String mimeType;
        private String sha256;
        private String id;
        private Boolean voice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Video {
        private String caption;
        @JsonProperty("mime_type")
        private String mimeType;
        private String sha256;
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        private String caption;
        private String filename;
        @JsonProperty("mime_type")
        private String mimeType;
        private String sha256;
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sticker {
        @JsonProperty("mime_type")
        private String mimeType;
        private String sha256;
        private String id;
        private Boolean animated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private Double latitude;
        private Double longitude;
        private String name;
        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactInfo {
        private Name name;
        private List<Phone> phones;
        private List<Email> emails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Name {
        @JsonProperty("formatted_name")
        private String formattedName;
        @JsonProperty("first_name")
        private String firstName;
        @JsonProperty("last_name")
        private String lastName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Phone {
        private String phone;
        private String type;
        @JsonProperty("wa_id")
        private String waId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Email {
        private String email;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Interactive {
        private String type;
        @JsonProperty("button_reply")
        private ButtonReply buttonReply;
        @JsonProperty("list_reply")
        private ListReply listReply;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ButtonReply {
        private String id;
        private String title;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListReply {
        private String id;
        private String title;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Button {
        private String text;
        private String payload;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Context {
        private String from;
        private String id;
        private Referred referred_product;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Referred {
        @JsonProperty("catalog_id")
        private String catalogId;
        @JsonProperty("product_retailer_id")
        private String productRetailerId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Reaction {
        @JsonProperty("message_id")
        private String messageId;
        private String emoji;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Referral {
        @JsonProperty("source_url")
        private String sourceUrl;
        @JsonProperty("source_type")
        private String sourceType;
        @JsonProperty("source_id")
        private String sourceId;
        private String headline;
        private String body;
        @JsonProperty("media_type")
        private String mediaType;
        @JsonProperty("image_url")
        private String imageUrl;
        @JsonProperty("video_url")
        private String videoUrl;
        @JsonProperty("thumbnail_url")
        private String thumbnailUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class System {
        private String body;
        private String identity;
        @JsonProperty("wa_id")
        private String waId;
        private String type;
        private String customer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private String id;
        private String status;
        private String timestamp;
        @JsonProperty("recipient_id")
        private String recipientId;
        private Conversation conversation;
        private Pricing pricing;
        private List<Error> errors;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Conversation {
        private String id;
        private Origin origin;
        @JsonProperty("expiration_timestamp")
        private String expirationTimestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Origin {
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pricing {
        private Boolean billable;
        @JsonProperty("pricing_model")
        private String pricingModel;
        private String category;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Error {
        private Integer code;
        private String title;
        private String message;
        @JsonProperty("error_data")
        private ErrorData errorData;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorData {
        private String details;
    }
}
