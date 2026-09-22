package com.vikrant.urlshortener.Services.impl;

import com.vikrant.urlshortener.config.AppProperties;
import com.vikrant.urlshortener.dto.UrlAnalyticsResponse;
import com.vikrant.urlshortener.entity.ClickEvent;
import com.vikrant.urlshortener.entity.Url;
import com.vikrant.urlshortener.exception.InvalidExpirationException;
import com.vikrant.urlshortener.exception.ShortUrlExpiredException;
import com.vikrant.urlshortener.exception.ShortUrlNotFoundException;
import com.vikrant.urlshortener.repository.ClickEventRepository;
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
    private final ClickEventRepository clickEventRepository;

    public UrlShortenerServiceImpl(
            UrlRepository urlRepository,
            CodeGenerator codeGenerator,
            AppProperties appProperties,
            ClickEventRepository clickEventRepository) {

        this.appProperties = appProperties;
        this.urlRepository = urlRepository;
        this.codeGenerator = codeGenerator;
        this.clickEventRepository = clickEventRepository;
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
    @Transactional
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
        ClickEvent clickEvent = new ClickEvent();
        clickEvent.setUrl(url);
        clickEvent.setClickedAt(LocalDateTime.now());
        clickEventRepository.save(clickEvent);


        return url.getOriginalUrl();
    }
    @Override
    @Transactional(readOnly = true)
    public UrlAnalyticsResponse getAnalytics(String code) {

        Url url = urlRepository
                .findByShortCode(code)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(
                                "Short URL not found: " + code
                        )
                );

        long totalClicks =
                clickEventRepository.countByUrlId(url.getId());

        LocalDateTime firstClickedAt =
                clickEventRepository
                        .findFirstByUrlIdOrderByClickedAtAsc(url.getId())
                        .map(ClickEvent::getClickedAt)
                        .orElse(null);

        LocalDateTime lastClickedAt =
                clickEventRepository
                        .findFirstByUrlIdOrderByClickedAtDesc(url.getId())
                        .map(ClickEvent::getClickedAt)
                        .orElse(null);

        return new UrlAnalyticsResponse(
                url.getShortCode(),
                url.getOriginalUrl(),
                totalClicks,
                firstClickedAt,
                lastClickedAt
        );
    }
}