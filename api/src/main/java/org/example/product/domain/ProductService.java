package org.example.product.domain;

import lombok.RequiredArgsConstructor;
import org.example.product.dto.ProductDTO;
import org.example.product.exception.ProductAlreadyExistsException;
import org.example.product.exception.ProductNotFoundException;
import org.example.product.instrastructure.ProductRepository;
import org.example.utils.ProductMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Product getProductByProductId(UUID productId) throws ProductNotFoundException {
        return productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
    }

    public Product getProductByProductName(String productName) throws ProductNotFoundException {
        Product prod = productRepository.findProductByName(productName);
        if (prod == null) throw new ProductNotFoundException();
        return prod;
    }

    public Product updateProductName(UUID id, String name) throws ProductNotFoundException, ProductAlreadyExistsException {
        Product existingProd = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);
        if(productRepository.findProductByName(name) != null) throw new ProductAlreadyExistsException();

        existingProd.setName(name);
        return productRepository.save(existingProd);
    }

    public Product updateProductStock(UUID id, Integer newStock) throws ProductNotFoundException {
        Product existingProd = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);
        existingProd.setStock(newStock);
        return productRepository.save(existingProd);
    }

    public Product updateProductPrice(UUID id, BigDecimal newPrice) throws ProductNotFoundException {
        Product existingProd = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);
        existingProd.setPrice(newPrice);
        return productRepository.save(existingProd);
    }

    public Product createProduct(ProductDTO productDTO) throws ProductAlreadyExistsException {
        Product prod = productRepository.findProductByName(productDTO.name());
        if (prod != null) throw new ProductAlreadyExistsException();

        Product product = productMapper.toEntity(productDTO);
        return productRepository.save(product);
    }

    public void deleteProduct(UUID id) throws ProductNotFoundException {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);
        productRepository.delete(existingProduct);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAllByOrderByNameAsc();
    }
}
