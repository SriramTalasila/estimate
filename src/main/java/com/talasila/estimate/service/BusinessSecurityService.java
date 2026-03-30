package com.talasila.estimate.service;

import com.talasila.estimate.model.Category;
import com.talasila.estimate.model.Estimate;
import com.talasila.estimate.model.Product;
import com.talasila.estimate.model.User;
import com.talasila.estimate.repository.CategoryRepository;
import com.talasila.estimate.repository.EstimateRepository;
import com.talasila.estimate.repository.ProductRepository;
import com.talasila.estimate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("businessSecurityService")
public class BusinessSecurityService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EstimateRepository estimateRepository;

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
    }

    public boolean isBusinessOwner(Authentication authentication, Long businessId) {
        User user = getAuthenticatedUser(authentication);
        return user != null && user.getBusiness() != null && user.getBusiness().getId().equals(businessId);
    }

    public boolean isUserInBusiness(Authentication authentication, Long businessId) {
        User user = getAuthenticatedUser(authentication);
        return user != null && user.getBusiness() != null && user.getBusiness().getId().equals(businessId);
    }

    public boolean isOwnerOfCategory(Authentication authentication, Long categoryId) {
        User user = getAuthenticatedUser(authentication);
        if (user == null || user.getBusiness() == null) {
            return false;
        }
        Category category = categoryRepository.findById(categoryId).orElse(null);
        return category != null && category.getBusiness() != null && user.getBusiness().getId().equals(category.getBusiness().getId());
    }

    public boolean isOwnerOfProduct(Authentication authentication, Long productId) {
        User user = getAuthenticatedUser(authentication);
        if (user == null || user.getBusiness() == null) {
            return false;
        }
        Product product = productRepository.findById(productId).orElse(null);
        return product != null && product.getCategory() != null && product.getCategory().getBusiness() != null
                && user.getBusiness().getId().equals(product.getCategory().getBusiness().getId());
    }

    public boolean isOwnerOfEstimate(Authentication authentication, Long estimateId) {
        User user = getAuthenticatedUser(authentication);
        if (user == null || user.getBusiness() == null) {
            return false;
        }
        Estimate estimate = estimateRepository.findById(estimateId).orElse(null);
        return estimate != null && estimate.getBusiness() != null && user.getBusiness().getId().equals(estimate.getBusiness().getId());
    }
}