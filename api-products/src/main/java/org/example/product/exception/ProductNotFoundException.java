package org.example.product.exception;

public class ProductNotFoundException extends Exception{
    public ProductNotFoundException() {
        super("Product not found!");
    }
}
