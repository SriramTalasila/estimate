package com.talasila.estimate.controller;

import com.talasila.estimate.dto.CategoryDiscountResponse;
import com.talasila.estimate.dto.EstimateRequest;
import com.talasila.estimate.dto.EstimateResponse;
import com.talasila.estimate.dto.EstimateItemResponse;
import com.talasila.estimate.dto.EstimateNoteResponse;
import com.talasila.estimate.dto.ShareLinkResponse;
import com.talasila.estimate.model.Estimate;
import com.talasila.estimate.security.JwtUtils;
import com.talasila.estimate.service.EstimateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Tag(name = "Estimate Management", description = "APIs for creating, updating, and deleting estimates.")
@RestController
@RequestMapping("/api")
public class EstimateController {

    private final EstimateService estimateService;
    private final JwtUtils jwtUtils;

    public EstimateController(EstimateService estimateService, JwtUtils jwtUtils) {
        this.estimateService = estimateService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/business/{businessId}/estimates")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'EMPLOYEE') and @businessSecurityService.isUserInBusiness(authentication, #businessId)")
    @Operation(summary = "Create a new estimate for a business")
    public ResponseEntity<EstimateResponse> createEstimate(@PathVariable Long businessId, @Valid @RequestBody EstimateRequest estimateRequest) {
        Estimate createdEstimate = estimateService.createEstimate(businessId, estimateRequest);
        return new ResponseEntity<>(mapToEstimateResponse(createdEstimate), HttpStatus.CREATED);
    }

    @GetMapping("/business/{businessId}/estimates")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'EMPLOYEE') and @businessSecurityService.isUserInBusiness(authentication, #businessId)")
    @Operation(summary = "Get all estimates for a business")
    public ResponseEntity<List<EstimateResponse>> getEstimatesByBusiness(@PathVariable Long businessId) {
        List<Estimate> estimates = estimateService.getEstimatesByBusiness(businessId);
        List<EstimateResponse> response = estimates.stream()
                .map(this::mapToEstimateResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/business/{businessId}/estimates/stats")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'EMPLOYEE') and @businessSecurityService.isUserInBusiness(authentication, #businessId)")
    @Operation(summary = "Get sales statistics for a business (today, this month, this year)")
    public ResponseEntity<Map<String, BigDecimal>> getEstimateStats(@PathVariable Long businessId) {
        Map<String, BigDecimal> stats = estimateService.getEstimateStats(businessId);
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/estimates/{estimateId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'EMPLOYEE') and @businessSecurityService.isOwnerOfEstimate(authentication, #estimateId)")
    @Operation(summary = "Update an existing estimate")
    public ResponseEntity<EstimateResponse> updateEstimate(@PathVariable Long estimateId, @Valid @RequestBody EstimateRequest estimateRequest) {
        Estimate updatedEstimate = estimateService.updateEstimate(estimateId, estimateRequest);
        return ResponseEntity.ok(mapToEstimateResponse(updatedEstimate));
    }

    @DeleteMapping("/estimates/{estimateId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'EMPLOYEE') and @businessSecurityService.isOwnerOfEstimate(authentication, #estimateId)")
    @Operation(summary = "Delete an existing estimate")
    public ResponseEntity<Void> deleteEstimate(@PathVariable Long estimateId) {
        estimateService.deleteEstimate(estimateId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/estimates/{estimateId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'EMPLOYEE') and @businessSecurityService.isOwnerOfEstimate(authentication, #estimateId)")
    @Operation(summary = "Generate PDF for an existing estimate")
    public ResponseEntity<byte[]> generateEstimatePdf(@PathVariable Long estimateId) {
        byte[] pdfBytes = estimateService.generateEstimatePdf(estimateId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "estimate-" + estimateId + ".pdf");
        
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/estimates/{estimateId}/share-link")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'EMPLOYEE') and @businessSecurityService.isOwnerOfEstimate(authentication, #estimateId)")
    @Operation(summary = "Generate public share link for estimate PDF")
    public ResponseEntity<ShareLinkResponse> getEstimateShareLink(@PathVariable Long estimateId) {
        String token = jwtUtils.generateEstimateShareToken(estimateId);
        String shareUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/public/estimates/{estimateId}/pdf")
                .queryParam("token", token)
                .buildAndExpand(estimateId)
                .toUriString();

        return ResponseEntity.ok(new ShareLinkResponse(shareUrl));
    }

    private EstimateResponse mapToEstimateResponse(Estimate estimate) {
        EstimateResponse response = new EstimateResponse();
        response.setId(estimate.getId());
        response.setEstimateNumber(estimate.getEstimateNumber());
        if (estimate.getCustomer() != null) {
            response.setCustomerId(estimate.getCustomer().getId());
            response.setCustomerName(estimate.getCustomer().getName());
            response.setCustomerPhone(estimate.getCustomer().getPhone());
        }
        response.setTotalBeforeDiscount(estimate.getTotalBeforeDiscount());
        response.setTotalDiscount(estimate.getTotalDiscount());
        response.setTaxAmount(estimate.getTaxAmount());
        response.setAdditionalDiscount(estimate.getAdditionalDiscount());
        response.setAdditionalDiscountType(estimate.getAdditionalDiscountType());
        response.setFinalTotal(estimate.getFinalTotal());
        response.setCreatedAt(estimate.getCreatedAt());

        if (estimate.getItems() != null) {
            response.setItems(estimate.getItems().stream().map(item -> {
                EstimateItemResponse itemResponse = new EstimateItemResponse();
                itemResponse.setId(item.getId());
                if (item.getProduct() != null) {
                    itemResponse.setProductId(item.getProduct().getId());
                }
                itemResponse.setProductName(item.getProductName());
                itemResponse.setUnit(item.getUnit());
                itemResponse.setQuantity(item.getQuantity());
                itemResponse.setUnitPrice(item.getUnitPrice());
                itemResponse.setDiscount(item.getDiscount());
                itemResponse.setSubtotal(item.getSubtotal());
                return itemResponse;
            }).collect(Collectors.toList()));
        }

        if (estimate.getNotes() != null) {
            response.setNotes(estimate.getNotes().stream().map(note -> {
                EstimateNoteResponse noteResponse = new EstimateNoteResponse();
                noteResponse.setId(note.getId());
                noteResponse.setNote(note.getNote());
                return noteResponse;
            }).collect(Collectors.toList()));
        }

        if (estimate.getCategoryDiscounts() != null) {
            response.setCategoryDiscounts(estimate.getCategoryDiscounts().stream().map(cd -> {
                CategoryDiscountResponse cdResponse = new CategoryDiscountResponse();
                cdResponse.setId(cd.getId());
                cdResponse.setCategoryId(cd.getCategoryId());
                cdResponse.setDiscount(cd.getDiscount());
                cdResponse.setDiscountType(cd.getDiscountType());
                return cdResponse;
            }).collect(Collectors.toList()));
        }

        return response;
    }
}
