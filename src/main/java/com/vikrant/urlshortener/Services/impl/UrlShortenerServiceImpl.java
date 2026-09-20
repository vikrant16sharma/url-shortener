package com.vikrant.urlshortener.Services.impl;

import com.vikrant.urlshortener.entity.Url;
import com.vikrant.urlshortener.exception.ShortUrlNotFoundException;
import com.vikrant.urlshortener.repository.UrlRepository;
import com.vikrant.urlshortener.Services.CodeGenerator;
import com.vikrant.urlshortener.Services.UrlShortenerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UrlShortenerServiceImpl implements UrlShortenerService {

    private static final String BASE_URL = "short.ly/";

    private final UrlRepository urlRepository;
    private final CodeGenerator codeGenerator;

    public UrlShortenerServiceImpl(
            UrlRepository urlRepository,
            CodeGenerator codeGenerator) {

        this.urlRepository = urlRepository;
        this.codeGenerator = codeGenerator;
    }

    @Override
    @Transactional
    public String shortUrl(String originalUrl) {

        return urlRepository
                .findByOriginalUrl(originalUrl)
                .map(url -> BASE_URL + url.getShortCode())
                .orElseGet(() -> createShortUrl(originalUrl));
    }

    private String createShortUrl(String originalUrl) {

        String code;

        do {
            code = codeGenerator.generate();
        } while (urlRepository.findByShortCode(code).isPresent());

        Url url = new Url();

        url.setOriginalUrl(originalUrl);
        url.setShortCode(code);

        urlRepository.save(url);

        return BASE_URL + code;
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

        return url.getOriginalUrl();
    }
}