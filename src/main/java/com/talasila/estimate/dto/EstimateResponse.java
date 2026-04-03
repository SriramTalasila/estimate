package com.talasila.estimate.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class EstimateResponse {
    private Long id;
    private String estimateNumber;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private BigDecimal totalBeforeDiscount;
    private BigDecimal totalDiscount;
    private BigDecimal taxAmount;
    private BigDecimal additionalDiscount;
    private String additionalDiscountType;
    private BigDecimal finalTotal;
    private LocalDateTime createdAt;
    private List<EstimateItemResponse> items;
    private List<EstimateNoteResponse> notes;
    private List<CategoryDiscountResponse> categoryDiscounts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEstimateNumber() {
        return estimateNumber;
    }

    public void setEstimateNumber(String estimateNumber) {
        this.estimateNumber = estimateNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

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

    public BigDecimal getTotalBeforeDiscount() {
        return totalBeforeDiscount;
    }

    public void setTotalBeforeDiscount(BigDecimal totalBeforeDiscount) {
        this.totalBeforeDiscount = totalBeforeDiscount;
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

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(BigDecimal finalTotal) {
        this.finalTotal = finalTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<EstimateItemResponse> getItems() {
        return items;
    }

    public void setItems(List<EstimateItemResponse> items) {
        this.items = items;
    }

    public List<EstimateNoteResponse> getNotes() {
        return notes;
    }

    public void setNotes(List<EstimateNoteResponse> notes) {
        this.notes = notes;
    }

    public List<CategoryDiscountResponse> getCategoryDiscounts() {
        return categoryDiscounts;
    }

    public void setCategoryDiscounts(List<CategoryDiscountResponse> categoryDiscounts) {
        this.categoryDiscounts = categoryDiscounts;
    }
}