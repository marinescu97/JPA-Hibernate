package com.spring.JPAHibernate.exception;

public class UniqueFieldException extends RuntimeException {
    public UniqueFieldException(String message) {
        super(message);
    }
}
