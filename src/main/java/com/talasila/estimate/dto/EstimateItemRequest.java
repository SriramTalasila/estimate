package com.talasila.estimate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class EstimateItemRequest {

    @NotNull
    private Long productId;

    @NotNull
    @Positive
    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal discount;

    private String discountType;

    private boolean overrideCategoryDiscount;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public boolean isOverrideCategoryDiscount() {
        return overrideCategoryDiscount;
    }

    public void setOverrideCategoryDiscount(boolean overrideCategoryDiscount) {
        this.overrideCategoryDiscount = overrideCategoryDiscount;
    }
}