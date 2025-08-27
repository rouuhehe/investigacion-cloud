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

    private void ensureNameIsUnique(String name) throws ProductAlreadyExistsException {
        if (productRepository.findProductByName(name) != null) {
            throw new ProductAlreadyExistsException();
        }
    }

    public Product getProductById(UUID productId) throws ProductNotFoundException {
        return productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
    }

    public Product getProductByName(String productName) throws ProductNotFoundException {
        Product prod = productRepository.findProductByName(productName);
        if (prod == null) throw new ProductNotFoundException();
        return prod;
    }

    public Product updateProductName(UUID id, String name) throws ProductNotFoundException, ProductAlreadyExistsException {
        Product existingProd = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        ensureNameIsUnique(name);
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

    public Product updateProduct(UUID id, ProductDTO newProduct) throws ProductNotFoundException, ProductAlreadyExistsException {
        Product existingProd = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        existingProd.setPrice(newProduct.price());
        existingProd.setStock(newProduct.stock());
        ensureNameIsUnique(newProduct.name());
        existingProd.setName(newProduct.name());

        return productRepository.save(existingProd);
    }

    public Product createProduct(ProductDTO productDTO) throws ProductAlreadyExistsException {
        ensureNameIsUnique(productDTO.name());
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
