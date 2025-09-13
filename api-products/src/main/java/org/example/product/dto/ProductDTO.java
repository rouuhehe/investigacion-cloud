package org.example.product.dto;

import java.math.BigDecimal;

public record ProductDTO(
        String name,
        Integer stock,
        BigDecimal price
){}
