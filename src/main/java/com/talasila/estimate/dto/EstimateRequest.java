package com.talasila.estimate.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public class EstimateRequest {

    private Long customerId;
    private String customerName;
    private String customerPhone;

    @NotEmpty
    @Valid
    private List<EstimateItemRequest> items;

    private List<@Valid EstimateNoteRequest> notes;

    private List<@Valid CategoryDiscountRequest> categoryDiscounts;

    private BigDecimal additionalDiscount;

    private String additionalDiscountType;

    private BigDecimal finalAmount;

    private BigDecimal totalDiscount;

    private BigDecimal taxAmount;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<EstimateItemRequest> getItems() {
        return items;
    }

    public void setItems(List<EstimateItemRequest> items) {
        this.items = items;
    }

    public List<EstimateNoteRequest> getNotes() {
        return notes;
    }

    public void setNotes(List<EstimateNoteRequest> notes) {
        this.notes = notes;
    }

    public List<CategoryDiscountRequest> getCategoryDiscounts() {
        return categoryDiscounts;
    }

    public void setCategoryDiscounts(List<CategoryDiscountRequest> categoryDiscounts) {
        this.categoryDiscounts = categoryDiscounts;
    }

    public BigDecimal getAdditionalDiscount() {
        return additionalDiscount;
    }

    public void setAdditionalDiscount(BigDecimal additionalDiscount) {
        this.additionalDiscount = additionalDiscount;
    }

    public String getAdditionalDiscountType() {
        return additionalDiscountType;
    }

    public void setAdditionalDiscountType(String additionalDiscountType) {
        this.additionalDiscountType = additionalDiscountType;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
}