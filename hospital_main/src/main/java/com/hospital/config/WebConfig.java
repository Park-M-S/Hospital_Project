package com.hospital.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * 웹 및 HTTP 통신 관련 설정
 * - RestTemplate 설정
 * - HTTP 메시지 컨버터 설정
 * - 외부 API 호출 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final XmlMapper xmlMapper;

    public WebConfig(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    @Bean
    public RestTemplate restTemplate() {
      
    	//연결 타임아웃 설정 (ConnectionConfig 사용) 
    	ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(5))    // 연결 타임아웃
                .build();
    	
    	   //요청 타임아웃 설정 (RequestConfig 사용)
        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofSeconds(60))   // 응답 타임아웃 
                .build();
    	
    	   
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(15);            // 전체 최대 연결 수
        connManager.setDefaultMaxPerRoute(10);  // 호스트별 최대 연결 수
        connManager.setDefaultConnectionConfig(connectionConfig);  // 연결 타임아웃 설정

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)     // 응답 타임아웃 설정
                .build();

        //설정이 완료된 HttpClient로 Factory 생성
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        
        
        RestTemplate restTemplate = new RestTemplate(factory);

        // 메시지 컨버터 설정
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

        // UTF-8 String 컨버터
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false);
        messageConverters.add(stringConverter);

        // XML 컨버터
        MappingJackson2XmlHttpMessageConverter xmlConverter = new MappingJackson2XmlHttpMessageConverter();
        xmlConverter.setObjectMapper(xmlMapper);
        messageConverters.add(xmlConverter);

        restTemplate.setMessageConverters(messageConverters);

        // HTTP 헤더 인터셉터 추가
        restTemplate.setInterceptors(Collections.singletonList(new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                    throws IOException {
                
                // User-Agent 헤더 설정
                if (!request.getHeaders().containsKey(HttpHeaders.USER_AGENT)) {
                    request.getHeaders().set(HttpHeaders.USER_AGENT,
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
                }
                
                // Accept 헤더 설정
                if (!request.getHeaders().containsKey(HttpHeaders.ACCEPT)) {
                    request.getHeaders().setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
                }

                return execution.execute(request, body);
            }
        }));

        System.out.println("✅ RestTemplate 설정 완료. 메시지 컨버터 개수: " + messageConverters.size());
        return restTemplate;
    }
}