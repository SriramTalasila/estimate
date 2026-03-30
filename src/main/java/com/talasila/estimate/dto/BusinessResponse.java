package com.talasila.estimate.dto;

public class BusinessResponse {
    private Long id;
    private String shopName;
    private String shopAddress;
    private String phone;
    private String gstNumber;

    public BusinessResponse() {
    }

    public BusinessResponse(Long id, String shopName, String shopAddress, String phone, String gstNumber) {
        this.id = id;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.phone = phone;
        this.gstNumber = gstNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }
}