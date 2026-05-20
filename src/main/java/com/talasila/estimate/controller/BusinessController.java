package com.talasila.estimate.controller;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.talasila.estimate.dto.BusinessRequest;
import com.talasila.estimate.dto.CategoryRequest;
import com.talasila.estimate.dto.ProductRequest;
import com.talasila.estimate.dto.BusinessResponse;
import com.talasila.estimate.dto.CategoryResponse;
import com.talasila.estimate.dto.ProductResponse;
import com.talasila.estimate.model.Business;
import com.talasila.estimate.model.Category;
import com.talasila.estimate.model.Product;
import com.talasila.estimate.service.BusinessService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Business Management", description = "APIs for creating and managing business, categories, and products. Restricted to Business Owners.")
@RestController
@RequestMapping("/api/manage")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    // --- Business Management ---

    @PostMapping("/business")
    @Operation(summary = "Create a new business", description = "Creates a new business profile and associates it with the currently authenticated business owner. A user can only be associated with one business.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or user already has a business"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not a Business Owner")
    })
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<BusinessResponse> createBusiness(@Valid @RequestBody BusinessRequest businessRequest) {
        Business createdBusiness = businessService.createBusiness(businessRequest);
        return ResponseEntity.ok(mapToBusinessResponse(createdBusiness));
    }

    @PutMapping("/business/{businessId}")
    @Operation(summary = "Update an existing business", description = "Updates the details of an existing business. The authenticated user must be the owner of this business.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business updated successfully"),
            @ApiResponse(responseCode = "404", description = "Business not found")
    })
    @PreAuthorize("hasRole('BUSINESS_OWNER') and @businessSecurityService.isBusinessOwner(authentication, #businessId)")
    public ResponseEntity<BusinessResponse> updateBusiness(@PathVariable Long businessId, @Valid @RequestBody BusinessRequest businessRequest) {
        Business updatedBusiness = businessService.updateBusiness(businessId, businessRequest);
        return ResponseEntity.ok(mapToBusinessResponse(updatedBusiness));
    }

    @GetMapping("/my-business")
    @Operation(summary = "Get the business of the logged-in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business found and returned"),
            @ApiResponse(responseCode = "404", description = "User is not associated with any business"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'EMPLOYEE')")
    public ResponseEntity<BusinessResponse> getMyBusiness() {
        Optional<Business> business = businessService.getBusinessForCurrentUser();
        return business.map(b -> ResponseEntity.ok(mapToBusinessResponse(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/business/{businessId}/categories")
    @Operation(summary = "Get all categories for a business")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'EMPLOYEE') and @businessSecurityService.isUserInBusiness(authentication, #businessId)")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByBusiness(@PathVariable Long businessId) {
        List<Category> categories = businessService.getCategoriesByBusiness(businessId);
        List<CategoryResponse> response = categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/business/{businessId}/categories")
    @Operation(summary = "Create a new category for a business")
    @PreAuthorize("hasRole('BUSINESS_OWNER') and @businessSecurityService.isBusinessOwner(authentication, #businessId)")
    public ResponseEntity<CategoryResponse> createCategory(@PathVariable Long businessId, @Valid @RequestBody CategoryRequest categoryRequest) {
        Category createdCategory = businessService.createCategory(businessId, categoryRequest);
        return ResponseEntity.ok(mapToCategoryResponse(createdCategory));
    }

    @PutMapping("/categories/{categoryId}")
    @Operation(summary = "Update an existing category")
    @PreAuthorize("hasRole('BUSINESS_OWNER') and @businessSecurityService.isOwnerOfCategory(authentication, #categoryId)")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long categoryId, @Valid @RequestBody CategoryRequest categoryRequest) {
        Category updatedCategory = businessService.updateCategory(categoryId, categoryRequest);
        return ResponseEntity.ok(mapToCategoryResponse(updatedCategory));
    }

    // --- Product Management ---

    @GetMapping("/categories/{categoryId}/products")
    @Operation(summary = "Get all products for a category")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'EMPLOYEE') and @businessSecurityService.isOwnerOfCategory(authentication, #categoryId)")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = businessService.getProductsByCategory(categoryId);
        List<ProductResponse> response = products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/categories/{categoryId}/products")
    @Operation(summary = "Create a new product under a category")
    @PreAuthorize("hasRole('BUSINESS_OWNER') and @businessSecurityService.isOwnerOfCategory(authentication, #categoryId)")
    public ResponseEntity<ProductResponse> createProduct(@PathVariable Long categoryId, @Valid @RequestBody ProductRequest productRequest) {
        Product createdProduct = businessService.createProduct(categoryId, productRequest);
        return ResponseEntity.ok(mapToProductResponse(createdProduct));
    }

    @PutMapping("/products/{productId}")
    @Operation(summary = "Update an existing product")
    @PreAuthorize("hasRole('BUSINESS_OWNER') and @businessSecurityService.isOwnerOfProduct(authentication, #productId)")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long productId, @Valid @RequestBody ProductRequest productRequest) {
        Product updatedProduct = businessService.updateProduct(productId, productRequest);
        return ResponseEntity.ok(mapToProductResponse(updatedProduct));
    }

    @DeleteMapping("/products/{productId}")
    @Operation(summary = "Update an existing product")
    @PreAuthorize("hasRole('BUSINESS_OWNER') and @businessSecurityService.isOwnerOfProduct(authentication, #productId)")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        businessService.deleteProduct(productId);
        return ResponseEntity.ok("Product deleted successfully. ");
    }

    private BusinessResponse mapToBusinessResponse(Business business) {
        return new BusinessResponse(
                business.getId(),
                business.getShopName(),
                business.getShopAddress(),
                business.getPhone(),
                business.getGstNumber(),
                business.getLogo() == null ? null : new String(business.getLogo(), StandardCharsets.UTF_8));
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }

    private ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getUnit(), product.getPrice());
    }
}
