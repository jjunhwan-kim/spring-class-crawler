package com.example.crawler.domain.coloso;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Product {

    @JsonProperty("@context")
    private String context;

    @JsonProperty("@type")
    private String type;

    @JsonProperty("brand")
    private String brand;

    @JsonProperty("productID")
    private Long productId;

    @JsonProperty("offers")
    private List<Offer> offers;

    @Data
    public static class Offer {
        @JsonProperty("@type")
        private String type;

        @JsonProperty("priceCurrency")
        private String priceCurrency;

        @JsonProperty("price")
        private Long price;

        @JsonProperty("priceSpecification")
        private List<PriceSpecification> priceSpecifications;

        @Data
        public static class PriceSpecification {

            @JsonProperty("@type")
            private String type;

            @JsonProperty("priceCurrency")
            private String priceCurrency;

            @JsonProperty("price")
            private Long price;
        }
    }
}
