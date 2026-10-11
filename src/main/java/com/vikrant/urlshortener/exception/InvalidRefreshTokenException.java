package com.vikrant.urlshortener.exception;

public class InvalidRefreshTokenException
        extends RuntimeException {

    public InvalidRefreshTokenException(
            String message
    ) {
        super(message);
    }
}