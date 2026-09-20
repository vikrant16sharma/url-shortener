package com.vikrant.urlshortener.Services;

public interface UrlShortenerService {
    String shortUrl(String originalUrl);

    String getOriginalUrl(String code);

//    private final CodeGenerator codeGenerator;
//    private final UrlRepository urlRepository;
//
//    public UrlShortenerService(
//            UrlRepository urlRepository,
//            CodeGenerator codeGenerator) {
//        if (urlRepository == null) {
//            throw new IllegalArgumentException(
//                    "URL repository cannot be null"
//            );
//        }
//
//        if (codeGenerator == null) {
//            throw new IllegalArgumentException(
//                    "Code generator cannot be null"
//            );
//        }
//
//        this.urlRepository = urlRepository;
//        this.codeGenerator = codeGenerator;
//    }
//
//    @Transactional
//    public String shortUrl(String originalUrl) {
//
//
//        Optional<Url> existingUrl =
//                urlRepository.findByOriginalUrl(originalUrl);
//
//        if (existingUrl.isPresent()) {
//            return "short.ly/" +
//                    existingUrl.get().getShortCode();
//        }
//
//        String code;
//
//        do {
//            code = codeGenerator.generate();
//        } while (urlRepository.findByShortCode(code).isPresent());
//
//        Url url = new Url();
//
//        url.setOriginalUrl(originalUrl);
//        url.setShortCode(code);
//
//        urlRepository.save(url);
//
//        return "short.ly/" + code;
//    }
//
//    public String getOriginalUrl(String code) {
//        Url url = urlRepository
//                .findByShortCode(code)
//                .orElseThrow(()->
//                    new ShortUrlNotFoundException(
//                            "Short URL Not Found: "+ code
//                    )
//                );
//        return url.getOriginalUrl();
//    }
}