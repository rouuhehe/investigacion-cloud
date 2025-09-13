package org.example.product.application;

import lombok.RequiredArgsConstructor;
import org.example.product.domain.ProductService;
import org.example.product.dto.ProductDTO;
import org.example.product.exception.ProductAlreadyExistsException;
import org.example.product.exception.ProductNotFoundException;
import org.example.utils.ProductMapper;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping("/all")
    public List<ProductDTO> listAllProducts() {
        return productService.getAllProducts()
                .stream()
                .map(productMapper::toDto)
                .toList();
    }


    @GetMapping("/uuid/{id}")
    public ProductDTO getProductById(@PathVariable UUID id)
            throws ProductNotFoundException {
        return productMapper.toDto(productService.getProductById(id));
    }

    @GetMapping("/name/{name}")
    public ProductDTO getProductByName(@PathVariable String name)
            throws ProductNotFoundException {
        return productMapper.toDto(productService.getProductByName(name));
    }

    @PostMapping("/new")
    public ProductDTO createProduct(@RequestBody ProductDTO productDTO)
            throws ProductAlreadyExistsException {
        return productMapper.toDto(productService.createProduct(productDTO));
    }

    @PutMapping("/update/{id}/price")
    public ProductDTO updateProductPrice(@PathVariable UUID id,
                                         @RequestParam BigDecimal price)
            throws ProductNotFoundException {
        return productMapper.toDto(productService.updateProductPrice(id, price));
    }

    @PutMapping("/update/{id}/stock")
    public ProductDTO updateProductStock(@PathVariable UUID id,
                                         @RequestParam Integer stock)
            throws ProductNotFoundException {
        return productMapper.toDto(productService.updateProductStock(id, stock));
    }

    @PutMapping("/update/{id}/name")
    public ProductDTO updateProductName(@PathVariable UUID id,
                                        @RequestParam String name)
            throws ProductNotFoundException, ProductAlreadyExistsException {
        return productMapper.toDto(productService.updateProductName(id, name));
    }

    @PutMapping("/update/{id}")
    public ProductDTO updateProduct(@PathVariable UUID id,
                                      @RequestBody ProductDTO productDTO)
            throws ProductNotFoundException, ProductAlreadyExistsException {
        return productMapper.toDto(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable UUID id)
            throws ProductNotFoundException {
        productService.deleteProduct(id);
    }

}
