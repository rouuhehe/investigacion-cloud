package org.example.product.exception;

public class ProductAlreadyExistsException extends Exception{
    public ProductAlreadyExistsException() {
        super("Product already exists!");
    }
}
