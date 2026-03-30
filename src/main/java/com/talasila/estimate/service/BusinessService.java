package com.talasila.estimate.service;

import com.talasila.estimate.dto.BusinessRequest;
import com.talasila.estimate.dto.CategoryRequest;
import com.talasila.estimate.dto.ProductRequest;
import com.talasila.estimate.model.*;
import com.talasila.estimate.repository.*;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class BusinessService {

    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Business createBusiness(BusinessRequest businessRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        if (currentUser.getBusiness() != null) {
            throw new IllegalStateException("User is already associated with a business.");
        }

        Business business = new Business();
        business.setShopName(businessRequest.getShopName());
        business.setShopAddress(businessRequest.getShopAddress());
        business.setPhone(businessRequest.getPhone());
        business.setGstNumber(businessRequest.getGstNumber());

        Business savedBusiness = businessRepository.save(business);

        currentUser.setBusiness(savedBusiness);
        userRepository.save(currentUser);

        return savedBusiness;
    }

    @Transactional
    public Business updateBusiness(Long businessId, BusinessRequest businessRequest) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Error: Business not found."));

        business.setShopName(businessRequest.getShopName());
        business.setShopAddress(businessRequest.getShopAddress());
        business.setPhone(businessRequest.getPhone());
        business.setGstNumber(businessRequest.getGstNumber());

        return businessRepository.save(business);
    }

    @Transactional(readOnly = true)
    public Optional<Business> getBusinessForCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getBusiness);
    }

    @Transactional
    public Category createCategory(Long businessId, CategoryRequest categoryRequest) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Error: Business not found."));

        if(categoryRepository.findByNameAndBusinessId(categoryRequest.getName(), businessId).isPresent()){
            throw new IllegalStateException("Category with this name already exists for this business.");
        }

        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setBusiness(business);

        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> getCategoriesByBusiness(Long businessId) {
        return categoryRepository.findByBusinessId(businessId);
    }

    @Transactional
    public Category updateCategory(Long categoryId, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Error: Category not found."));

        categoryRepository.findByNameAndBusinessId(categoryRequest.getName(), category.getBusiness().getId())
                .ifPresent(existingCategory -> {
                    if (!existingCategory.getId().equals(categoryId)) {
                        throw new IllegalStateException("Another category with this name already exists for this business.");
                    }
                });

        category.setName(categoryRequest.getName());
        return categoryRepository.save(category);
    }

    @Transactional
    public Product createProduct(Long categoryId, ProductRequest productRequest) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Error: Category not found."));

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setUnit(productRequest.getUnit());
        product.setPrice(productRequest.getPrice());
        product.setCategory(category);
        return productRepository.save(product);
    }

    @Transactional
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public Product updateProduct(Long productId, ProductRequest productRequest) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Error: Product not found."));
        product.setName(productRequest.getName());
        product.setUnit(productRequest.getUnit());
        product.setPrice(productRequest.getPrice());
        return productRepository.save(product);
    }
}