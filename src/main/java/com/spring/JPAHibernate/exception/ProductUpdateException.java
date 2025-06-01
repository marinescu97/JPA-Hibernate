package com.spring.JPAHibernate.exception;

public class ProductUpdateException extends RuntimeException {
    public ProductUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
