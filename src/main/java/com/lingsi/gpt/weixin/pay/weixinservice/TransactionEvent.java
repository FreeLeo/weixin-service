package com.lingsi.gpt.weixin.pay.weixinservice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionEvent {
    @JsonProperty("id")
    private String id;

    @JsonProperty("create_time")
    private String createTime;

    @JsonProperty("resource_type")
    private String resourceType;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("resource")
    private Resource resource;

    // Constructors, getters, and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public static class Resource {
        @JsonProperty("original_type")
        private String originalType;

        @JsonProperty("algorithm")
        private String algorithm;

        @JsonProperty("ciphertext")
        private String ciphertext;

        @JsonProperty("associated_data")
        private String associatedData;

        @JsonProperty("nonce")
        private String nonce;

        // Constructors, getters, and setters

        public String getOriginalType() {
            return originalType;
        }

        public void setOriginalType(String originalType) {
            this.originalType = originalType;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public String getCiphertext() {
            return ciphertext;
        }

        public void setCiphertext(String ciphertext) {
            this.ciphertext = ciphertext;
        }

        public String getAssociatedData() {
            return associatedData;
        }

        public void setAssociatedData(String associatedData) {
            this.associatedData = associatedData;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }
    }
}
