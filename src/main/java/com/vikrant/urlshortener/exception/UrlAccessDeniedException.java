package com.vikrant.urlshortener.exception;

public class UrlAccessDeniedException extends RuntimeException {

    public UrlAccessDeniedException(String message) {
        super(message);
    }
}