package com.vikrant.urlshortener.exception;

public class ErrorResponse {
    private int status;
    private String message;
    public ErrorResponse(int status,String message){
        this.message = message;
        this.status = status;
    }
    public int getStatus(){
        return status;
    }
    public String getMessage(){
        return message;
    }
}
