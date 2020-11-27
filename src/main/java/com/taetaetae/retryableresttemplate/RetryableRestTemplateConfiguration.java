package com.taetaetae.retryableresttemplate;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@EnableRetry(proxyTargetClass=true)
@Configuration
@EnableAsync
@EnableCaching
public class RetryableRestTemplateConfiguration {
	private final static Logger loggerExt = LoggerFactory.getLogger(RestTemplate.class);



	@Bean
	public RestTemplate retryableRestTemplate() {
		loggerExt.info("*** @Configuration @Bean RetryableRestTemplateConfiguration retryableRestTemplate");
		SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
		clientHttpRequestFactory.setReadTimeout(2000);
		clientHttpRequestFactory.setConnectTimeout(500);

		RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory) {
			private final Logger logger = LoggerFactory.getLogger(RestTemplate.class);

			@Override
			@Retryable(value = RestClientException.class, maxAttempts = 7, backoff = @Backoff(delay = 1000)) // retry 3, delay 1000ms
			public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType)
				throws RestClientException {
				logger.debug(">>> RetryableRestTemplateConfiguration exchange {}"+ url);
				loggerExt.debug("+++ RetryableRestTemplateConfiguration exchange {}"+ url);
				System.out.println("~~~ RetryableRestTemplateConfiguration exchange {}"+ url);
				return super.exchange(url, method, requestEntity, responseType);
			}

			@Recover
			public <T> ResponseEntity<String> exchangeRecover(RestClientException e) {
				return ResponseEntity.badRequest().body("bad request T.T");
			}
		};

		return restTemplate;
	}
}