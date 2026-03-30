package com.talasila.estimate.controller;

import com.talasila.estimate.dto.EstimateRequest;
import com.talasila.estimate.dto.EstimateResponse;
import com.talasila.estimate.dto.EstimateItemResponse;
import com.talasila.estimate.dto.EstimateNoteResponse;
import com.talasila.estimate.model.Estimate;
import com.talasila.estimate.service.EstimateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@Tag(name = "Estimate Management", description = "APIs for creating, updating, and deleting estimates.")
@RestController
@RequestMapping("/api")
public class EstimateController {

    private final EstimateService estimateService;

    public EstimateController(EstimateService estimateService) {
        this.estimateService = estimateService;
    }

    @PostMapping("/business/{businessId}/estimates")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'EMPLOYEE') and @businessSecurityService.isUserInBusiness(authentication, #businessId)")
    @Operation(summary = "Create a new estimate for a business")
    public ResponseEntity<EstimateResponse> createEstimate(@PathVariable Long businessId, @Valid @RequestBody EstimateRequest estimateRequest) {
        Estimate createdEstimate = estimateService.createEstimate(businessId, estimateRequest);
        return new ResponseEntity<>(mapToEstimateResponse(createdEstimate), HttpStatus.CREATED);
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

    private EstimateResponse mapToEstimateResponse(Estimate estimate) {
        EstimateResponse response = new EstimateResponse();
        response.setId(estimate.getId());
        response.setEstimateNumber(estimate.getEstimateNumber());
        if (estimate.getCustomer() != null) {
            response.setCustomerId(estimate.getCustomer().getId());
        }
        response.setTotalBeforeDiscount(estimate.getTotalBeforeDiscount());
        response.setTotalDiscount(estimate.getTotalDiscount());
        response.setTaxAmount(estimate.getTaxAmount());
        response.setFinalTotal(estimate.getFinalTotal());
        //response.setCreatedAt(estimate.);

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

        return response;
    }
}