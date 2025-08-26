package org.example.product.instrastructure;

import org.example.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Product findProductByName(String name);
    List<Product> findAllByOrderByNameAsc();

}
