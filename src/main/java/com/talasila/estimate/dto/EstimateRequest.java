package com.talasila.estimate.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class EstimateRequest {

    @NotNull
    private Long customerId;

    @NotEmpty
    @Valid
    private List<EstimateItemRequest> items;

    private List<@Valid EstimateNoteRequest> notes;

    private BigDecimal totalDiscount;

    private BigDecimal taxAmount;

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