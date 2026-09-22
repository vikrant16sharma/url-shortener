package com.vikrant.urlshortener.Services.impl;

import com.vikrant.urlshortener.config.AppProperties;
import com.vikrant.urlshortener.entity.Url;
import com.vikrant.urlshortener.exception.InvalidExpirationException;
import com.vikrant.urlshortener.exception.ShortUrlExpiredException;
import com.vikrant.urlshortener.exception.ShortUrlNotFoundException;
import com.vikrant.urlshortener.repository.UrlRepository;
import com.vikrant.urlshortener.Services.CodeGenerator;
import com.vikrant.urlshortener.Services.UrlShortenerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UrlShortenerServiceImpl implements UrlShortenerService {

//    private static final String BASE_URL = "short.ly/";
    private final AppProperties appProperties;

    private final UrlRepository urlRepository;
    private final CodeGenerator codeGenerator;

    public UrlShortenerServiceImpl(
            UrlRepository urlRepository,
            CodeGenerator codeGenerator,
            AppProperties appProperties) {

        this.appProperties = appProperties;
        this.urlRepository = urlRepository;
        this.codeGenerator = codeGenerator;
    }

    @Override
    @Transactional
    public String shortUrl(String originalUrl,LocalDateTime expiresAt) {
        if(expiresAt != null &&
                !expiresAt.isAfter(LocalDateTime.now())
        ){
            throw new InvalidExpirationException(
                    "Expiration time must be in the future"
            );
        }
        return urlRepository
                .findByOriginalUrl(originalUrl)
                .map(url -> buildShortUrl(url.getShortCode()))
                .orElseGet(() -> createShortUrl(originalUrl,expiresAt));
    }

    private String createShortUrl(String originalUrl, LocalDateTime expiresAt) {

        String code;

        do {
            code = codeGenerator.generate();
        } while (urlRepository.findByShortCode(code).isPresent());

        Url url = new Url();

        url.setOriginalUrl(originalUrl);
        url.setShortCode(code);
        url.setExpiresAt(expiresAt);

        urlRepository.save(url);

        return buildShortUrl(code);
    }

    private String buildShortUrl(String code) {
        return appProperties.getBaseUrl() + code;
    }

    @Override
    @Transactional(readOnly = true)
    public String getOriginalUrl(String code) {

        Url url = urlRepository
                .findByShortCode(code)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(
                                "Short URL not found: " + code
                        )
                );
        if(
                url.getExpireAt() != null &&
                        !url.getExpireAt().isAfter(LocalDateTime.now())
        ){
            throw new ShortUrlExpiredException(
                    "Short Url has expired: "+ code
            );
        }

        return url.getOriginalUrl();
    }
}