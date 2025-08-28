package com.michael.ragdemo.service;

import com.michael.ragdemo.dto.ProductDetails;
import com.michael.ragdemo.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTools {
    private final ProductService productService;

    @Tool(description = "Get Product details by name")
    public ProductDetails getProductDetails(String productName) {
        log.info("Get Product details by name: {}", productName);

        Product product = productService.findProductByName(productName);
        if (product != null) {
            return new ProductDetails(product.getId(), product.getName(), product.getPrice(), product.getQuantity());
        } else {
            return new ProductDetails(0, "Not Found", 0, 0);
        }
    }
}
