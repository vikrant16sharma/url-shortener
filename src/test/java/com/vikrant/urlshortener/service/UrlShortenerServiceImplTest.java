package com.vikrant.urlshortener.service;

import com.vikrant.urlshortener.entity.Url;
import com.vikrant.urlshortener.exception.ShortUrlNotFoundException;
import com.vikrant.urlshortener.repository.UrlRepository;
import com.vikrant.urlshortener.Services.impl.UrlShortenerServiceImpl;
import com.vikrant.urlshortener.Services.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceImplTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private CodeGenerator codeGenerator;

    @InjectMocks
    private UrlShortenerServiceImpl service;

    @Test
    void shouldCreateShortUrlForNewUrl() {

        String originalUrl = "https://google.com";
        String generatedCode = "x7Kp91a";

        when(urlRepository.findByOriginalUrl(originalUrl))
                .thenReturn(Optional.empty());

        when(codeGenerator.generate())
                .thenReturn(generatedCode);

        when(urlRepository.findByShortCode(generatedCode))
                .thenReturn(Optional.empty());

        String result = service.shortUrl(originalUrl);

        assertEquals(
                "short.ly/" + generatedCode,
                result
        );

        verify(urlRepository).save(any(Url.class));
    }
    @Test
    void shouldReturnExistingShortUrlForDuplicateUrl() {

        String originalUrl = "https://google.com";

        Url existingUrl = new Url();

        existingUrl.setOriginalUrl(originalUrl);
        existingUrl.setShortCode("abc1234");

        when(urlRepository.findByOriginalUrl(originalUrl))
                .thenReturn(Optional.of(existingUrl));

        String result = service.shortUrl(originalUrl);

        assertEquals(
                "short.ly/abc1234",
                result
        );

        verify(codeGenerator, never()).generate();

        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    void shouldReturnOriginalUrl() {

        String code = "abc1234";
        String originalUrl = "https://google.com";

        Url url = new Url();

        url.setOriginalUrl(originalUrl);
        url.setShortCode(code);

        when(urlRepository.findByShortCode(code))
                .thenReturn(Optional.of(url));

        String result = service.getOriginalUrl(code);

        assertEquals(originalUrl, result);
    }
    @Test
    void shouldThrowExceptionWhenShortCodeDoesNotExist() {

        String code = "doesNotExist";

        when(urlRepository.findByShortCode(code))
                .thenReturn(Optional.empty());

        assertThrows(
                ShortUrlNotFoundException.class,
                () -> service.getOriginalUrl(code)
        );
    }

}