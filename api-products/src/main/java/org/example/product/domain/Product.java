package org.example.product.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(length = 100, nullable = false,  unique = true)
    private String name;

    @Min(0)
    @Column(nullable = false)
    private Integer stock;

    @DecimalMin("1.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
