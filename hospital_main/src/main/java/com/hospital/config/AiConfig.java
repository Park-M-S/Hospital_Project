package com.hospital.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.openai.OpenAiChatModel;  // ✅ 올바른 클래스명
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.OpenAiChatOptions;

@Configuration
public class AiConfig {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Bean
    public OpenAiApi openAiApi() {
        return OpenAiApi.builder()
                .apiKey(openaiApiKey)
                .build();
    }

    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        var options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini")  // 비용 효율적인 모델
                .temperature(0.3)     // 의료 정보는 보수적으로
                .maxTokens(500)       // 적당한 응답 길이
                .build();
        
        return new OpenAiChatModel(openAiApi, options);
    }
}