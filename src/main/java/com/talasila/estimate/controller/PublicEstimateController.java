package com.talasila.estimate.controller;

import com.talasila.estimate.security.JwtUtils;
import com.talasila.estimate.service.EstimateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public Estimate Sharing", description = "Public access to a shared estimate PDF using a signed token.")
@RestController
public class PublicEstimateController {

    private final EstimateService estimateService;
    private final JwtUtils jwtUtils;

    public PublicEstimateController(EstimateService estimateService, JwtUtils jwtUtils) {
        this.estimateService = estimateService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping(value = "/public/estimates/{estimateId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Download shared estimate PDF")
    public ResponseEntity<byte[]> getSharedEstimatePdf(@PathVariable Long estimateId,
                                                       @RequestParam String token) {
        if (!jwtUtils.validateEstimateShareToken(token, estimateId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        byte[] pdfBytes = estimateService.generateEstimatePdf(estimateId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "estimate-" + estimateId + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
