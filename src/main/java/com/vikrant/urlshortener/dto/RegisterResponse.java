package com.vikrant.urlshortener.dto;

public class RegisterResponse {
    private Long id;
    private String email;

    public RegisterResponse(Long id , String email){
        this.email = email;
        this.id = id;
    }
    public Long getId(){
        return id;
    }

    public String getEmail(){
        return email;
    }

}
