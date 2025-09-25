package com.hospital.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.hospital.interceptor.MetricsInterceptor;

/**
 * 웹 및 HTTP 통신 관련 설정
 * - RestTemplate 설정
 * - HTTP 메시지 컨버터 설정
 * - 외부 API 호출 설정
 * - 메트릭 인터셉터 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final XmlMapper xmlMapper;
    
    @Autowired
    private MetricsInterceptor metricsInterceptor;
    
    public WebConfig(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }
    
    /**
     * Spring MVC 인터셉터 등록 - 메트릭 수집용
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(metricsInterceptor)
                .addPathPatterns("/**")  // 모든 경로에 적용
                .excludePathPatterns(
                    "/actuator/**",      // 모니터링 엔드포인트 제외
                    "/static/**",        // 정적 리소스 제외
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/**/*.ico",
                    "/**/*.png",
                    "/**/*.jpg",
                    "/**/*.jpeg",
                    "/**/*.gif",
                    "/**/*.css",
                    "/**/*.js",
                    "/**/*.woff",
                    "/**/*.woff2",
                    "/**/*.ttf",
                    "/error"             // 에러 페이지 제외
                );
        
        System.out.println("✅ 메트릭 인터셉터 등록 완료");
    }

    /**
     * 콘텐츠 협상 설정 - JSON 우선
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorPathExtension(false)
                .favorParameter(false)
                .ignoreAcceptHeader(false)
                .useRegisteredExtensionsOnly(false)
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("xml", MediaType.APPLICATION_XML);
    }
    
    /**
     * RestTemplate Bean 설정 - 외부 API 호출용
     */
    @Bean
    public RestTemplate restTemplate() {
        // HTTP 클라이언트 팩토리 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);  // 15초 연결 타임아웃
        factory.setReadTimeout(60000);     // 60초 읽기 타임아웃
        
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
        
        // HTTP 헤더 인터셉터 추가 (외부 API 호출용)
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
                
                // 외부 API 호출 메트릭 수집 (선택사항)
                long startTime = System.currentTimeMillis();
                String uri = request.getURI().toString();
                
                try {
                    ClientHttpResponse response = execution.execute(request, body);
                    long duration = System.currentTimeMillis() - startTime;
                    
                    // 외부 API 호출 성공 로깅
                    System.out.println("📡 외부 API 호출 성공: " + uri + " (" + duration + "ms)");
                    
                    return response;
                } catch (Exception e) {
                    long duration = System.currentTimeMillis() - startTime;
                    
                    // 외부 API 호출 실패 로깅
                    System.err.println("❌ 외부 API 호출 실패: " + uri + " (" + duration + "ms) - " + e.getMessage());
                    
                    throw e;
                }
            }
        }));
        
        System.out.println("✅ RestTemplate 설정 완료. 메시지 컨버터 개수: " + messageConverters.size());
        return restTemplate;
    }
}
