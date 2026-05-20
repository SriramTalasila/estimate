package com.talasila.estimate.service;

import com.talasila.estimate.dto.CategoryDiscountRequest;
import com.talasila.estimate.dto.EstimateItemRequest;
import com.talasila.estimate.dto.EstimateNoteRequest;
import com.talasila.estimate.dto.EstimateRequest;
import com.talasila.estimate.model.*;
import com.talasila.estimate.repository.*;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EstimateService {

    private final EstimateRepository estimateRepository;
    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public EstimateService(EstimateRepository estimateRepository, BusinessRepository businessRepository,
            CustomerRepository customerRepository, ProductRepository productRepository) {
        this.estimateRepository = estimateRepository;
        this.businessRepository = businessRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Estimate createEstimate(Long businessId, EstimateRequest estimateRequest) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found with id: " + businessId));

        Customer customer = findOrCreateCustomer(estimateRequest, business);

        Estimate estimate = new Estimate();
        estimate.setBusiness(business);
        estimate.setCustomer(customer);

        updateEstimateFromRequest(estimate, estimateRequest);

        return estimateRepository.save(estimate);
    }

    @Transactional
    public Estimate updateEstimate(Long estimateId, EstimateRequest estimateRequest) {
        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new RuntimeException("Estimate not found with id: " + estimateId));

        Customer customer = findOrCreateCustomer(estimateRequest, estimate.getBusiness());
        estimate.setCustomer(customer);

        // Clear existing items and notes to replace them
        estimate.getItems().clear();
        estimate.getNotes().clear();
        estimate.getCategoryDiscounts().clear();

        updateEstimateFromRequest(estimate, estimateRequest);

        return estimateRepository.save(estimate);
    }

    @Transactional
    public void deleteEstimate(Long estimateId) {
        estimateRepository.deleteById(estimateId);
    }

    @Transactional(readOnly = true)
    public List<Estimate> getEstimatesByBusiness(Long businessId) {
        return estimateRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
    }

    @Transactional(readOnly = true)
    public Page<Estimate> getEstimatesByBusiness(Long businessId, int page, int size, String search) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), 100);
        return estimateRepository.findByBusinessIdOrderByCreatedAtDesc(
                businessId,
                PageRequest.of(normalizedPage, normalizedSize));
    }

    @Transactional(readOnly = true)
    public byte[] generateEstimatePdf(Long estimateId) {
        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new RuntimeException("Estimate not found with id: " + estimateId));

        try {
            ClassPathResource resource = new ClassPathResource("templates/pdf-Template.html");
            String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // Branding Info
            html = html.replace("&lt;Company Name&gt;", escapeHtml(estimate.getBusiness().getShopName() != null ? estimate.getBusiness().getShopName() : "Business"));
            html = html.replace("&lt;123 Street Address, City, State&gt;", escapeHtml(estimate.getBusiness().getShopAddress() != null ? estimate.getBusiness().getShopAddress() : ""));
            html = html.replace("&lt;Website / Email Address&gt;", escapeHtml(estimate.getBusiness().getPhone() != null ? estimate.getBusiness().getPhone() : ""));
            html = html.replace("{{businessLogo}}", buildBusinessLogoHtml(estimate.getBusiness()));

            // Customer Info
            html = html.replace("{{customerName}}", escapeHtml(estimate.getCustomer() != null && estimate.getCustomer().getName() != null ? estimate.getCustomer().getName() : "Walk-in Customer"));
            html = html.replace("{{phoneNumber}}", escapeHtml(estimate.getCustomer() != null && estimate.getCustomer().getPhone() != null ? estimate.getCustomer().getPhone() : "N/A"));
            html = html.replace("{{estimateNumber}}", escapeHtml(estimate.getEstimateNumber() != null ? estimate.getEstimateNumber() : "EST-00" + estimate.getId()));

            // Dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            html = html.replace("{{date}}", estimate.getCreatedAt() != null ? estimate.getCreatedAt().format(formatter) : LocalDate.now().format(formatter));
            html = html.replace("{{expiryDate}}", estimate.getCreatedAt() != null ? estimate.getCreatedAt().plusDays(30).format(formatter) : LocalDate.now().plusDays(30).format(formatter));

            // Line Items Array parsing
            StringBuilder rowsHtml = new StringBuilder();
            if (estimate.getItems() != null) {
                Map<Long, EstimateCategoryDiscount> categoryDiscountMap = new HashMap<>();
                if (estimate.getCategoryDiscounts() != null) {
                    for (EstimateCategoryDiscount cd : estimate.getCategoryDiscounts()) {
                        categoryDiscountMap.put(cd.getCategoryId(), cd);
                    }
                }

                for (EstimateItem item : estimate.getItems()) {
                    BigDecimal displayDiscount = item.getDiscount();
                    String displayDiscountType = item.getDiscountType();

                    if (!item.isOverrideCategoryDiscount() && item.getProduct() != null && item.getProduct().getCategory() != null) {
                        EstimateCategoryDiscount catDiscount = categoryDiscountMap.get(item.getProduct().getCategory().getId());
                        if (catDiscount != null) {
                            displayDiscount = catDiscount.getDiscount();
                            displayDiscountType = catDiscount.getDiscountType();
                        }
                    }

                    BigDecimal itemTotal = item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO;
                    String discountStr = "-";
                    if (displayDiscount != null && displayDiscount.compareTo(BigDecimal.ZERO) > 0) {
                        discountStr = displayDiscount + ("PERCENTAGE".equals(displayDiscountType) ? "%" : " Flat");

                        if ("PERCENTAGE".equals(displayDiscountType)) {
                            BigDecimal discountAmount = itemTotal.multiply(displayDiscount).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                            itemTotal = itemTotal.subtract(discountAmount);
                        } else {
                            itemTotal = itemTotal.subtract(displayDiscount);
                        }

                        if (itemTotal.compareTo(BigDecimal.ZERO) < 0) {
                            itemTotal = BigDecimal.ZERO;
                        }
                    }

                    String row = "<tr>" +
                            "<td>" + escapeHtml(item.getProductName() != null ? item.getProductName() : "") + "</td>" +
                            "<td class=\"text-center\">" + item.getQuantity() + "</td>" +
                            "<td class=\"text-right\">" + item.getUnitPrice() + "</td>" +
                            "<td class=\"text-right\">" + discountStr + "</td>" +
                            "<td class=\"text-right\">" + itemTotal + "</td>" +
                            "</tr>";
                    rowsHtml.append(row);
                }
            }
            html = html.replaceAll("(?s)<tr>\\s*<td>\\{\\{itemDescription\\}\\}</td>.*?</tr>", java.util.regex.Matcher.quoteReplacement(rowsHtml.toString()));

            // Notes Array
            StringBuilder notesHtml = new StringBuilder();
            if (estimate.getNotes() != null) {
                for (EstimateNote note : estimate.getNotes()) {
                    notesHtml.append(escapeHtml(note.getNote())).append("<br/>");
                }
            }
            html = html.replace("{{notes}}", notesHtml.length() > 0 ? notesHtml.toString() : "None");

            // Summaries
            html = html.replace("{{subtotal}}", estimate.getTotalBeforeDiscount() != null ? estimate.getTotalBeforeDiscount().toString() : "0.00");
            html = html.replace("{{itemDiscountsTotal}}", estimate.getTotalDiscount() != null ? estimate.getTotalDiscount().toString() : "0.00");
            html = html.replace("{{overallDiscount}}", estimate.getAdditionalDiscount() != null ? estimate.getAdditionalDiscount().toString() : "0.00");
            html = html.replace("{{taxAmount}}", estimate.getTaxAmount() != null ? estimate.getTaxAmount().toString() : "0.00");
            html = html.replace("{{totalAmount}}", estimate.getFinalTotal() != null ? estimate.getFinalTotal().toString() : "0.00");

            // Convert finalized HTML text to PDF
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, "");
                builder.toStream(os);
                builder.run();
                return os.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getEstimateStats(Long businessId) {
        Map<String, BigDecimal> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Today's stats
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        stats.put("today", estimateRepository.sumFinalTotalByBusinessIdAndCreatedAtBetween(businessId, startOfToday,
                startOfTomorrow));

        // This month's stats
        YearMonth currentMonth = YearMonth.from(today);
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime startOfNextMonth = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        stats.put("month", estimateRepository.sumFinalTotalByBusinessIdAndCreatedAtBetween(businessId, startOfMonth,
                startOfNextMonth));

        // This year's stats
        int currentYear = today.getYear();
        LocalDateTime startOfYear = LocalDate.of(currentYear, 1, 1).atStartOfDay();
        LocalDateTime startOfNextYear = LocalDate.of(currentYear + 1, 1, 1).atStartOfDay();
        stats.put("year", estimateRepository.sumFinalTotalByBusinessIdAndCreatedAtBetween(businessId, startOfYear,
                startOfNextYear));

        return stats;
    }

    private Customer findOrCreateCustomer(EstimateRequest estimateRequest, Business business) {
        if (estimateRequest.getCustomerId() != null) {
            return customerRepository.findById(estimateRequest.getCustomerId())
                    .orElseThrow(() -> new RuntimeException(
                            "Customer not found with id: " + estimateRequest.getCustomerId()));
        }

        if (StringUtils.hasText(estimateRequest.getCustomerPhone())) {
            return customerRepository.findByPhoneAndBusinessId(estimateRequest.getCustomerPhone(), business.getId())
                    .orElseGet(() -> {
                        Customer newCustomer = new Customer();
                        newCustomer.setBusiness(business);
                        newCustomer.setName(estimateRequest.getCustomerName());
                        newCustomer.setPhone(estimateRequest.getCustomerPhone());
                        return customerRepository.save(newCustomer);
                    });
        }

        // This is a temporary solution for walk-in customers without a phone number,
        // assuming the 'phone' column has a UNIQUE constraint.
        Customer walkinCustomer = new Customer();
        walkinCustomer.setBusiness(business);
        walkinCustomer
                .setName(StringUtils.hasText(estimateRequest.getCustomerName()) ? estimateRequest.getCustomerName()
                        : "Walk-in Customer");
        walkinCustomer.setPhone("N/A-" + System.currentTimeMillis()); // Unique phone to avoid constraint issues
        return customerRepository.save(walkinCustomer);
    }

    private void updateEstimateFromRequest(Estimate estimate, EstimateRequest estimateRequest) {
        List<Long> productIds = estimateRequest.getItems().stream().map(EstimateItemRequest::getProductId)
                .collect(Collectors.toList());
        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        BigDecimal totalBeforeDiscount = BigDecimal.ZERO;

        for (EstimateItemRequest itemRequest : estimateRequest.getItems()) {
            Product product = productMap.get(itemRequest.getProductId());
            if (product == null)
                throw new RuntimeException("Product not found with id: " + itemRequest.getProductId());

            EstimateItem item = new EstimateItem();
            item.setEstimate(estimate);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setUnit(product.getUnit());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice() != null ? itemRequest.getUnitPrice() : product.getPrice());
            item.setDiscount(itemRequest.getDiscount());
            item.setDiscountType(itemRequest.getDiscountType());
            item.setOverrideCategoryDiscount(itemRequest.isOverrideCategoryDiscount());

            BigDecimal subtotal = item.getUnitPrice().multiply(item.getQuantity());
            item.setSubtotal(subtotal);
            totalBeforeDiscount = totalBeforeDiscount.add(subtotal);

            estimate.getItems().add(item);
        }

        if (estimateRequest.getNotes() != null) {
            for (EstimateNoteRequest noteRequest : estimateRequest.getNotes()) {
                EstimateNote note = new EstimateNote();
                note.setEstimate(estimate);
                note.setNote(noteRequest.getNote());
                estimate.getNotes().add(note);
            }
        }

        if (estimateRequest.getCategoryDiscounts() != null) {
            for (CategoryDiscountRequest cdRequest : estimateRequest.getCategoryDiscounts()) {
                EstimateCategoryDiscount categoryDiscount = new EstimateCategoryDiscount();
                categoryDiscount.setEstimate(estimate);
                categoryDiscount.setCategoryId(cdRequest.getCategoryId());
                categoryDiscount.setDiscount(cdRequest.getDiscount());
                categoryDiscount.setDiscountType(cdRequest.getDiscountType());
                estimate.getCategoryDiscounts().add(categoryDiscount);
            }
        }

        estimate.setTotalBeforeDiscount(totalBeforeDiscount);
        estimate.setTotalDiscount(estimateRequest.getTotalDiscount());
        estimate.setTaxAmount(estimateRequest.getTaxAmount());
        estimate.setFinalTotal(estimateRequest.getFinalAmount());
        estimate.setAdditionalDiscount(estimateRequest.getAdditionalDiscount());
        estimate.setAdditionalDiscountType(estimateRequest.getAdditionalDiscountType());
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return org.springframework.web.util.HtmlUtils.htmlEscape(text).replace("\n", "<br/>");
    }

    private String buildBusinessLogoHtml(Business business) {
        if (business == null || business.getLogo() == null || business.getLogo().length == 0) {
            return "<div class=\"logo-placeholder\">NO LOGO</div>";
        }

        String logoDataUrl = new String(business.getLogo(), StandardCharsets.UTF_8);
        if (!StringUtils.hasText(logoDataUrl)) {
            return "<div class=\"logo-placeholder\">NO LOGO</div>";
        }
        log.info("logoDataUrl: " + logoDataUrl);

        return "<img class=\"logo-image\" src=\"" + escapeHtmlAttribute(logoDataUrl) + "\" alt=\"Business logo\" />";
    }

    private String escapeHtmlAttribute(String text) {
        if (text == null) {
            return "";
        }
        return org.springframework.web.util.HtmlUtils.htmlEscape(text);
    }
}
