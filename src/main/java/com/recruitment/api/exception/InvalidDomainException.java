package com.recruitment.api.exception;

public class InvalidDomainException extends RuntimeException {
    public InvalidDomainException() {
        super("Email must be a company address ending in @alkhorayef.com");
    }
}
