package com.vikrant.urlshortener.exception;

public class InvalidExpirationException extends RuntimeException {

    public InvalidExpirationException(String message) {
        super(message);
    }
}