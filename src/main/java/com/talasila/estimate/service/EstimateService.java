package com.talasila.estimate.service;

import com.talasila.estimate.dto.EstimateRequest;
import com.talasila.estimate.model.*;
import com.talasila.estimate.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class EstimateService {

    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final EstimateRepository estimateRepository;

    public EstimateService(BusinessRepository businessRepository, CustomerRepository customerRepository, ProductRepository productRepository, EstimateRepository estimateRepository) {
        this.businessRepository = businessRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.estimateRepository = estimateRepository;
    }

    @Transactional
    public Estimate createEstimate(Long businessId, EstimateRequest request) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!customer.getBusiness().getId().equals(businessId)) {
            throw new IllegalStateException("Customer does not belong to this business.");
        }

        Estimate estimate = new Estimate();
        estimate.setBusiness(business);
        estimate.setCustomer(customer);
        estimate.setEstimateNumber(generateEstimateNumber());

        processEstimateItems(estimate, request);
        processEstimateNotes(estimate, request);
        calculateTotals(estimate, request);

        return estimateRepository.save(estimate);
    }

    @Transactional
    public Estimate updateEstimate(Long estimateId, EstimateRequest request) {
        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new RuntimeException("Estimate not found"));
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!customer.getBusiness().getId().equals(estimate.getBusiness().getId())) {
            throw new IllegalStateException("Customer does not belong to this business.");
        }

        estimate.setCustomer(customer);

        estimate.getItems().clear();
        estimate.getNotes().clear();
        estimateRepository.flush();

        processEstimateItems(estimate, request);
        processEstimateNotes(estimate, request);
        calculateTotals(estimate, request);

        return estimateRepository.save(estimate);
    }

    @Transactional
    public void deleteEstimate(Long estimateId) {
        if (!estimateRepository.existsById(estimateId)) {
            throw new RuntimeException("Estimate not found");
        }
        estimateRepository.deleteById(estimateId);
    }

    private void processEstimateItems(Estimate estimate, EstimateRequest request) {
        request.getItems().forEach(itemRequest -> {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemRequest.getProductId()));

            if (!product.getCategory().getBusiness().getId().equals(estimate.getBusiness().getId())) {
                throw new IllegalStateException("Product " + product.getName() + " does not belong to this business.");
            }

            EstimateItem item = new EstimateItem();
            item.setEstimate(estimate);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setUnit(product.getUnit());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice() != null ? itemRequest.getUnitPrice() : product.getPrice());
            item.setDiscount(itemRequest.getDiscount() != null ? itemRequest.getDiscount() : BigDecimal.ZERO);
            item.setSubtotal(item.getUnitPrice().multiply(item.getQuantity()));
            estimate.getItems().add(item);
        });
    }

    private void processEstimateNotes(Estimate estimate, EstimateRequest request) {
        if (request.getNotes() != null) {
            request.getNotes().forEach(noteRequest -> {
                EstimateNote note = new EstimateNote();
                note.setEstimate(estimate);
                note.setNote(noteRequest.getNote());
                estimate.getNotes().add(note);
            });
        }
    }

    private void calculateTotals(Estimate estimate, EstimateRequest request) {
        BigDecimal totalBeforeDiscount = estimate.getItems().stream().map(EstimateItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalItemDiscount = estimate.getItems().stream().map(EstimateItem::getDiscount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overallDiscount = request.getTotalDiscount() != null ? request.getTotalDiscount() : BigDecimal.ZERO;
        BigDecimal totalDiscount = totalItemDiscount.add(overallDiscount);
        BigDecimal taxAmount = request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal finalTotal = totalBeforeDiscount.subtract(totalDiscount).add(taxAmount);

        estimate.setTotalBeforeDiscount(totalBeforeDiscount);
        estimate.setTotalDiscount(totalDiscount);
        estimate.setTaxAmount(taxAmount);
        estimate.setFinalTotal(finalTotal);
    }

    private String generateEstimateNumber() {
        return "EST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}