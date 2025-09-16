package com.hospital.caller;

import com.hospital.config.RegionConfig;
import com.hospital.dto.OpenApiWrapper;
import com.hospital.dto.PharmacyApiItem;
import com.hospital.dto.PharmacyApiResponse;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class PharmacyApiCaller {

    @Value("${hospital.pharmacy.api.base-url}")
    private String baseUrl;

    @Value("${hospital.pharmacy.api.key}")
    private String serviceKey;
    
    private final RegionConfig regionConfig; 
    
    public PharmacyApiCaller(RegionConfig regionConfig) { 
        this.regionConfig = regionConfig;
    }
    
    public List<PharmacyApiItem>  callApiByDistrict(String sgguCd) {
        String fullUrl = null;
        
        try {
            fullUrl = baseUrl 
                    + "?serviceKey=" + serviceKey
                    + "&pageNo=1"
                    + "&sidoCd=" + regionConfig.getSidoCode() 
                    + "&sgguCd=" + sgguCd
                    + "&numOfRows=1000";
                    

            // URL 연결
            URL url = new URL(fullUrl);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();

            if (inputStream == null) {
                log.warn("API 응답 스트림이 비어있음");
                throw new RuntimeException("API 응답 스트림이 비어있습니다");
            }

            // UTF-8로 명시적 디코딩
            JAXBContext context = JAXBContext.newInstance(PharmacyApiResponse.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            // XML → 객체 변환
            PharmacyApiResponse result = (PharmacyApiResponse) unmarshaller.unmarshal(reader);
            
            if (result == null || result.getBody() == null) {
                log.warn("API 응답 파싱 결과가 비어있음");
                return List.of();  // 빈 리스트 반환
            }

            log.debug("약국 API 응답 파싱 성공");
            return result.getBody().getItems();  // List<PharmacyApiItem> 반환
        
        } catch (MalformedURLException e) {
            // URL 형식 오류
            log.error("잘못된 URL 형식: {}", fullUrl);
            throw new RuntimeException("Pharmacy API URL 형식 오류: " + e.getMessage(), e);

        } catch (IOException e) {
            // 네트워크 연결 오류
            log.error("네트워크 연결 오류 - URL: {}", fullUrl);
            throw new RuntimeException("Pharmacy API 네트워크 오류: " + e.getMessage(), e);

        } catch (JAXBException e) {
            // XML 파싱 오류
            log.error("XML 파싱 오류: {}", e.getMessage());
            throw new RuntimeException("Pharmacy API XML 파싱 오류: " + e.getMessage(), e);

        } catch (Exception e) {
            // 기타 예외
            log.error("약국 API 호출 중 예외 발생", e);
            throw new RuntimeException("약국 API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
}