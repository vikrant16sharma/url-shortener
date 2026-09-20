package com.vikrant.urlshortener.controller;
import com.vikrant.urlshortener.Services.UrlShortenerService;
import com.vikrant.urlshortener.dto.CreateUrlRequest;
import com.vikrant.urlshortener.dto.CreateUrlResponse;
import com.vikrant.urlshortener.exception.ShortUrlNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlController {
    private final UrlShortenerService service;
    public UrlController(UrlShortenerService service){

        this.service = service;
    }
    @PostMapping("/api/urls")
//    public String createUrl(@RequestBody CreateUrlRequest request) {
//
//        return service.shortUrl(request.getUrl());
//    }
    public ResponseEntity<CreateUrlResponse> createUrl(@Valid @RequestBody CreateUrlRequest request){
        String url = service.shortUrl(request.getUrl());
        CreateUrlResponse response = new CreateUrlResponse(url);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{code}")
//    public String getOriginalUrl(@PathVariable String code){
//        return service.getOriginalUrl(code);
//    }
    public ResponseEntity<Void> redirect(@PathVariable String code){
        String originalUrl = service.getOriginalUrl(code);
//        if(originalUrl.equals("Not Found")){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION,originalUrl).build();
    }

}