package com.hospital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Jackson JSON/XML 매퍼 설정
 * - JSON 파싱용 ObjectMapper
 * - XML 파싱용 XmlMapper
 * - 공통 설정 적용
 */
@Configuration
public class JacksonConfig {

    /**
     * JSON 처리용 ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 공통 설정 적용
        configureCommonSettings(mapper);
        
        System.out.println("✅ ObjectMapper 설정 완료 (JSON 처리용)");
        return mapper;
    }

    /**
     * XML 처리용 XmlMapper
     */
    @Bean
    public XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        
        // 공통 설정 적용
        configureCommonSettings(xmlMapper);
        
        // XML 특화 설정
        xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        xmlMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

        System.out.println("✅ XmlMapper 설정 완료 (XML 처리용)");
        return xmlMapper;
    }

    /**
     * 공통 매퍼 설정
     */
    private void configureCommonSettings(ObjectMapper mapper) {
        // 알려지지 않은 속성 무시
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 빈 문자열을 null로 처리
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        
        // 빈 문자열 강제 변환 설정
        mapper.coercionConfigFor(LogicalType.POJO)
              .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
    }
}