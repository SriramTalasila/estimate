package com.talasila.estimate.dto;

import java.math.BigDecimal;

public class ProductResponse {
    private Long id;
    private String name;
    private String unit;
    private BigDecimal price;

    public ProductResponse() {
    }

    public ProductResponse(Long id, String name, String unit, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}