package com.vikrant.urlshortener;

import com.vikrant.urlshortener.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vikrant.urlshortener.config.AppProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(
		{AppProperties.class,
				JwtProperties.class}
)
public class UrlshortenerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UrlshortenerApplication.class, args); // main spring application starts from here
	}

}
